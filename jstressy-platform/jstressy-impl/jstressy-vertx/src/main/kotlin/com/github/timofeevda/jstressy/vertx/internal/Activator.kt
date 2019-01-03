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

package com.github.timofeevda.jstressy.vertx.internal

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.utils.StressyUtils.observeService
import com.github.timofeevda.jstressy.utils.StressyUtils.serviceAwaitTimeout
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import com.github.timofeevda.jstressy.vertx.OSGIStressyVertxService
import com.github.timofeevda.jstressy.vertx.metrics.StressyVertxMetricsService
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import java.util.*
import java.util.concurrent.TimeUnit

class Activator : BundleActivator {

    companion object : LazyLogging()

    private var subscription: Disposable? = null

    override fun start(context: BundleContext) {
        logger.info("Starting Vertx service activator")

        val stressyMetricsService = observeService<StressyVertxMetricsService>(StressyVertxMetricsService::class.java.name, context)
        val metricsRegistryService = observeService<MetricsRegistryService>(MetricsRegistryService::class.java.name, context)

        Observable.combineLatest<StressyVertxMetricsService, MetricsRegistryService, VertxService>(
                stressyMetricsService.toObservable(),
                metricsRegistryService.toObservable(),
                BiFunction<StressyVertxMetricsService, MetricsRegistryService, VertxService> { vxService, metricsRegistry -> OSGIStressyVertxService(context, metricsRegistry.get()) })
                .doOnSubscribe { logger.info("Vertx service subscribes on Configuration service and Metrics service") }
                .doOnNext { logger.info("Registering Vertx service") }
                .timeout(serviceAwaitTimeout().toMilliseconds(), TimeUnit.MILLISECONDS)
                .subscribe { vertxService ->
                    context.registerService(VertxService::class.java.name, vertxService, Hashtable<Any, Any>())
                }
    }

    override fun stop(context: BundleContext) {
        logger.info("Stopping Verx service bundle")
        subscription?.dispose()
    }
}
