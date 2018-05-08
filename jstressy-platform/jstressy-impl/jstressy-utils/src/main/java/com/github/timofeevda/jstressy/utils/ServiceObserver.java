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

package com.github.timofeevda.jstressy.utils;

import io.reactivex.Single;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides observable object for getting reference to the registered service. Doesn't tracks service events, just
 * gets the first version of registered service
 *
 * @author timofeevda
 */
public class ServiceObserver {

    private static final Logger logger = LoggerFactory.getLogger(ServiceObserver.class.getName());

    @SuppressWarnings("unchecked")
    public static <T> Single<T> observeService(String className, BundleContext bundleContext) {
        return Single.create(singleEmitter -> {
            ServiceListener serviceListener = event -> {
                ServiceReference ref = event.getServiceReference();
                if (event.getType() == ServiceEvent.REGISTERED) {
                    logger.info("Getting service {}", bundleContext.getService(ref));
                    singleEmitter.onSuccess((T) bundleContext.getService(ref));
                }
            };
            bundleContext.addServiceListener(serviceListener, "(objectClass=" + className + ")");
            synchronized (singleEmitter) {
                ServiceReference ref = bundleContext.getServiceReference(className);
                if (ref != null) {
                    logger.info("Getting service {}", bundleContext.getService(ref));
                    singleEmitter.onSuccess((T) bundleContext.getService(ref));
                }
            }
            singleEmitter.setCancellable(() -> bundleContext.removeServiceListener(serviceListener));
        });
    }
}
