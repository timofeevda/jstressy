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

package com.github.timofeevda.jstressy.httpclient.internal

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httpclient.HttpClientService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.httpclient.StressyHttpClientService
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import com.github.timofeevda.jstressy.utils.StressyUtils.observeService as observeService
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author timofeevda
 */
class Activator : BundleActivator {

    companion object : LazyLogging()

    var subscription: Disposable? = null

    override fun start(context: BundleContext) {
        logger.info("Starting HTTP Client service activator")

        val vertxService = observeService<VertxService>(
                VertxService::class.java.name, context)

        val configurationService = observeService<ConfigurationService>(
                ConfigurationService::class.java.name, context)

        subscription = Observable.combineLatest<VertxService, ConfigurationService, StressyHttpClientService>(
                vertxService.toObservable(),
                configurationService.toObservable(),
                BiFunction<VertxService, ConfigurationService, StressyHttpClientService> { vxService, configService -> StressyHttpClientService(vxService, configService) })
                .doOnSubscribe { logger.info("HTTP client service subscribes on VertX and Configuration services") }
                .doOnNext { logger.info("Registering HTTP client service") }
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(
                        { httpClientService ->
                            context.registerService(HttpClientService::class.java.name, httpClientService, Hashtable<Any, Any>())
                        },
                        { t -> logger.error("Error registering HTTP client service", t) })
    }

    override fun stop(context: BundleContext) {
        subscription?.dispose()
    }
}
