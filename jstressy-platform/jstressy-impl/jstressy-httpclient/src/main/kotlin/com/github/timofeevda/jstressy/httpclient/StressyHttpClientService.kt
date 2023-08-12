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

package com.github.timofeevda.jstressy.httpclient

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httpclient.HttpClientService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import io.vertx.core.http.HttpClientOptions
import io.vertx.reactivex.core.http.HttpClient

/**
 * Basic implementation of HttpClientService. Creates [HttpClient] configured based on JStressy configuration
 *
 * @author timofeevda
 */
open class StressyHttpClientService(
    vertxService: VertxService,
    configurationService: ConfigurationService
) : HttpClientService {

    private val client: HttpClient

    init {
        val globals = configurationService.configuration.globals
        this.client = vertxService.vertx.createHttpClient(
            HttpClientOptions()
                .setSsl(globals.useSsl)
                .setTrustAll(globals.insecureSsl)
                .setMaxPoolSize(globals.maxConnections)
                .setTryUsePerMessageWebSocketCompression(globals.webSocketPerMessageDeflate)
                .setWebSocketCompressionLevel(globals.webSocketCompressionLevel)
                .setMaxWebSockets(globals.maxWebSockets)
                .setMaxWebSocketFrameSize(globals.maxWebSocketFrameSize)
                .setMaxWebSocketMessageSize(globals.maxWebSocketMessageSize)
                .setKeepAlive(globals.connectionKeepAlive)
                .setLogActivity(globals.logNetworkActivity)
        )
    }

    override fun get(): HttpClient {
        return client
    }
}
