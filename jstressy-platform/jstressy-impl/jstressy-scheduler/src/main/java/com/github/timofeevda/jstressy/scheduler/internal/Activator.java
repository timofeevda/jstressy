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

package com.github.timofeevda.jstressy.scheduler.internal;

import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage;
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService;
import com.github.timofeevda.jstressy.api.scenario.Scenario;
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService;
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService;
import com.github.timofeevda.jstressy.api.vertx.VertxService;
import com.github.timofeevda.jstressy.scheduler.ScenarioProvidersTracker;
import com.github.timofeevda.jstressy.scheduler.StressyScenariosScheduler;
import com.github.timofeevda.jstressy.utils.ServiceObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class Activator implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) {
        logger.info("Starting scenarios scheduler service activator");

        Single<ConfigurationService> observeConfigurationService = ServiceObserver.observeService(ConfigurationService.class.getName(), context);
        Single<VertxService> vertxService = ServiceObserver.observeService(VertxService.class.getName(), context);
        Single<ScenarioRegistryService> configurationService = ServiceObserver.observeService(ScenarioRegistryService.class.getName(), context);
        Single<MetricsRegistryService> metricsRegistryServiceSingle = ServiceObserver.observeService(MetricsRegistryService.class.getName(), context);
        Single<RequestExecutorService> requestExecutorServiceSingle = ServiceObserver.observeService(RequestExecutorService.class.getName(), context);

        Observable.combineLatest(
                observeConfigurationService.toObservable(),
                configurationService.toObservable(),
                metricsRegistryServiceSingle.toObservable(),
                requestExecutorServiceSingle.toObservable(),
                vertxService.toObservable(),
                this::toScenariosSchedulerService
        ).flatMap(stressyScenariosScheduler -> toObservableScenarioScheduler(context, stressyScenariosScheduler))
                .map(ScenarioSchedulerService::observeScenarios)
                .subscribe(scenarioObservable -> scenarioObservable.subscribe(Scenario::start));

    }

    @Override
    public void stop(BundleContext context) {

    }

    private ObservableSource<? extends StressyScenariosScheduler> toObservableScenarioScheduler(BundleContext context, StressyScenariosScheduler stressyScenariosScheduler) {
        ScenarioProvidersTracker scenarioProvidersTracker =
                new ScenarioProvidersTracker(context, stressyScenariosScheduler.getScenarioRegistryService());
        scenarioProvidersTracker.trackScenarioProviders(
                stressyScenariosScheduler.getListOfStages()
                        .stream().map(StressyStage::getScenarioName).distinct().collect(Collectors.toList()));
        return scenarioProvidersTracker.observeScenarioProviders()
                .andThen(Observable.just(stressyScenariosScheduler));
    }

    private StressyScenariosScheduler toScenariosSchedulerService(ConfigurationService configurationService,
                                                                  ScenarioRegistryService scenarioRegistryService,
                                                                  MetricsRegistryService metricsRegistryService,
                                                                  RequestExecutorService requestExecutorService,
                                                                  VertxService vertxService) {
        return new StressyScenariosScheduler(vertxService, requestExecutorService.get(),
                metricsRegistryService.get(), configurationService, scenarioRegistryService);
    }
}
