/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Denis Timofeev <timofeevda@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *
 */

package com.github.timofeevda.jstressy.metrics

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.SharedMetricRegistries
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.type.Counter
import com.github.timofeevda.jstressy.api.metrics.type.Gauge
import com.github.timofeevda.jstressy.api.metrics.type.Timer
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.dropwizard.DropwizardExports

import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * Basic [MetricsRegistry] implementation
 *
 * @author timofeevda
 */
class StressyMetricsRegistry internal constructor() : MetricsRegistry {

    private val metricRegistry: MetricRegistry = SharedMetricRegistries.getOrCreate("stressyMetrics")

    fun getCollectorMetricRegistry(): CollectorRegistry {
        return CollectorRegistry.defaultRegistry
    }

    init {
        CollectorRegistry.defaultRegistry.register(DropwizardExports(metricRegistry))
    }

    override fun counter(name: String): Counter {
        val counter = metricRegistry.counter(name)
        return object : Counter {
            override fun inc() {
                counter.inc()
            }
        }
    }

    override fun timer(name: String): Timer {
        val timer = metricRegistry.timer(name)
        return object : Timer {
            override fun context(): Timer.Context {
                val context = timer.time()
                return object : Timer.Context {
                    override fun stop() {
                        context.stop()
                    }
                }
            }

            override fun record(duration: Long, timeUnit: TimeUnit) {
                timer.update(duration, timeUnit)
            }
        }
    }

    override fun gauge(name: String, valueSupplier: Supplier<Double>): Gauge {
        return object : Gauge {
            override val value: Double
                get() = valueSupplier.get()
        }
    }

}
