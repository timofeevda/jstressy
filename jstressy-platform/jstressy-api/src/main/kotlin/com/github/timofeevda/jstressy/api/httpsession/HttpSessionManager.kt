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

package com.github.timofeevda.jstressy.api.httpsession

import io.reactivex.Single
import io.vertx.reactivex.core.http.HttpClientRequest
import io.vertx.reactivex.core.http.HttpClientResponse

/**
 * Session manager which can be used to implement stateful HTTP communication
 *
 * @author timofeevda
 */
interface HttpSessionManager {

    /**
     * Returns current collection of request headers being passed to each request by this session manager.
     *
     * @return current collection of request headers being passed to each request by this session manager,
     * empty collection in case there is no HTTP headers to set or request manager doesn't implement this logic
     */
    val headers: Collection<HttpRequestHeader>

    /**
     * Processes HTTP request (e.g. adding session cookie)
     *
     * @param request [HttpClientRequest] to process
     * @return [HttpClientRequest] instance
     */
    fun processRequest(request: Single<HttpClientRequest>): Single<HttpClientRequest>

    /**
     * Processes HTTP response (e.g. getting new session cookie)
     *
     * @param response [HttpClientResponse] to process
     * @return [HttpClientResponse] instance
     */
    fun processResponse(response: HttpClientResponse): HttpClientResponse

}
