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
package com.github.timofeevda.jstressy.dummy.scenario

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util

import io.vertx.reactivex.core.http.HttpClientResponse

/**
  * Example implementation of scenario. Just tries to GET data from https://google.com/
  */
object TouchGoogleScenario {
  private val logger = LoggerFactory.getLogger(classOf[TouchGoogleScenario])
}

class TouchGoogleScenario private[scenario](val metricsRegistry: MetricsRegistry, val requestExecutor: RequestExecutor, val configurationService: ConfigurationService) extends Scenario {
  this.host = configurationService.getConfiguration.getGlobals.getHost
  this.port = configurationService.getConfiguration.getGlobals.getPort
  final private var host = ""
  final private var port = 0

  override def start(): Unit = requestExecutor.get(host, port, "/").doOnSuccess((httpClientResponse: HttpClientResponse) => metricsRegistry.counter("googl_request_success").inc())
    .subscribe((httpClientResponse: HttpClientResponse) => TouchGoogleScenario.logger.info("Host {} answered with code {}", host, httpClientResponse.statusCode))

  override def stop(): Unit = {
  }

  override def withParameters(parameters: util.Map[String, String]): Scenario = this

  override def withArrivalInterval(intervalId: String): Scenario = this
}