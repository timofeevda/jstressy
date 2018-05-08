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

package com.github.timofeevda.jstressy.scheduler;

import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage;
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.scenario.Scenario;
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider;
import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService;
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService;
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService;
import com.github.timofeevda.jstressy.api.vertx.VertxService;
import io.dropwizard.util.Duration;
import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Basic implementation of {@link ScenarioRegistryService}. Implements basic scheduling reading arrivateRate,
 * rampArrival etc. values from stages configuration
 *
 * @author timofeevda
 */
public class StressyScenariosScheduler implements ScenarioSchedulerService {

    private final VertxService vertxService;
    private final RequestExecutor requestExecutor;
    private final MetricsRegistry metricsRegistry;
    private final ConfigurationService configurationService;
    private final ScenarioRegistryService scenarioRegistryService;

    public StressyScenariosScheduler(VertxService vertxService,
                                     RequestExecutor requestExecutor,
                                     MetricsRegistry metricsRegistry,
                                     ConfigurationService configurationService,
                                     ScenarioRegistryService scenarioRegistryService) {
        this.configurationService = configurationService;
        this.scenarioRegistryService = scenarioRegistryService;
        this.metricsRegistry = metricsRegistry;
        this.requestExecutor = requestExecutor;
        this.vertxService = vertxService;
    }

    @Override
    public Observable<Scenario> observeScenarios() {
        return observeScenarios(configurationService.getConfiguration().getStressPlan().getStages());
    }

    private Observable<Scenario> observeScenarios(List<StressyStage> stages) {
        return stages.stream()
                .map(this::observeScenarios)
                .reduce(Observable.empty(), Observable::merge);
    }

    private Observable<Scenario> observeScenarios(StressyStage stage) {
        long delay = Duration.parse(stage.getStageDelay()).toMilliseconds();
        long duration = Duration.parse(stage.getStageDuration()).toMilliseconds();
        return Observable.timer(delay, TimeUnit.MILLISECONDS)
                .flatMap(t ->
                        observeWithRamping(stage)
                                .orElseGet(() -> observeWithoutRamping(stage)))
                .take(delay + duration, TimeUnit.MILLISECONDS);
    }

    private Observable<Scenario> observeWithoutRamping(StressyStage stage) {
        ScenarioProviderService scenarioProviderService = scenarioRegistryService.get(stage.getScenarioName());
        ScenarioProvider scenarioProvider = scenarioProviderService.get(stage.getScenarioProviderParameters());
        scenarioProvider.init(metricsRegistry, requestExecutor, configurationService, vertxService);
        return Observable.interval((long) (1000 / stage.getArrivalRate()), TimeUnit.MILLISECONDS)
                .map(i -> scenarioProvider.get().withParameters(stage.getScenarioParameters()));
    }

    private Optional<Observable<Scenario>> observeWithRamping(StressyStage stage) {
        if (stage.getRampArrival() != -1
                && stage.getRampArrivalRate() != -1
                && stage.getRampInterval() != null) {
            ScenarioProviderService scenarioProviderService = scenarioRegistryService.get(stage.getScenarioName());
            ScenarioProvider scenarioProvider = scenarioProviderService.get(stage.getScenarioProviderParameters());
            scenarioProvider.init(metricsRegistry, requestExecutor, configurationService, vertxService);

            long rampInterval = rateToIntervalInMillis(stage.getRampArrivalRate());
            long rampDuration = Duration.parse(stage.getRampInterval()).toMilliseconds();
            int rampSteps = (int) (rampDuration / rampInterval);
            double rampIncrease = (stage.getRampArrival() - stage.getArrivalRate()) / rampSteps;
            return Optional.of(Observable.interval(0, rampInterval, TimeUnit.MILLISECONDS)
                    .take(rampSteps)
                    .map(i -> stage.getArrivalRate() + rampIncrease * i)
                    .switchMap(newRate ->
                            Observable.interval(rateToIntervalInMillis(newRate), TimeUnit.MILLISECONDS)
                                    .map(i -> scenarioProvider.get().withParameters(stage.getScenarioParameters()))
                    ));
        } else {
            return Optional.empty();
        }
    }

    private long rateToIntervalInMillis(double rate) {
        return (long) (1000 / rate);
    }

    public List<StressyStage> getListOfStages() {
        return configurationService.getConfiguration().getStressPlan().getStages();
    }

    public ScenarioRegistryService getScenarioRegistryService() {
        return scenarioRegistryService;
    }
}
