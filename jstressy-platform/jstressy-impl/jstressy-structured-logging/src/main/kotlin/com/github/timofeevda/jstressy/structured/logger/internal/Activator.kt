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

package com.github.timofeevda.jstressy.structured.logger.internal

import com.github.structlog4j.SLoggerFactory
import com.github.structlog4j.StructLog4J
import com.github.structlog4j.json.JsonFormatter
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

/**
 * Activator for setting up structured logging
 *
 * @author timofeevda
 */
class Activator : BundleActivator {

    companion object {
        private val logger = SLoggerFactory.getLogger(Activator::class.java)
    }

    override fun start(bundleContext: BundleContext) {
        logger.info("Registering JSON log formatting")
        StructLog4J.setFormatter(JsonFormatter.getInstance())
        logger.info("Starting logger activator")
    }

    override fun stop(bundleContext: BundleContext) {
        logger.info("Stopping logger activator")
    }
}
