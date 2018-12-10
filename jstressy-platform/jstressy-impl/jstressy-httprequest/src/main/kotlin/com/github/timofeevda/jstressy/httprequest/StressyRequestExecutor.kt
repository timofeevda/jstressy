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
import io.vertx.core.http.impl.HeadersAdaptor
import io.vertx.reactivex.core.MultiMap
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
internal class StressyRequestExecutor(httpClientService: HttpClientService,
                                      metricsRegistryService: MetricsRegistryService,
                                      httpSessionManagerService: HttpSessionManagerService) : RequestExecutor {

    companion object : LazyLogging()

    private val customHeaders = ConcurrentHashMap<String, String>()

    private val client: HttpClient = httpClientService.get()
    private val metricsRegistry: MetricsRegistry = metricsRegistryService.get()

    override val httpSessionManager: HttpSessionManager = httpSessionManagerService.get()

    override fun get(host: String, port: Int, requestURI: String): Single<HttpClientResponse> {
        return getMeasuredRequest(
                addCustomHeaders(
                        httpSessionManager.processRequest(client.request(HttpMethod.GET, port, host, requestURI))), null)
    }

    override fun post(host: String, port: Int, requestURI: String): Single<HttpClientResponse> {
        return createMeasuredRequest(host, port, requestURI, HttpMethod.POST)
    }

    override fun post(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse> {
        return createMeasuredJSONPayloadRequest(host, port, requestURI, HttpMethod.POST, data)
    }

    override fun postFormData(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse> {
        return createMeasuredFormDataRequest(host, port, requestURI, HttpMethod.POST, data)
    }

    override fun put(host: String, port: Int, requestURI: String): Single<HttpClientResponse> {
        return createMeasuredRequest(host, port, requestURI, HttpMethod.PUT)
    }

    override fun put(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse> {
        return createMeasuredJSONPayloadRequest(host, port, requestURI, HttpMethod.PUT, data)
    }

    override fun putFormData(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse> {
        return createMeasuredFormDataRequest(host, port, requestURI, HttpMethod.PUT, data)
    }

    override fun delete(host: String, port: Int, requestURI: String): Single<HttpClientResponse> {
        return createMeasuredRequest(host, port, requestURI, HttpMethod.DELETE)
    }

    override fun delete(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse> {
        return createMeasuredJSONPayloadRequest(host, port, requestURI, HttpMethod.DELETE, data)
    }

    override fun deleteFormData(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse> {
        return createMeasuredFormDataRequest(host, port, requestURI, HttpMethod.DELETE, data)
    }

    override fun websocket(host: String, port: Int, requestURI: String): Single<WebSocket> {
        val headersAdaptor = HeadersAdaptor(DefaultHttpHeaders())
        httpSessionManager.headers.forEach { header -> headersAdaptor.add(header.name, header.value) }
        customHeaders.forEach { name: String, value: String -> headersAdaptor.add(name, value) }
        return Single.create { emitter ->
            val requestTimer = RequestTimer("WebSocket Connection Setup")
            client.websocketStream(port, host, requestURI, MultiMap(headersAdaptor))
                    .toObservable()
                    .doOnSubscribe { requestTimer.start() }
                    .doOnNext { requestTimer.stop() }
                    .subscribe(
                            { emitter.onSuccess(it) },
                            { emitter.onError(it) })
        }
    }

    override fun invoke(request: HttpClientRequest): Single<HttpClientResponse> {
        return getMeasuredRequest(request, null)
    }

    override fun addCustomHeader(headerName: String, headerValue: String) {
        customHeaders[headerName] = headerValue
    }

    override fun removeCustomHeader(headerName: String) {
        customHeaders.remove(headerName)
    }

    private fun addCustomHeaders(request: HttpClientRequest): HttpClientRequest {
        customHeaders.forEach { name: String, value: String -> request.putHeader(name, value) }
        return request
    }

    private fun createMeasuredRequest(host: String, port: Int, requestURI: String, method: HttpMethod): Single<HttpClientResponse> {
        return getMeasuredRequest(
                addCustomHeaders(
                        httpSessionManager.processRequest(client.request(method, port, host, requestURI))), null)
    }

    private fun createMeasuredJSONPayloadRequest(host: String, port: Int, requestURI: String, method: HttpMethod, data: String): Single<HttpClientResponse> {
        return getMeasuredRequest(
                processJsonDataRequest(client.request(method, port, host, requestURI), data), data)
    }


    private fun createMeasuredFormDataRequest(host: String, port: Int, requestURI: String, method: HttpMethod, data: String): Single<HttpClientResponse> {
        return getMeasuredRequest(
                processFormDataRequest(client.request(method, port, host, requestURI), data), data)
    }

    private fun getMeasuredRequest(rq: HttpClientRequest, data: String?): Single<HttpClientResponse> {
        val rqUUID = UUID.randomUUID()
        val requestTimer = RequestTimer("request_" + rq.uri())
        return Single.create { emitter ->
            rq.toObservable()
                    .timeout(httpTimeout().toMilliseconds(), TimeUnit.MILLISECONDS)
                    .doOnSubscribe {
                        logger.debug { "Invoking request ${buildRequestDescription(rqUUID, rq)}" }
                        requestTimer.start()
                    }
                    .doOnNext { rp ->
                        logger.debug { "Processing response ${buildResponseDescription(rqUUID, rp)}" }
                        requestTimer.stop()
                        httpSessionManager.processResponse(rp)
                    }
                    .doOnError { e ->
                        logger.debug({ "Error invoking request ${buildRequestDescription(rqUUID, rq)}" }, e)
                    }
                    .subscribe(
                            { emitter.onSuccess(it) },
                            { emitter.onError(it) })
            if (data != null) {
                rq.write(data)
            }
            rq.end()
        }
    }

    private fun processFormDataRequest(request: HttpClientRequest, data: String): HttpClientRequest {
        return addCustomHeaders(httpSessionManager.processRequest(request))
                .putHeader("Content-Length", Integer.toString(data.toByteArray().size))
                .putHeader("Content-Type", "application/x-www-form-urlencoded")
    }

    private fun processJsonDataRequest(request: HttpClientRequest, jsonData: String): HttpClientRequest {
        return addCustomHeaders(httpSessionManager.processRequest(request))
                .putHeader("Content-Length", Integer.toString(jsonData.toByteArray().size))
                .putHeader("Content-Type", "application/json")
    }

    private inner class RequestTimer constructor(name: String) {
        private var context: Timer.Context? = null
        private val timer: Timer = metricsRegistry.timer(name)

        fun start() {
            context = timer.context()
        }

        fun stop() {
            context?.stop()
        }
    }

    private fun buildRequestDescription(uuid: UUID, rq: HttpClientRequest): String {
        return "id: $uuid uri: ${rq.uri()} headers: ${multiMapToString(rq.headers())}"
    }

    private fun buildResponseDescription(uuid: UUID, rs: HttpClientResponse): String {
        return "id: $uuid uri: ${rs.request().uri()} headers: ${multiMapToString(rs.headers())}"
    }

    private fun multiMapToString(multiMap: MultiMap): String {
        return multiMap.delegate.entries().joinToString(";") { "[${it.key} -> ${it.value}]" }
    }
}
