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

package com.github.timofeevda.jstressy.metrics;

import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService;
import com.github.timofeevda.jstressy.api.vertx.VertxService;
import io.vertx.reactivex.ext.web.Router;

import java.io.IOException;

/**
 * Basic implementation of {@link MetricsRegistryService}. Publishes metrics in Prometheus format getting
 * endpoint configuration from JStressy configuration service
 *
 * @author timofeevda
 */
public class StressyMetricsRegistryService implements MetricsRegistryService {

    private final StressyMetricsRegistry metricsRegistry;

    public StressyMetricsRegistryService() {
        this.metricsRegistry = new StressyMetricsRegistry();
    }

    @Override
    public MetricsRegistry get() {
        return metricsRegistry;
    }

    public void publishMetrics(VertxService vertxService,
                               ConfigurationService configurationService) {
        StressyMetricsPublisher stressyMetricsPublisher = new StressyMetricsPublisher(metricsRegistry);

        Router router = Router.router(vertxService.getVertx());
        String metricsPath = configurationService.getConfiguration().getGlobals().getStressyMetricsPath();
        router.get(metricsPath).handler(event -> {
            try {
                stressyMetricsPublisher.publish(event.request(), event.response());
            } catch (IOException e) {
                event.fail(e);
            }
        });

        int port = configurationService.getConfiguration().getGlobals().getStressyMetricsPort();
        vertxService.getVertx().createHttpServer().requestHandler(router::accept).listen(port);
    }
}
