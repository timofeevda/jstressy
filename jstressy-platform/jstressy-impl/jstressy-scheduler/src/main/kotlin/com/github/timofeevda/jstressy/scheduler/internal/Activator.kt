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

package com.github.timofeevda.jstressy.scheduler.internal

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.scheduler.ScenarioProvidersTracker
import com.github.timofeevda.jstressy.scheduler.StressyScenariosScheduler
import com.github.timofeevda.jstressy.utils.StressyUtils.observeService
import com.github.timofeevda.jstressy.utils.StressyUtils.serviceAwaitTimeout
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function5
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import java.util.concurrent.TimeUnit

class Activator : BundleActivator {

    companion object : LazyLogging()

    override fun start(context: BundleContext) {
        logger.info("Starting scenarios scheduler service activator")

        val configurationServiceSingle = observeService<ConfigurationService>(ConfigurationService::class.java.name, context)
        val vertxServiceSingle = observeService<VertxService>(VertxService::class.java.name, context)
        val scenarioRegistryServiceSingle = observeService<ScenarioRegistryService>(ScenarioRegistryService::class.java.name, context)
        val metricsRegistryServiceSingle = observeService<MetricsRegistryService>(MetricsRegistryService::class.java.name, context)
        val requestExecutorServiceSingle = observeService<RequestExecutorService>(RequestExecutorService::class.java.name, context)

        Observable.combineLatest<ConfigurationService, ScenarioRegistryService, MetricsRegistryService, RequestExecutorService, VertxService, StressyScenariosScheduler>(
                configurationServiceSingle.toObservable(),
                scenarioRegistryServiceSingle.toObservable(),
                metricsRegistryServiceSingle.toObservable(),
                requestExecutorServiceSingle.toObservable(),
                vertxServiceSingle.toObservable(),
                Function5<ConfigurationService, ScenarioRegistryService, MetricsRegistryService, RequestExecutorService, VertxService, StressyScenariosScheduler> { configurationService, scenarioRegistryService, metricsRegistryService, requestExecutorService, vertxService -> this.toScenariosSchedulerService(configurationService, scenarioRegistryService, metricsRegistryService, requestExecutorService, vertxService) }        )
                .doOnSubscribe { logger.info("Scenario Scheduler service subscribes on Configuration, Scenario Registry, Metrics Registry, HTTP Request Executor, VertX service") }
                .timeout(serviceAwaitTimeout().toMilliseconds(), TimeUnit.MILLISECONDS)
                .doOnNext { logger.info("Registering Scenario Scheduler") }
                .flatMap { stressyScenariosScheduler -> toObservableScenarioScheduler(context, stressyScenariosScheduler) }
                .flatMap { it.observeScenarios() }
                .subscribe(
                        { scenario -> scenario.start() },
                        { throwable -> logger.error("Error in scheduled scenarios stream", throwable) })
    }

    override fun stop(context: BundleContext) {

    }

    private fun toObservableScenarioScheduler(context: BundleContext, stressyScenariosScheduler: StressyScenariosScheduler): ObservableSource<out StressyScenariosScheduler> {
        val scenarioProvidersTracker = ScenarioProvidersTracker(context, stressyScenariosScheduler.scenarioRegistryService)
        scenarioProvidersTracker.trackScenarioProviders(
                stressyScenariosScheduler.listOfStages.map { it.scenarioName }.distinct())
        return scenarioProvidersTracker.observeScenarioProviders()
                .andThen(Observable.just(stressyScenariosScheduler))
    }

    private fun toScenariosSchedulerService(configurationService: ConfigurationService,
                                            scenarioRegistryService: ScenarioRegistryService,
                                            metricsRegistryService: MetricsRegistryService,
                                            requestExecutorService: RequestExecutorService,
                                            vertxService: VertxService): StressyScenariosScheduler {
        return StressyScenariosScheduler(vertxService, requestExecutorService,
                configurationService, metricsRegistryService, scenarioRegistryService)
    }

}
