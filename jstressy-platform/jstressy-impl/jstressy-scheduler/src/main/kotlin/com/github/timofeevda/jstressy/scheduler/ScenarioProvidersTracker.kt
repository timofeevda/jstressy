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

import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.util.tracker.ServiceTracker
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Waits for all scenario provider registrations and notifies listeners only if all required providers are registered
 * as services. Provides a ways to update scenario provider without the need to restart application
 *
 * @author timofeevda
 */
class ScenarioProvidersTracker(context: BundleContext, private val scenarioRegistryService: ScenarioRegistryService) : ServiceTracker<ScenarioProviderService, ScenarioProviderService>(context, ScenarioProviderService::class.java.name, null) {

    private val scenariosToWait = CopyOnWriteArrayList<String>()

    private val observeAwaitComplete = CompletableSubject.create()

    override fun addingService(reference: ServiceReference): ScenarioProviderService {
        val scenarioProviderService = super.addingService(reference)
        registerScenarioProviderService(scenarioProviderService.scenarioName, scenarioProviderService)
        scenariosToWait.remove(scenarioProviderService.scenarioName)
        if (scenariosToWait.isEmpty()) {
            observeAwaitComplete.onComplete()
        }
        return scenarioProviderService
    }

    fun trackScenarioProviders(scenarioNames: List<String>) {
        this.scenariosToWait.addAll(scenarioNames)
        this.open()
    }

    fun observeScenarioProviders(): Completable {
        return observeAwaitComplete
    }

    private fun registerScenarioProviderService(scenarioName: String, scenarioProviderService: ScenarioProviderService) {
        scenarioRegistryService.registerScenarioProviderService(scenarioName, scenarioProviderService)
    }
}
