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

package com.github.timofeevda.jstressy.dummy.scenario;

import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.scenario.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Example implementation of scenario. Just tries to GET data from https://google.com/
 */
public class TouchGoogleScenario implements Scenario {

    private static final Logger logger = LoggerFactory.getLogger(TouchGoogleScenario.class);

    private final MetricsRegistry metricsRegistry;
    private final RequestExecutor requestExecutor;
    private final String host;
    private final int port;

    TouchGoogleScenario(MetricsRegistry metricsRegistry,
                        RequestExecutor requestExecutor,
                        ConfigurationService configurationService) {
        this.metricsRegistry = metricsRegistry;
        this.requestExecutor = requestExecutor;

        this.host = configurationService.getConfiguration().getGlobals().getHost();
        this.port = configurationService.getConfiguration().getGlobals().getPort();
    }

    @Override
    public void start() {
        requestExecutor.get(host, port, "/")
                .doOnSuccess(httpClientResponse -> metricsRegistry.counter("googl_request_success").inc())
                .subscribe(
                        httpClientResponse -> logger.info("Host {} answered with code {}", host, httpClientResponse.statusCode())
                );
    }

    @Override
    public void stop() {

    }

    @Override
    public Scenario withParameters(Map<String, String> parameters) {
        return this;
    }
}
