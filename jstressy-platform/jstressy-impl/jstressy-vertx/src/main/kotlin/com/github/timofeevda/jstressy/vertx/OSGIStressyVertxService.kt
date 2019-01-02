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

package com.github.timofeevda.jstressy.vertx

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.utils.StressyUtils.getBlockedEventLoopThreadTimeout
import com.github.timofeevda.jstressy.utils.osgi.SPIHelper
import com.github.timofeevda.jstressy.utils.osgi.TcclSwitch.executeWithTCCLSwitch
import com.github.timofeevda.jstressy.vertx.metrics.StressyVertexMetricsOptions
import io.vertx.core.VertxOptions
import io.vertx.core.spi.VertxMetricsFactory
import io.vertx.reactivex.core.Vertx
import org.osgi.framework.BundleContext
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * OSGI-fied version of VertX provider. Uses "TCCL switch" pattern for registering metrics collector
 * through VertX SPI extensions
 */
class OSGIStressyVertxService(private val bundleContext: BundleContext, private val metricsRegistry: MetricsRegistry) : VertxService {
    override val vertx: Vertx
        get() {
            val options = VertxOptions()
                    .setWarningExceptionTime(getBlockedEventLoopThreadTimeout().toMilliseconds())
                    .setWarningExceptionTimeUnit(TimeUnit.MILLISECONDS)
                    .setMetricsOptions(
                            StressyVertexMetricsOptions()
                                    .setMetricsRegistry(metricsRegistry)
                                    .setFactory(SPIHelper.lookup(VertxMetricsFactory::class.java, bundleContext, "stressy")))
            return executeWithTCCLSwitch(Callable<Vertx> { Vertx.vertx(options) })
        }

}