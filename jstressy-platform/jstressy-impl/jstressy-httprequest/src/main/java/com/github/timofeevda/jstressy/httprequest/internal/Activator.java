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

package com.github.timofeevda.jstressy.httprequest.internal;

import com.github.timofeevda.jstressy.api.httpclient.HttpClientService;
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService;
import com.github.timofeevda.jstressy.httprequest.StressyRequestExecutorService;
import com.github.timofeevda.jstressy.utils.ServiceObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class Activator implements BundleActivator {

    private static Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) {
        logger.info("Starting request executor service activator");

        Single<HttpClientService> vertxService = ServiceObserver.observeService(HttpClientService.class.getName(), context);
        Single<MetricsRegistryService> configurationService = ServiceObserver.observeService(MetricsRegistryService.class.getName(), context);

        Observable.combineLatest(
                vertxService.toObservable(),
                configurationService.toObservable(),
                StressyRequestExecutorService::new).subscribe(requestExecutorService -> {
            logger.info("Registering request executor service");
            context.registerService(RequestExecutorService.class.getName(), requestExecutorService, new Hashtable());
        });

    }

    @Override
    public void stop(BundleContext context) {

    }
}
