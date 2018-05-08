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

import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService;
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Waits for all scenario provider registrations and notifies listeners only if all required providers are registered
 * as services
 *
 * @author timofeevda
 */
public class ScenarioProvidersTracker extends ServiceTracker {

    private final CopyOnWriteArrayList<CompletableEmitter> emittersList = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> scenariosToWait = new CopyOnWriteArrayList<>();

    private final ScenarioRegistryService scenarioRegistryService;

    public ScenarioProvidersTracker(BundleContext context, ScenarioRegistryService scenarioRegistryService) {
        super(context, ScenarioProviderService.class.getName(), null);
        this.scenarioRegistryService = scenarioRegistryService;
    }

    @Override
    public Object addingService(ServiceReference reference) {
        ScenarioProviderService scenarioProviderService = (ScenarioProviderService) super.addingService(reference);
        registerScenarioProviderService(scenarioProviderService.getScenarioName(), scenarioProviderService);
        scenariosToWait.remove(scenarioProviderService.getScenarioName());
        if (scenariosToWait.isEmpty()) {
            emittersList.forEach(CompletableEmitter::onComplete);
        }
        return scenarioProviderService;
    }

    public void trackScenarioProviders(List<String> scenarioNames) {
        this.scenariosToWait.addAll(scenarioNames);
        this.open();
    }

    public Completable observeScenarioProviders() {
        return Completable.create(emitter -> {
            emittersList.add(emitter);
            if (scenariosToWait.isEmpty()) {
                emitter.onComplete();
            }
            emitter.setCancellable(() -> emittersList.remove(emitter));
        });
    }

    private void registerScenarioProviderService(String scenarioName, ScenarioProviderService scenarioProviderService) {
        scenarioRegistryService.registerScenarioProviderService(scenarioName, scenarioProviderService);
    }
}
