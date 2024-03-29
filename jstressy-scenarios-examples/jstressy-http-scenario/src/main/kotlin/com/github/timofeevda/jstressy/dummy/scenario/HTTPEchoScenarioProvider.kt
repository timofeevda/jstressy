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
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.vertx.VertxService

/**
 * Example implementation of scenario provider
 *
 * @author timofeevda
 */
class HTTPEchoScenarioProvider : ScenarioProvider {
    private lateinit var metricsRegistry: MetricsRegistry
    private lateinit var requestExecutorService: RequestExecutorService
    private lateinit var configurationService: ConfigurationService
    private lateinit var scenarioSchedulerService: ScenarioSchedulerService

    override fun get(): Scenario {
        return HTTPEchoScenario(metricsRegistry, requestExecutorService.get(), configurationService)
    }

    override fun init(metricsRegistry: MetricsRegistry,
                      requestExecutorService: RequestExecutorService,
                      configurationService: ConfigurationService,
                      scenarioSchedulerService: ScenarioSchedulerService,
                      vertxService: VertxService) {
        this.metricsRegistry = metricsRegistry
        this.requestExecutorService = requestExecutorService
        this.configurationService = configurationService
        this.scenarioSchedulerService = scenarioSchedulerService
    }
}
