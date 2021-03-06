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

package com.github.timofeevda.jstressy.api.config.parameters

/**
 * Global parameters
 *
 * @author timofeevda
 */
interface StressyGlobals {
    /**
     * Target system's port
     *
     * @return target system's port
     */
    val port: Int

    /**
     * Target system's host
     *
     * @return target system's host
     */
    val host: String

    /**
     * Port which is used for exposing JStressy metrics
     *
     * @return port for exposing JStressy metrics
     */
    val stressyMetricsPort: Int?

    /**
     * Path which is used for exposing JStressy metrics
     *
     * @return path for exposing JStressy metrics
     */
    val stressyMetricsPath: String?

    /**
     * Denotes if SSL is switched on/off
     *
     * @return true if SSL is switched on, otherwise false
     */
    val useSsl: Boolean

    /**
     * Denotes if invalid SSL certificates are skipped
     *
     * @return true if invalid SSL certificated are skipped, otherwise false
     */
    val insecureSsl: Boolean

    /**
     * Max number of connections
     *
     * @return max number of connections
     */
    val maxConnections: Int

    /**
     * Denotes if WebSocket per message deflate should be turned off/on. Default is "true"
     *
     * @return true if WebSocket per message deflate should be turned on, otherwise false
     */
    val webSocketPerMessageDeflate: Boolean

    /**
     * WebSocket compression level. 0-9 levels are supported. Default is 6
     *
     * @return WebSocket compression level for standard zip algorithm
     */
    val webSocketCompressionLevel: Int

}
