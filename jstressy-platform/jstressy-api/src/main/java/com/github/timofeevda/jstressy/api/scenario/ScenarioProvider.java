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

package com.github.timofeevda.jstressy.api.scenario;

import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor;
import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.vertx.VertxService;

/**
 * Provides {@link Scenario} instance. It can provide new instance on each invocation or cached one if applicable
 *
 * @author timofeevda
 */
public interface ScenarioProvider {

    /**
     * Returns {@link Scenario} instance
     *
     * @return {@link Scenario} instance
     */
    Scenario get();

    /**
     * Initializes scenario provider. Passed value can be stored for passing to scenario instances and used for
     * long-running initializition logic (e.g. reading list of available users from file)
     *
     * @param metricsRegistry      metrics registry
     * @param requestExecutor      request executor
     * @param configurationService configuration service
     * @param vertxService         VertX service
     */
    void init(MetricsRegistry metricsRegistry,
              RequestExecutor requestExecutor,
              ConfigurationService configurationService,
              VertxService vertxService);

}
