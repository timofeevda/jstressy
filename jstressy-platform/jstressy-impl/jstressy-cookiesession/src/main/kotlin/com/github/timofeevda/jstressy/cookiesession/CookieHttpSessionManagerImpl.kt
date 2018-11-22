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

package com.github.timofeevda.jstressy.cookiesession

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httpsession.HttpRequestHeader
import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManager
import io.vertx.reactivex.core.http.HttpClientRequest
import io.vertx.reactivex.core.http.HttpClientResponse
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Simple implementation of session manager. Keeps track of HTTP session cookie, assigning its value to all
 * [HttpClientRequest]s. Allows to configure session cookie name and header name via access to  global parameters in
 * [ConfigurationService]
 *
 * @author timofeevda
 */
internal class CookieHttpSessionManagerImpl : HttpSessionManager {

    private var sessionCookies: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    private var sessionCookieHeader: AtomicReference<HttpRequestHeaderImpl> = AtomicReference()

    override val headers: Collection<HttpRequestHeader>
        get() = getTrackedCookies()

    private fun getTrackedCookies(): List<HttpRequestHeader> {
        return if (sessionCookieHeader.get() == null) {
            emptyList()
        } else {
            listOf(sessionCookieHeader.get())
        }
    }

    override fun processRequest(request: HttpClientRequest): HttpClientRequest {
        val cookieHeader = sessionCookieHeader.get()
        if (cookieHeader != null) {
            request.putHeader(cookieHeader.name, cookieHeader.value)
        }
        return request
    }

    override fun processResponse(response: HttpClientResponse): HttpClientResponse {
        response.cookies()
                .map { it.substringBefore(";") }
                .map { it.split("=") }
                .forEach { sessionCookies[it[0]] = it[1] }
        if (sessionCookies.isNotEmpty()) {
            sessionCookieHeader.set(HttpRequestHeaderImpl("Cookie", sessionCookies.entries.joinToString("; ") { "${it.key}=${it.value}" }))
        }
        return response
    }

    private data class HttpRequestHeaderImpl constructor(override val name: String, override val value: String) : HttpRequestHeader

}
