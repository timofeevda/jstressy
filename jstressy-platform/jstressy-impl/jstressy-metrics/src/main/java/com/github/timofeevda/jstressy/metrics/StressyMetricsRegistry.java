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

package com.github.timofeevda.jstressy.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.metrics.type.Counter;
import com.github.timofeevda.jstressy.api.metrics.type.Gauge;
import com.github.timofeevda.jstressy.api.metrics.type.Timer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Basic {@link MetricsRegistry} implementation
 *
 * @author timofeevda
 */
public class StressyMetricsRegistry implements MetricsRegistry {

    private final MetricRegistry metricRegistry;

    StressyMetricsRegistry() {
        metricRegistry = SharedMetricRegistries.getOrCreate("stressyMetrics");
        CollectorRegistry.defaultRegistry.register(new DropwizardExports(metricRegistry));
    }

    @Override
    public Counter counter(String name) {
        final com.codahale.metrics.Counter counter = metricRegistry.counter(name);
        return new Counter() {
            public void inc() {
                counter.inc();
            }

            public void dec() {
                counter.dec();
            }
        };
    }

    @Override
    public Timer timer(String name) {
        final com.codahale.metrics.Timer timer = metricRegistry.timer(name);
        return new Timer() {
            @Override
            public Context context() {
                final com.codahale.metrics.Timer.Context context = timer.time();
                return context::stop;
            }

            @Override
            public void record(long duration, TimeUnit timeUnit) {
                timer.update(duration, timeUnit);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Gauge gauge(String name, final Supplier<Double> valueSupplier) {
        throw new UnsupportedOperationException("Not implemented");
    }

    CollectorRegistry getMetricRegistry() {
        return CollectorRegistry.defaultRegistry;
    }
}
