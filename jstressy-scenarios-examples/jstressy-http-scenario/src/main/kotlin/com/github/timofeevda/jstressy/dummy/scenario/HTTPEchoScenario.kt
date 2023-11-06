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
import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import java.util.concurrent.atomic.AtomicLong

private val globalCounter = AtomicLong(0)

/**
 * Example implementation of scenario. Just tries to GET data from https://host:port/get/number
 */
class HTTPEchoScenario internal constructor(private val metricsRegistry: MetricsRegistry,
                                            private val requestExecutor: RequestExecutor,
                                            configurationService: ConfigurationService) : Scenario {

    companion object : LazyLogging()

    private val host: String = configurationService.configuration.globals.host
    private val port: Int = configurationService.configuration.globals.port

    override fun start(actions: List<ScenarioActionDefinition>) {
        val count = globalCounter.incrementAndGet()
        requestExecutor.get(host, port, "/get/$count")
                .doOnSubscribe { logger.info("Going to request path /get/$count") }
                .doOnSuccess { metricsRegistry.counter("echo_request_success", "Number of successful requests").inc() }
                .subscribe(
                        { httpClientResponse ->
                                logger.info("Host $host answered with code ${httpClientResponse.statusCode()} for $count request")
                        },
                        { t -> logger.error("Error getting response from $host", t) })
    }

    override fun stop() {

    }

    override fun withArrivalInterval(intervalId: String): Scenario {
        return this
    }

    override fun withParameters(parameters: Map<String, String>): Scenario {
        return this
    }

    override fun createAction(action: String, parameters: Map<String, String>, run: ((metricsRegistry: MetricsRegistry, requestExecutor: RequestExecutor) -> Unit)?, intervalId: String): ScenarioAction {
        return object : ScenarioAction {
            override fun run() {
                // do nothing
            }
        }
    }

    override fun withActionDistributionId(id: String): Scenario {
        return this
    }

    override fun getActionDistributionId(): String? = null

    override fun isAvailableForActionDistribution() : Boolean = false

}
