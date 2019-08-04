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

package com.github.timofeevda.jstressy.vertx.metrics

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.type.Timer
import com.github.timofeevda.jstressy.vertx.metrics.http.HttpEndpointMetric
import com.github.timofeevda.jstressy.vertx.metrics.http.HttpRequestMetric
import com.github.timofeevda.jstressy.vertx.metrics.http.WebSocketRequestMetric
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.WebSocket
import io.vertx.core.net.SocketAddress
import io.vertx.core.spi.metrics.HttpClientMetrics
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Function
import java.util.function.Supplier

const val METRIC_PREFIX = "vertx.metrics.http.client."
private const val METRIC_PREFIX_RECEIVED = "${METRIC_PREFIX}bytes.received"
private const val METRIC_PREFIX_SENT = "${METRIC_PREFIX}bytes.sent"

private val bytesReceived: AtomicLong = AtomicLong()
private val bytesSent: AtomicLong = AtomicLong()

private val queueSize = ConcurrentHashMap<String, AtomicInteger>()
private val openSockets = ConcurrentHashMap<String, AtomicInteger>()
private val exceptions = ConcurrentHashMap<String, AtomicInteger>()
private val httpRequestStatuses = ConcurrentHashMap<String, AtomicInteger>()

private val openWebSockets: AtomicLong = AtomicLong()

class StressyHTTPClientMetrics(private val metricsRegistry: MetricsRegistry) : HttpClientMetrics<HttpRequestMetric, WebSocketRequestMetric, String, HttpEndpointMetric, Timer> {

    init {
        metricsRegistry.gauge(METRIC_PREFIX_RECEIVED, Supplier { bytesReceived.toDouble() })
        metricsRegistry.gauge(METRIC_PREFIX_SENT, Supplier { bytesSent.toDouble() })
    }

    override fun disconnected(webSocketMetric: WebSocketRequestMetric?) {
        if (webSocketMetric?.webSocket != null) {
            openWebSockets.decrementAndGet()
        }
    }

    override fun connected(endpointMetric: HttpEndpointMetric?, socketMetric: String?, webSocket: WebSocket?): WebSocketRequestMetric {
        if (webSocket != null) {
            openWebSockets.incrementAndGet()
        }
        return WebSocketRequestMetric(webSocket)
    }

    override fun disconnected(socketMetric: String?, remoteAddress: SocketAddress?) {
        if (socketMetric != null) {
            openSockets.getOrDefault("${socketMetric}_open_sockets", AtomicInteger(0)).decrementAndGet()
        }
    }


    override fun connected(remoteAddress: SocketAddress?, remoteName: String?): String {
        if (remoteAddress != null) {
            val endpointName = "$remoteName:${remoteAddress.port()}"
            openSockets.getOrDefault("${endpointName}_open_sockets", AtomicInteger(0)).incrementAndGet()
            return endpointName
        }
        return ""
    }

    override fun createEndpoint(host: String, port: Int, maxPoolSize: Int): HttpEndpointMetric {
        val endpointName = "$host:$port"
        metricsRegistry.gauge("$METRIC_PREFIX${endpointName}_open_sockets", openSockets, Function { openSockets.getOrDefault(endpointName, AtomicInteger(0)).toDouble() })
        metricsRegistry.gauge("${METRIC_PREFIX}_open_websockets", openWebSockets, Function { openWebSockets.toDouble() })
        metricsRegistry.gauge("$METRIC_PREFIX${endpointName}_queue_size", queueSize, Function { queueSize.getOrDefault(endpointName, AtomicInteger(0)).toDouble() })
        return HttpEndpointMetric(endpointName, metricsRegistry)
    }

    override fun endpointConnected(endpointMetric: HttpEndpointMetric?, socketMetric: String?) {
        if (endpointMetric != null) {
            openSockets.computeIfAbsent(endpointMetric.endpointName) { AtomicInteger(0) }.incrementAndGet()
        }
    }

