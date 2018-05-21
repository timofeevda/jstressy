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

package com.github.timofeevda.jstressy.api.httprequest;

import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManager;
import io.reactivex.Single;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpClientResponse;
import io.vertx.reactivex.core.http.WebSocket;

import java.util.Optional;

/**
 * Request executor. Proxy object hiding concrete request/response handling functionality
 *
 * @author timofeevda
 */
public interface RequestExecutor {

    /**
     * Invokes GET method
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return {@link HttpClientResponse} response
     */
    Single<HttpClientResponse> get(String host, int port, String requestURI);

    /**
     * Invokes POST method
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return {@link HttpClientResponse} response
     */
    Single<HttpClientResponse> post(String host, int port, String requestURI);

    /**
     * Invokes POST method with payload as json
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @param data       json payload
     * @return {@link HttpClientResponse} response
     */
    Single<HttpClientResponse> post(String host, int port, String requestURI, String data);

    /**
     * Invokes POST method with payload as form data with "application/x-www-form-urlencoded" content type
     *
     * @param host       host
     * @param port       port
     * @param requestURI request URI
     * @return {@link HttpClientResponse} response
     */
    Single<HttpClientResponse> postFormData(String host, int port, String requestURI, String data);

    /**
     * Opens websocket stream
     *
     * @param host       host
     * @param port       port
     * @param requestURI websocket request URI
     * @return WebSocket handler
     */
    Single<WebSocket> websocket(String host, int port, String requestURI);

    /**
     * Method for arbitrary request invocation
     *
     * @param request arbitrary request
     * @return {@link HttpClientResponse} response
     */
    Single<HttpClientResponse> invoke(HttpClientRequest request);

    /**
     * Returns {@link HttpSessionManager} instance assigned to request executor
     * @return {@link HttpSessionManager} instance which manages requests in this request executor
     */
    Optional<HttpSessionManager> getSessionManager();

}
