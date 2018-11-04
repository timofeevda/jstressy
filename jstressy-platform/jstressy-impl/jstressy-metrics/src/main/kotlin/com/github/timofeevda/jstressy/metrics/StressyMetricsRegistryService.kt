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

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import io.vertx.reactivex.ext.web.Router
import java.io.IOException

/**
 * Basic implementation of [MetricsRegistryService]. Publishes metrics in Prometheus format getting
 * endpoint configuration from JStressy configuration service
 *
 * @author timofeevda
 */
class StressyMetricsRegistryService : MetricsRegistryService {

    private val metricsRegistry: StressyMetricsRegistry = StressyMetricsRegistry()

    override fun get(): MetricsRegistry {
        return metricsRegistry
    }

    fun publishMetrics(vertxService: VertxService,
                       configurationService: ConfigurationService) {
        val stressyMetricsPublisher = StressyMetricsPublisher(metricsRegistry)

        val router = Router.router(vertxService.vertx)
        val metricsPath = configurationService.configuration.globals.stressyMetricsPath
        router.get(metricsPath).handler { event ->
            try {
                stressyMetricsPublisher.publish(event.request(), event.response())
            } catch (e: IOException) {
                event.fail(e)
            }
        }


        val port = configurationService.configuration.globals.stressyMetricsPort

        if (port != null) {
            vertxService.vertx
                    .createHttpServer()
                    .requestHandler { router.accept(it) }
                    .listen(port)
        }

    }
}
