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

package com.github.timofeevda.jstressy.dummy.scenario

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.utils.logging.LazyLogging

/**
 * Example implementation of scenario. Just tries to GET data from https://google.com/
 */
class TouchGoogleScenario internal constructor(private val metricsRegistry: MetricsRegistry,
                                               private val requestExecutor: RequestExecutor,
                                               configurationService: ConfigurationService) : Scenario {
    companion object : LazyLogging()

    private val host: String = configurationService.configuration.globals.host
    private val port: Int = configurationService.configuration.globals.port

    override fun start() {
        requestExecutor.get(host, port, "/")
                .doOnSuccess { httpClientResponse -> metricsRegistry.counter("googl_request_success").inc() }
                .subscribe(
                        { httpClientResponse ->
                            run {
                                logger.info("Host $host answered with code ${httpClientResponse.statusCode()}")
                                httpClientResponse.bodyHandler { event ->
                                    logger.info("Host $host answered with data $event")
                                }
                            }
                        },
                        { t -> logger.error("Error getting response from $host", t) })
    }

    override fun stop() {

    }

    override fun withParameters(parameters: Map<String, String>): Scenario {
        return this
    }

}
