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
import io.vertx.core.http.WebSocket
import io.vertx.core.net.SocketAddress
import io.vertx.core.spi.metrics.ClientMetrics
import io.vertx.core.spi.metrics.HttpClientMetrics
import io.vertx.core.spi.observability.HttpRequest
import io.vertx.core.spi.observability.HttpResponse
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

const val METRIC_PREFIX = "vertx.http.client"

private val bytesReceived: AtomicLong = AtomicLong()
private val bytesSent: AtomicLong = AtomicLong()
private val requests: AtomicLong = AtomicLong()
private val requestResets: AtomicLong = AtomicLong()
private val requestCount: AtomicLong = AtomicLong()
private val queueSize: AtomicLong = AtomicLong()
private val connections: AtomicLong = AtomicLong()
private val openWebSockets: AtomicLong = AtomicLong()

/**
 * Custom implementation of Vert.X HTTP client metrics which can be used for more fine-grained control over
 * metrics provided by HTTP client
 */
class StressyHTTPClientMetrics(private val metricsRegistry: MetricsRegistry) :
    HttpClientMetrics<HTTPRequestMetric, WebSocketRequestMetric, HTTPSocketMetric, Timer.Context> {

    override fun createEndpointMetrics(
        remoteAddress: SocketAddress?,
        maxPoolSize: Int
    ): ClientMetrics<HTTPRequestMetric, Timer.Context, HttpRequest, HttpResponse> {
        metricsRegistry.gauge("${METRIC_PREFIX}.connections",
            "Number of connections to the remote host currently opened", { connections.toDouble() })

        metricsRegistry.gauge("${METRIC_PREFIX}.ws.connections",
            "Number of websockets currently opened", { openWebSockets.toDouble() })

        metricsRegistry.gauge("${METRIC_PREFIX}.queue.size",
            "Number of pending requests in queue", { queueSize.toDouble() })

        metricsRegistry.gauge("${METRIC_PREFIX}.bytes.received",
            "Number of bytes received from the remote host", { bytesReceived.toDouble() })

        metricsRegistry.gauge("${METRIC_PREFIX}.bytes.sent",
            "Number of bytes sent to the remote host", { bytesSent.toDouble() })

        metricsRegistry.gauge("${METRIC_PREFIX}.requests",
            "Number of requests waiting for the response", { requests.toDouble() })

        metricsRegistry.gauge("${METRIC_PREFIX}.requests",
            "Number of requests sent", { requestCount.toDouble() })

        return HTTPEndpointMetrics(metricsRegistry)
    }

    override fun disconnected(webSocketMetric: WebSocketRequestMetric?) {
        if (webSocketMetric != null) {
            openWebSockets.decrementAndGet()
        }
    }

    override fun connected(webSocket: WebSocket?): WebSocketRequestMetric {
        if (webSocket != null) {
            openWebSockets.incrementAndGet()
        }
        return WebSocketRequestMetric()
    }

    override fun disconnected(socketMetric: HTTPSocketMetric?, remoteAddress: SocketAddress?) {
        if (socketMetric != null) {
            connections.decrementAndGet()
        }
    }

    override fun connected(remoteAddress: SocketAddress?, remoteName: String?): HTTPSocketMetric {
        connections.incrementAndGet()
        return HTTPSocketMetric()
    }

    override fun bytesRead(socketMetric: HTTPSocketMetric?, remoteAddress: SocketAddress?, numberOfBytes: Long) {
        bytesReceived.addAndGet(numberOfBytes)
    }

    override fun bytesWritten(socketMetric: HTTPSocketMetric?, remoteAddress: SocketAddress?, numberOfBytes: Long) {
        bytesSent.addAndGet(numberOfBytes)
    }

    override fun exceptionOccurred(socketMetric: HTTPSocketMetric?, remoteAddress: SocketAddress?, t: Throwable?) {
        if (remoteAddress != null) {
            metricsRegistry.counter(
                "${METRIC_PREFIX}.errors",
                "Number of errors occurred for the connections to the remote host"
            ).inc()
        }
    }

}

class WebSocketRequestMetric
class HTTPRequestMetric {
    val requestStart = System.nanoTime()
    var requestEnd: Long = 0
    var response: HttpResponse? = null
}

class HTTPSocketMetric

class HTTPEndpointMetrics(
    private val metricsRegistry: MetricsRegistry
) : ClientMetrics<HTTPRequestMetric, Timer.Context, HttpRequest, HttpResponse> {

    override fun enqueueRequest(): Timer.Context {
        queueSize.incrementAndGet()
        return metricsRegistry.timer("${METRIC_PREFIX}.queue.delay", "Time spent in queue before being processed")
            .context()
    }

    override fun dequeueRequest(timerContext: Timer.Context?) {
        timerContext?.stop()
        queueSize.decrementAndGet()
    }

    override fun requestBegin(uri: String?, request: HttpRequest?): HTTPRequestMetric? {
        return if (request != null) {
            requests.incrementAndGet()
            requestCount.incrementAndGet()
            HTTPRequestMetric()
        } else {
            null
        }
    }

    override fun requestEnd(requestMetric: HTTPRequestMetric?, bytesWritten: Long) {
        requests.decrementAndGet()
        requestMetric?.requestEnd = System.nanoTime()
    }

    override fun responseBegin(requestMetric: HTTPRequestMetric?, response: HttpResponse?) {
        if (requestMetric != null) {
            val waitTime = System.nanoTime() - requestMetric.requestEnd
            metricsRegistry.timer("${METRIC_PREFIX}.ttfb", "Time till first byte received for response")
                .record(waitTime, TimeUnit.NANOSECONDS)
            requestMetric.response = response
        }
    }

    override fun requestReset(requestMetric: HTTPRequestMetric?) {
        if (requestMetric != null) {
            requests.decrementAndGet()
            requestResets.incrementAndGet()
            metricsRegistry.counter("${METRIC_PREFIX}.resets", "Number of request resets").inc()
        }
    }

    override fun responseEnd(requestMetric: HTTPRequestMetric?) {
        if (requestMetric != null) {
            val waitTime = System.nanoTime() - requestMetric.requestStart
            metricsRegistry.timer("${METRIC_PREFIX}.responseTime", "Response time")
                .record(waitTime, TimeUnit.NANOSECONDS)
            metricsRegistry.counter("${METRIC_PREFIX}.responseCount", "Response count").inc()
        }
    }

    override fun responseEnd(requestMetric: HTTPRequestMetric?, bytesRead: Long) {
        return responseEnd(requestMetric)
    }

}
