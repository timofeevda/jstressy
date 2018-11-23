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
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.utils.StressyUtils.parseDuration
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Basic implementation of [ScenarioRegistryService]. Implements basic scheduling reading arrivalRate,
 * rampArrival etc. values from stages configuration
 *
 * @author timofeevda
 */
class StressyScenariosScheduler(private val vertxService: VertxService,
                                private val requestExecutorService: RequestExecutorService,
                                private val metricsRegistry: MetricsRegistry,
                                private val configurationService: ConfigurationService,
                                val scenarioRegistryService: ScenarioRegistryService) : ScenarioSchedulerService {

    val listOfStages: List<StressyStage>
        get() = configurationService.configuration.stressPlan.stages

    override fun observeScenarios(): Observable<Scenario> {
        return observeScenarios(configurationService.configuration.stressPlan.stages)
    }

    private fun observeScenarios(stages: List<StressyStage>): Observable<Scenario> {
        return stages.stream()
                .map<Observable<Scenario>> { this.observeScenarios(it) }
                .reduce(Observable.empty()) { source1, source2 -> Observable.merge(source1, source2) }
    }

    private fun observeScenarios(stage: StressyStage): Observable<Scenario> {
        val delay = parseDuration(stage.stageDelay ?: "0ms").toMilliseconds()
        val duration = parseDuration(stage.stageDuration).toMilliseconds()
        return Observable.timer(delay, TimeUnit.MILLISECONDS)
                .flatMap {
                    observeWithRamping(stage)
                            .orElseGet { observeWithoutRamping(stage) }
                }
                .take(delay + duration, TimeUnit.MILLISECONDS)
    }

    private fun observeWithoutRamping(stage: StressyStage): Observable<Scenario> {
        return Observable.interval(0, (1000 / stage.arrivalRate).toLong(), TimeUnit.MILLISECONDS)
                .flatMap { createScenario(stage) }
    }

    private fun createScenario(stage: StressyStage): Observable<Scenario> {
        val scenarioProviderService = scenarioRegistryService[stage.scenarioName]
        val scenarioProvider = scenarioProviderService?.get(stage.scenarioProviderParameters)
        try {
            scenarioProvider?.init(metricsRegistry, requestExecutorService, configurationService, vertxService)
        } catch (e: Throwable) {
            return Observable.error(e)
        }
        return Observable.just(scenarioProvider?.get()?.withParameters(stage.scenarioParameters))
    }

    private fun observeWithRamping(stage: StressyStage): Optional<Observable<Scenario>> {
        if (stage.rampArrival != null
                && stage.rampArrivalRate != null
                && stage.rampInterval != null) {
            val rampInterval = rateToIntervalInMillis(stage.rampArrivalRate ?: 1.0)
            val rampDuration = parseDuration(stage.rampInterval ?: "0ms").toMilliseconds()
            val rampSteps = (rampDuration / rampInterval).toInt()
            val rampIncrease = (stage.rampArrival ?: 1.0 - stage.arrivalRate) / rampSteps
            return Optional.of(Observable.interval(0, rampInterval, TimeUnit.MILLISECONDS)
                    .take(rampSteps.toLong())
                    .map { i -> stage.arrivalRate + rampIncrease * i }
                    .switchMap { newRate ->
                        Observable.interval(rateToIntervalInMillis(newRate), TimeUnit.MILLISECONDS)
                                .flatMap { createScenario(stage) }
                    })
        } else {
            return Optional.empty()
        }
    }

    private fun rateToIntervalInMillis(rate: Double): Long {
        return (1000 / rate).toLong()
    }
}