    override fun endpointDisconnected(endpointMetric: HttpEndpointMetric?, socketMetric: String?) {
        if (endpointMetric != null) {
            openSockets[endpointMetric.endpointName]?.decrementAndGet()
        }
    }

    override fun enqueueRequest(endpointMetric: HttpEndpointMetric?): Timer? {
        return if (endpointMetric != null) {
            queueSize.getOrDefault(endpointMetric.endpointName, AtomicInteger(0)).incrementAndGet()
            endpointMetric.queueDelay
        } else {
            null
        }
    }

    override fun dequeueRequest(endpointMetric: HttpEndpointMetric?, taskMetric: Timer?) {
        if (endpointMetric != null) {
            taskMetric?.context()?.stop()
            queueSize.getOrDefault(endpointMetric.endpointName, AtomicInteger(0)).decrementAndGet()
        }
    }

    override fun requestBegin(endpointMetric: HttpEndpointMetric?, socketMetric: String?, localAddress: SocketAddress?, remoteAddress: SocketAddress?, request: HttpClientRequest?): HttpRequestMetric? {
        return if (endpointMetric != null && request != null) {
            HttpRequestMetric(endpointMetric, request.uri(), request.method())
        } else {
            null
        }
    }

    override fun requestEnd(requestMetric: HttpRequestMetric?) {
        requestMetric?.requestEnd = System.nanoTime()
    }

    override fun responseBegin(requestMetric: HttpRequestMetric?, response: HttpClientResponse?) {
        if (requestMetric != null && response != null) {
            val waitTime = System.nanoTime() - requestMetric.requestEnd
            requestMetric.endpointMetric.ttfb.record(waitTime, TimeUnit.NANOSECONDS)
        }
    }

    override fun responseEnd(requestMetric: HttpRequestMetric?, response: HttpClientResponse?) {
        if (requestMetric != null && response != null) {
            reportRequestEnd(requestMetric, response.statusCode())
        }
    }

    override fun requestReset(requestMetric: HttpRequestMetric?) {
        reportRequestEnd(requestMetric, -1)
    }

    override fun responsePushed(endpointMetric: HttpEndpointMetric?, socketMetric: String?, localAddress: SocketAddress?, remoteAddress: SocketAddress?, request: HttpClientRequest?): HttpRequestMetric? {
        return requestBegin(endpointMetric, socketMetric, localAddress, remoteAddress, request)
    }

    override fun bytesRead(socketMetric: String?, remoteAddress: SocketAddress?, numberOfBytes: Long) {
        bytesReceived.addAndGet(numberOfBytes)
    }

    override fun bytesWritten(socketMetric: String?, remoteAddress: SocketAddress?, numberOfBytes: Long) {
        bytesSent.addAndGet(numberOfBytes)
    }

    override fun exceptionOccurred(socketMetric: String?, remoteAddress: SocketAddress?, t: Throwable?) {
        if (remoteAddress != null) {
            exceptions.getOrDefault("${remoteAddress.host()}:${remoteAddress.port()}", AtomicInteger(0)).incrementAndGet()
        }
    }

    private fun reportRequestEnd(requestMetric: HttpRequestMetric?, statusCode: Int) {
        if (requestMetric != null) {
            val statusCounterName = "$METRIC_PREFIX${requestMetric.endpointMetric.endpointName}_status_${statusCodeToStatusRange(statusCode)}"
            if (httpRequestStatuses.computeIfAbsent(statusCounterName) { AtomicInteger(0) }.incrementAndGet() != 0) {
                metricsRegistry.gauge(statusCounterName,
                        httpRequestStatuses,
                        Function { httpRequestStatuses.getOrDefault(statusCounterName, AtomicInteger(0)).toDouble() })
            }
        }
    }

    private fun statusCodeToStatusRange(statusCode: Int): String {
        return "${statusCode / 100}xx"
    }
}