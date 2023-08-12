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

package com.github.timofeevda.jstressy.config.parameters

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.timofeevda.jstressy.api.config.parameters.StressyGlobals

/**
 * Global Stressy configuration
 *
 * @author timofeevda
 */
@JsonPropertyOrder("host", "port", "stressyMetricsPort", "stressyMetricsPath", "useSsl", "insecureSsl",
    "maxConnections", "maxWebSockets", "maxWebSocketFrameSize", "maxWebSocketMessageSize", "webSocketPerMessageDeflate",
    "webSocketCompressionLevel", "connectionKeepAlive", "logNetworkActivity")
class Globals : StressyGlobals {
    /**
     * Port of the host being tested
     */
    override val port: Int = 80
    /**
     * Host being tested
     */
    override val host: String = "localhost"
    /**
     * Port which is exposed by Stressy to provide application metrics
     */
    override val stressyMetricsPort: Int? = null
    /**
     * Path in URL which is used by Stressy to provide application metrics
     */
    override val stressyMetricsPath: String? = null
    /**
     * Turns SSL on/off
     */
    override val useSsl = false
    /**
     * Turns insecure SSL on/off
     */
    override val insecureSsl = false
    /**
     * Number of max concurrent connection generated by Stressy
     */
    override val maxConnections = 1000
    /**
     * WebSocket per message deflated is turned on
     */
    override val webSocketPerMessageDeflate: Boolean = true
    /**
     * Default ZIP compression level
     */
    override val webSocketCompressionLevel: Int = 6

    /**
     * Max number of websocket connections
     */
    override val maxWebSockets = 1000

    /**
     * Max size of the websocket message frame
     */
    override val maxWebSocketFrameSize = 65536

    /**
     * Max size of the websocket message
     */
    override val maxWebSocketMessageSize = this.maxWebSocketFrameSize * 4

    /**
     * Turn HTTP client connection pooling on/off
     */
    override val connectionKeepAlive: Boolean = true

    /**
     * Turn network activity logging in HTTP client on/off
     */
    override val logNetworkActivity: Boolean = false

}
