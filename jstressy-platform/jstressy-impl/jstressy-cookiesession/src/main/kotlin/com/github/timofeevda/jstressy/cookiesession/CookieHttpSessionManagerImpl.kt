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
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream


private const val DEFAULT_COOKIE_NAME = "JSESSIONID"
private const val DEFAULT_SESSION_COOKIE_HEADER_NAME = "Cookie"

/**
 * Simple implementation of session manager. Keeps track of HTTP session cookie, assigning its value to all
 * [HttpClientRequest]s. Allows to configure session cookie name and header name via access to  global parameters in
 * [ConfigurationService]
 *
 * @author timofeevda
 */
internal class CookieHttpSessionManagerImpl(configService: ConfigurationService) : HttpSessionManager {

    /**
     * List of headers which can be added to HTTP session manager externally (e.g. as a result of some HTTP request
     * processing)
     */
    private val customHeaders = CopyOnWriteArrayList<CustomHeader>()

    /**
     * Defines session cookie header name. "Cookie" by default
     */
    private val sessionCookieHeaderName = configService.configuration.globalParameters["session.cookie.header.name"]
            ?: DEFAULT_SESSION_COOKIE_HEADER_NAME

    /**
     * Defines session cookie name. Classic "JSESSIONID" by default
     */
    private val sessionCookieName = configService.configuration.globalParameters["session.cookie.name"]
            ?: DEFAULT_COOKIE_NAME

    /**
     * Pattern used to extract session cookie value from HTTP response header
     */
    private val sessionCookiePattern = Pattern.compile("$sessionCookieName=(.*);")

    private var sessionCookie: String? = null

    override val headers: Collection<HttpRequestHeader>
        get() = Stream.concat(getTrackedCookie().stream(), customHeaders.stream()).collect(Collectors.toList())

    private fun getTrackedCookie(): List<CustomHeader> {
        return Optional.ofNullable(sessionCookie)
                .map { listOf(CustomHeader(sessionCookieHeaderName, "$sessionCookieName=$sessionCookie")) }
                .orElse(emptyList())
    }

    override fun processRequest(request: HttpClientRequest): HttpClientRequest {
        Optional.ofNullable(sessionCookie)
                .ifPresent { cookie -> request.putHeader(sessionCookieHeaderName, "$sessionCookieName=$cookie") }

        customHeaders.forEach { customHeader -> request.putHeader(customHeader.name, customHeader.value) }

        return request
    }

    override fun processResponse(response: HttpClientResponse): HttpClientResponse {
        sessionCookie = response.cookies()
                .map { sessionCookiePattern.matcher(it) }
                .filter { it.find() }
                .map { m -> m.group(1) }
                .first()
        return response
    }

    override fun addCustomHeader(headerName: String, headerValue: String) {
        customHeaders.add(CustomHeader(headerName, headerValue))
    }

    private data class CustomHeader constructor(override val name: String, override val value: String) : HttpRequestHeader

}
