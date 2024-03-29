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

package com.github.timofeevda.jstressy.httprequest

import com.github.timofeevda.jstressy.api.httpclient.HttpClientService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManager
import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManagerService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.metrics.type.Timer
import com.github.timofeevda.jstressy.utils.StressyUtils.httpTimeout
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.reactivex.Single
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.WebSocketConnectOptions
import io.vertx.core.http.impl.headers.HeadersAdaptor
import io.vertx.reactivex.core.http.HttpClient
import io.vertx.reactivex.core.http.HttpClientRequest
import io.vertx.reactivex.core.http.HttpClientResponse
import io.vertx.reactivex.core.http.WebSocket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Basic implementation of [RequestExecutor]
 *
 * @author timofeevda
 */
open class StressyRequestExecutor(
    httpClientService: HttpClientService,
    metricsRegistryService: MetricsRegistryService,
    httpSessionManagerService: HttpSessionManagerService
) : RequestExecutor {

    companion object : LazyLogging()

    private val customHeaders = ConcurrentHashMap<String, String>()

    private val client: HttpClient = httpClientService.get()
    private val metricsRegistry: MetricsRegistry = metricsRegistryService.get()

    override val httpSessionManager: HttpSessionManager = httpSessionManagerService.get()

    override fun get(
        host: String,
        port: Int,
        requestURI: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return getMeasuredRequest(
            addCustomHeaders(
                httpSessionManager.processRequest(client.rxRequest(HttpMethod.GET, port, host, requestURI))
            ).adjustRequest(requestAdjustment), null
        )
    }

    override fun post(
        host: String,
        port: Int,
        requestURI: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredRequest(host, port, requestURI, HttpMethod.POST, requestAdjustment)
    }

    override fun post(
        host: String,
        port: Int,
        requestURI: String,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredJSONPayloadRequest(host, port, requestURI, HttpMethod.POST, data, requestAdjustment)
    }

    override fun postFormData(
        host: String,
        port: Int,
        requestURI: String,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredFormDataRequest(host, port, requestURI, HttpMethod.POST, data, requestAdjustment)
    }

    override fun put(
        host: String,
        port: Int,
        requestURI: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredRequest(host, port, requestURI, HttpMethod.PUT, requestAdjustment)
    }

    override fun put(
        host: String,
        port: Int,
        requestURI: String,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredJSONPayloadRequest(host, port, requestURI, HttpMethod.PUT, data, requestAdjustment)
    }

    override fun putFormData(
        host: String,
        port: Int,
        requestURI: String,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredFormDataRequest(host, port, requestURI, HttpMethod.PUT, data, requestAdjustment)
    }

    override fun delete(
        host: String,
        port: Int,
        requestURI: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredRequest(host, port, requestURI, HttpMethod.DELETE, requestAdjustment)
    }

    override fun delete(
        host: String,
        port: Int,
        requestURI: String,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredJSONPayloadRequest(host, port, requestURI, HttpMethod.DELETE, data, requestAdjustment)
    }

    override fun deleteFormData(
        host: String,
        port: Int,
        requestURI: String,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return createMeasuredFormDataRequest(host, port, requestURI, HttpMethod.DELETE, data, requestAdjustment)
    }

    override fun websocket(host: String, port: Int, requestURI: String): Single<WebSocket> {
        val headersAdaptor = HeadersAdaptor(DefaultHttpHeaders())
        httpSessionManager.headers.forEach { header -> headersAdaptor.add(header.name, header.value) }
        customHeaders.forEach { (name: String, value: String) -> headersAdaptor.add(name, value) }
        return Single.create { emitter ->
            val requestTimer = WSRequestTimer()

            val connectOptions =
                WebSocketConnectOptions().setHost(host).setPort(port).setURI(requestURI).setHeaders(headersAdaptor)

            client.rxWebSocket(connectOptions)
                .doOnSubscribe { requestTimer.start() }
                .doFinally { requestTimer.stop() }
                .subscribe(
                    { emitter.onSuccess(it) },
                    { emitter.onError(it) })
        }
    }

    override fun invoke(request: Single<HttpClientRequest>): Single<HttpClientResponse> {
        return getMeasuredRequest(request, null)
    }

    override fun addCustomHeader(headerName: String, headerValue: String) {
        customHeaders[headerName] = headerValue
    }

    override fun removeCustomHeader(headerName: String) {
        customHeaders.remove(headerName)
    }

    private fun addCustomHeaders(request: Single<HttpClientRequest>): Single<HttpClientRequest> {
        return request.map { r ->
            customHeaders.forEach { (name: String, value: String) -> r.putHeader(name, value) }
            r
        }
    }

    private fun createMeasuredRequest(
        host: String,
        port: Int,
        requestURI: String,
        method: HttpMethod,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return getMeasuredRequest(
            addCustomHeaders(
                httpSessionManager.processRequest(client.rxRequest(method, port, host, requestURI))
            ).adjustRequest(requestAdjustment), null
        )
    }

    private fun createMeasuredJSONPayloadRequest(
        host: String,
        port: Int,
        requestURI: String,
        method: HttpMethod,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return getMeasuredRequest(
            processJsonDataRequest(client.rxRequest(method, port, host, requestURI), data, requestAdjustment), data
        )
    }

    private fun createMeasuredFormDataRequest(
        host: String,
        port: Int,
        requestURI: String,
        method: HttpMethod,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientResponse> {
        return getMeasuredRequest(
            processFormDataRequest(client.rxRequest(method, port, host, requestURI), data, requestAdjustment), data
        )
    }

    private fun getMeasuredRequest(rq: Single<HttpClientRequest>, data: String?): Single<HttpClientResponse> {
        val rqUUID = UUID.randomUUID()
        return rq.flatMap { r ->
            val preparedRequest = prepareRequest(r, rqUUID)
            preparedRequest.rxConnect().timeout(httpTimeout().toMilliseconds(), TimeUnit.MILLISECONDS)
                .doOnSubscribe {
                    if (data != null) {
                        preparedRequest.write(data)
                    }
                    preparedRequest.end()
                    logger.debug({ "Invoking request" }, *requestDescriptionParameters(rqUUID, preparedRequest))
                }
                .doOnSuccess { rp ->
                    logger.debug({ "Processing response" }, *responseDescriptionParameters(rqUUID, rp))
                    httpSessionManager.processResponse(rp)
                }
                .doOnError { e ->
                    logger.debug(
                        { "Error invoking request" },
                        *requestDescriptionParameters(rqUUID, preparedRequest),
                        e
                    )
                }
        }
    }

    /**
     * This method can be used for request customization. For example, adding automatically generated UUID to
     * HTTP request headers for traceability or any other arbitrary action with the original request
     *
     * @param request original request
     * @param uuid UUID generated for the provided request
     *
     * @return customized request that will be used further in request executor
     */
    open fun prepareRequest(request: HttpClientRequest, uuid: UUID): HttpClientRequest {
        return request
    }

    private fun processFormDataRequest(
        request: Single<HttpClientRequest>,
        data: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientRequest> {
        return addCustomHeaders(httpSessionManager.processRequest(request))
            .map { r ->
                r.putHeader("Content-Length", data.toByteArray().size.toString())
                r.putHeader("Content-Type", "application/x-www-form-urlencoded")
            }.adjustRequest(requestAdjustment)
    }

    private fun processJsonDataRequest(
        request: Single<HttpClientRequest>,
        jsonData: String,
        requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?
    ): Single<HttpClientRequest> {
        return addCustomHeaders(httpSessionManager.processRequest(request))
            .map { r ->
                r.putHeader("Content-Length", jsonData.toByteArray().size.toString())
                r.putHeader("Content-Type", "application/json")
            }.adjustRequest(requestAdjustment)
    }

    private inner class WSRequestTimer {
        private var context: Timer.Context? = null
        private val timer: Timer = metricsRegistry.timer(
            "stressy.request.executor.websocket.setup", "Time to establish websocket connection"
        )

        fun start() {
            context = timer.context()
        }

        fun stop() {
            context?.stop()
        }
    }

    private fun multiMapToString(multiMap: io.vertx.reactivex.core.MultiMap): String {
        return multiMap.delegate.entries().joinToString(";") { "[${it.key} -> ${it.value}]" }
    }

    private fun requestDescriptionParameters(uuid: UUID, rq: HttpClientRequest) = arrayOf(
        "id", uuid.toString(),
        "uri", rq.uri,
        "method", rq.method.name(),
        "requestHeaders", multiMapToString(rq.headers())
    )

    private fun responseDescriptionParameters(uuid: UUID, rs: HttpClientResponse) = arrayOf(
        *requestDescriptionParameters(uuid, rs.request()),
        "statusCode", rs.statusCode().toString(),
        "responseHeaders", multiMapToString(rs.headers())
    )

    private fun Single<HttpClientRequest>.adjustRequest(requestAdjustment: ((request: HttpClientRequest) -> HttpClientRequest)?) =
        map {
            requestAdjustment?.invoke(it)
                ?: it
        }
}
