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
import com.github.timofeevda.jstressy.api.config.parameters.ActionDistributionMode
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.scenario.ScenarioWithActions
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.scheduler.ScenarioRateScheduler.observeScenarioActionArrivals
import com.github.timofeevda.jstressy.scheduler.ScenarioRateScheduler.observeScenarioArrivals
import io.reactivex.Observable
import java.util.concurrent.ConcurrentHashMap


/**
 * Basic implementation of [ScenarioRegistryService]. Implements basic scheduling reading arrivalRate,
 * rampArrival etc. values from stages configuration
 *
 * @author dtimofeev
 */
open class StressyScenariosScheduler(
    private val vertxService: VertxService,
    private val requestExecutorService: RequestExecutorService,
    private val configurationService: ConfigurationService,
    metricsRegistryService: MetricsRegistryService,
    private val scenarioRegistryService: ScenarioRegistryService
) : ScenarioSchedulerService {

    private val metricsRegistry = metricsRegistryService.get()

    private val distributedActions = ConcurrentHashMap<String, DistributedActionSelector>()

    override fun observeScenarios(): Observable<Scenario> = observeScenariosWithActions().map { swa -> swa.scenario }

    override fun observeScenariosWithActions() = observeScenarios(configurationService.configuration.stressPlan.stages)

    override fun observeScenarioActions(scenarioWithActions: ScenarioWithActions): Observable<ScenarioAction> {
        return scenarioWithActions.actions
            .mapIndexed { idx, actionDefinition ->
                if (actionDefinition.distributionMode != null && scenarioWithActions.scenario.getActionDistributionId() != null) {
                    val scenarioSelector = distributedActions.computeIfAbsent(scenarioWithActions.scenario.getActionDistributionId()!! + "-action-" + idx) {
                        actionDefinition.distributionMode?.let {
                            when (it) {
                                ActionDistributionMode.ROUND_ROBIN -> RoundRobinScenarioSelector(
                                    actionDefinition,
                                    observeScenarioActionArrivals(actionDefinition)
                                )

                                ActionDistributionMode.RANDOM -> RandomScenarioSelector(
                                    actionDefinition,
                                    observeScenarioActionArrivals(actionDefinition)
                                )

                                ActionDistributionMode.NONE -> NoOpScenarioSelector
                            }
                        } ?: NoOpScenarioSelector
                    }
                    scenarioSelector.observeActions(scenarioWithActions.scenario)
                } else {
                    observeScenarioActionArrivals(actionDefinition)
                        .map { arrivalIntervalId ->
                            scenarioWithActions.scenario.createAction(
                                actionDefinition.name,
                                actionDefinition.actionParameters,
                                arrivalIntervalId
                            )
                        }
                }
            }.fold(Observable.empty()) { acc, obs -> Observable.merge(acc, obs) }
    }

    private fun observeScenarios(stages: List<StressyStage>): Observable<ScenarioWithActions> {
        return stages.stream()
            .map { stage ->
                val distributedActions = stage.actions?.any { it.distributionMode != null } != null
                observeScenarioArrivals(stage)
                    .flatMap { arrivalIntervalId -> createScenario(stage, arrivalIntervalId, distributedActions) }
            }
            .reduce(Observable.empty()) { source1, source2 -> Observable.merge(source1, source2) }
    }

    private fun createScenario(
        stage: StressyStage,
        arrivalIntervalId: String?,
        distributedActions: Boolean
    ): Observable<ScenarioWithActions> {
        val scenarioProviderService = scenarioRegistryService[stage.scenarioName]
        val scenarioProvider = scenarioProviderService?.get(stage.scenarioProviderParameters)
        try {
            scenarioProvider?.init(metricsRegistry, requestExecutorService, configurationService, vertxService)
        } catch (e: Throwable) {
            return Observable.error(e)
        }

        var scenario = scenarioProvider?.get()?.withParameters(stage.scenarioParameters)
            ?: return Observable.error(IllegalStateException("Couldn't find scenario provider for \"${stage.scenarioName}\" scenario"))

        scenario = if (distributedActions) scenario.withActionDistributionId(stage.name) else scenario

        scenario = if (arrivalIntervalId != null) scenario.withArrivalInterval(arrivalIntervalId) else scenario

        return Observable.just(ScenarioWithActions(scenario, stage.actions ?: emptyList()))

    }

}
