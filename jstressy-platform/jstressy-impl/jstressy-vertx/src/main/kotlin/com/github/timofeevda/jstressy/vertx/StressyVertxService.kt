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

package com.github.timofeevda.jstressy.vertx

import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.metrics.micrometer.MicrometerMetricsRegistryService
import com.github.timofeevda.jstressy.utils.StressyUtils.getBlockedEventLoopThreadTimeout
import io.micrometer.core.instrument.config.MeterFilter
import io.vertx.core.VertxOptions
import io.vertx.micrometer.Label
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.VertxPrometheusOptions
import io.vertx.reactivex.core.Vertx
import java.util.concurrent.TimeUnit

/**
 * Path labels override definition that can be used to reduce the cardinality of the label by replacing it with a generic one.
 *
 * For example, HTTP client may generate following path labels for DELETE request metric:
 *
 * /api/user/1
 * /api/user/2
 * /api/user/3
 * /api/user/4
 *
 * To avoid having this number of labels, path label override can be set with "/api/user/.*" as a regex expression and
 * "/api/user" as path label override
 */
data class PathLabelOverride(val regex: Regex, val override: String)

/**
 * Service providing Vertx instance
 *
 * @param enablePathLabelsInMetrics enable Micrometer labels (aka tags or fields) that are used to provide dimensionality to HTTP client metrics. Consider using pathLabelOverrides if you expect metric labels with high cardinality
 * @param pathLabelsOverrides enabling labels may result in a high cardinality in values, which can cause troubles on the metrics backend and affect performance. Passing path labels overrides allows to reduce cardinality by replacing such labels with a generic ones
 * @author timofeevda
 */
open class StressyVertxService(
    private val metricsRegistryService: MicrometerMetricsRegistryService,
    private val enablePathLabelsInMetrics: Boolean = false,
    private val pathLabelsOverrides: List<PathLabelOverride> = emptyList()
) : VertxService {
    override val vertx: Vertx
        get() {

            if (enablePathLabelsInMetrics && pathLabelsOverrides.isNotEmpty()) {
                metricsRegistryService.metricsRegistry.prometheusRegistry.config()
                    .meterFilter(
                        MeterFilter.replaceTagValues(
                        Label.HTTP_PATH.toString(),
                        { actualPath ->
                            pathLabelsOverrides.firstOrNull { it.regex.containsMatchIn(actualPath) }?.override ?: actualPath
                        }
                    ))
            }

            return Vertx.vertx(
                VertxOptions()
                    .setWarningExceptionTime(getBlockedEventLoopThreadTimeout().toMilliseconds())
                    .setWarningExceptionTimeUnit(TimeUnit.MILLISECONDS)
                    .setMetricsOptions(
                        MicrometerMetricsOptions()
                            .setPrometheusOptions(VertxPrometheusOptions().setEnabled(true))
                            .setJvmMetricsEnabled(true)
                            .setMicrometerRegistry(metricsRegistryService.metricsRegistry.compositeMetricsRegistry)
                            .addLabels(*(if (enablePathLabelsInMetrics) arrayOf(Label.HTTP_PATH) else emptyArray()))
                            .setEnabled(true)
                    )
            )
        }

}

