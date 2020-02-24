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

package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.scheduler.ScenarioRateScheduler.observeScenarioTicks
import io.reactivex.Observable

/**
 * Basic implementation of [ScenarioRegistryService]. Implements basic scheduling reading arrivalRate,
 * rampArrival etc. values from stages configuration
 *
 * @author timofeevda
 */
open class StressyScenariosScheduler(private val vertxService: VertxService,
                                     private val requestExecutorService: RequestExecutorService,
                                     private val configurationService: ConfigurationService,
                                     metricsRegistryService: MetricsRegistryService,
                                     val scenarioRegistryService: ScenarioRegistryService) : ScenarioSchedulerService {

    private val metricsRegistry = metricsRegistryService.get()

    val listOfStages: List<StressyStage>
        get() = configurationService.configuration.stressPlan.stages

    override fun observeScenarios(): Observable<Scenario> {
        return observeScenarios(configurationService.configuration.stressPlan.stages)
    }

    private fun observeScenarios(stages: List<StressyStage>): Observable<Scenario> {
        return stages.stream()
                .map<Observable<Scenario>> { stage ->
                    observeScenarioTicks(stage)
                            .flatMap { arrivalIntervalId -> createScenario(stage, arrivalIntervalId) }
                }
                .reduce(Observable.empty()) { source1, source2 -> Observable.merge(source1, source2) }
    }

    private fun createScenario(stage: StressyStage, arrivalIntervalId: String?): Observable<Scenario> {
        val scenarioProviderService = scenarioRegistryService[stage.scenarioName]
        val scenarioProvider = scenarioProviderService?.get(stage.scenarioProviderParameters)
        try {
            scenarioProvider?.init(metricsRegistry, requestExecutorService, configurationService, vertxService)
        } catch (e: Throwable) {
            return Observable.error(e)
        }
        return if (arrivalIntervalId != null) {
            Observable.just(scenarioProvider?.get()?.withArrivalInterval(arrivalIntervalId)!!
                    .withParameters(stage.scenarioParameters));
        } else {
            Observable.just(scenarioProvider?.get()?.withParameters(stage.scenarioParameters))
        }
    }

}
