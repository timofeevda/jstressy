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

package com.github.timofeevda.jstressy.api.httprequest

import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManager
import io.reactivex.Single
import io.vertx.reactivex.core.http.HttpClientRequest
import io.vertx.reactivex.core.http.HttpClientResponse
import io.vertx.reactivex.core.http.WebSocket

/**
 * Request executor. Proxy object hiding concrete request/response handling functionality.
 *
 * Example interface (contains most frequently used HTTP methods). Implementors may choose to use different way
 * of creating handler for various HTTP methods (e.g. providing builders) and different interface
 *
 * @author timofeevda
 */
interface RequestExecutor {

    /**
     * Returns [HttpSessionManager] instance assigned to request executor
     * @return [HttpSessionManager] instance which manages requests in this request executor
     */
    val httpSessionManager: HttpSessionManager

    /**
     * Invokes GET method
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun get(host: String, port: Int, requestURI: String): Single<HttpClientResponse>

    /**
     * Invokes POST method
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun post(host: String, port: Int, requestURI: String): Single<HttpClientResponse>

    /**
     * Invokes POST method with payload as json
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @param data       json payload
     * @return [HttpClientResponse] response
     */
    fun post(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse>

    /**
     * Invokes POST method with payload as form data with "application/x-www-form-urlencoded" content type
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun postFormData(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse>

    /**
     * Invokes PUT method
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun put(host: String, port: Int, requestURI: String): Single<HttpClientResponse>

    /**
     * Invokes PUT method with payload as json
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @param data       json payload
     * @return [HttpClientResponse] response
     */
    fun put(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse>

    /**
     * Invokes PUT method with payload as form data with "application/x-www-form-urlencoded" content type
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun putFormData(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse>

    /**
     * Invokes DELETE method
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun delete(host: String, port: Int, requestURI: String): Single<HttpClientResponse>

    /**
     * Invokes DELETE method with payload as json
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @param data       json payload
     * @return [HttpClientResponse] response
     */
    fun delete(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse>

    /**
     * Invokes DELETE method with payload as form data with "application/x-www-form-urlencoded" content type
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return [HttpClientResponse] response
     */
    fun deleteFormData(host: String, port: Int, requestURI: String, data: String): Single<HttpClientResponse>

    /**
     * Opens websocket stream
     *
     * @param host       host
     * @param port       port
     * @param requestURI websocket request URI
     * @return WebSocket handler
     */
    fun websocket(host: String, port: Int, requestURI: String): Single<WebSocket>

    /**
     * Method for arbitrary request invocation
     *
     * @param request arbitrary request
     * @return [HttpClientResponse] response
     */
    fun invoke(request: Single<HttpClientRequest>): Single<HttpClientResponse>

    /**
     * Adds custom header for all subsequent requests
     *
     * @param headerName  custom header name
     * @param headerValue custom header value
     */
    fun addCustomHeader(headerName: String, headerValue: String)

    /**
     * Remove custom header for all subsequent requests
     *
     * @param headerName  custom header name
     */
    fun removeCustomHeader(headerName: String)

}
