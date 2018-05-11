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

package com.github.timofeevda.jstressy.httprequest;

import com.github.timofeevda.jstressy.api.httpclient.HttpClientService;
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor;
import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManager;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService;
import com.github.timofeevda.jstressy.api.metrics.type.Timer;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpClientResponse;
import io.vertx.reactivex.core.http.WebSocket;

import java.util.concurrent.TimeUnit;

/**
 * Basic implementation of {@link RequestExecutor}. Also serves as HTTP session manager implemenation doing nothing
 * with processed requests
 *
 * @author timofeevda
 */
class StressyRequestExecutor implements RequestExecutor, HttpSessionManager {

    private final HttpClient client;
    private final MetricsRegistry metricsRegistry;

    StressyRequestExecutor(HttpClientService httpClientService,
                           MetricsRegistryService metricsRegistryService) {
        this.client = httpClientService.get();
        this.metricsRegistry = metricsRegistryService.get();
    }

    @Override
    public Single<HttpClientResponse> get(String host, int port, String requestURI) {
        return getMeasuredRequest(processRequest(client.request(HttpMethod.GET, port, host, requestURI)));
    }

    @Override
    public Single<HttpClientResponse> post(String host, int port, String requestURI) {
        return getMeasuredRequest(processRequest(client.request(HttpMethod.POST, port, host, requestURI)));
    }

    @Override
    public Single<HttpClientResponse> post(String host, int port, String requestURI, String data) {
        return getMeasuredRequestWithPayload(
                processJsonDataRequest(client.request(HttpMethod.POST, port, host, requestURI), data), data);
    }

    @Override
    public Single<WebSocket> websocket(String host, int port, String requestURI) {
        HeadersAdaptor headersAdaptor = new HeadersAdaptor(new DefaultHttpHeaders());
        return Single.create(emitter -> {
            RequestTimer requestTimer = new RequestTimer("WebSocket Connection Setup");
            client.websocketStream(port, host, requestURI, new MultiMap(headersAdaptor))
                    .toObservable()
                    .doOnSubscribe(disposable -> requestTimer.start())
                    .doOnNext(webSocket -> requestTimer.stop())
                    .subscribe(emitter::onSuccess, emitter::onError);
        });
    }

    @Override
    public Single<HttpClientResponse> invoke(HttpClientRequest request) {
        return getMeasuredRequest(request);
    }

    @Override
    public Single<HttpClientResponse> postFormData(String host, int port, String requestURI, String data) {
        return getMeasuredRequestWithPayload(
                processFormDataRequest(client.request(HttpMethod.POST, port, host, requestURI), data), data);
    }

    private Single<HttpClientResponse> getMeasuredRequest(HttpClientRequest rq) {
        RequestTimer requestTimer = new RequestTimer("rpath_" + rq.uri());
        return Single.create(emitter -> {
            rq.toObservable()
                    .timeout(60, TimeUnit.SECONDS)
                    .doOnSubscribe(disposable -> requestTimer.start())
                    .doOnNext(response -> {
                        requestTimer.stop();
                        processResponse(response);
                    })
                    .subscribe(emitter::onSuccess, emitter::onError);
            rq.end();
        });
    }

    private Single<HttpClientResponse> getMeasuredRequestWithPayload(HttpClientRequest rq, String data) {
        RequestTimer requestTimer = new RequestTimer(rq.uri());
        return Single.create(emitter -> {
            rq.toObservable()
                    .timeout(60, TimeUnit.SECONDS)
                    .doOnSubscribe(disposable -> requestTimer.start())
                    .doOnNext(response -> {
                        requestTimer.stop();
                        processResponse(response);
                    })
                    .subscribe(emitter::onSuccess, emitter::onError);
            rq.write(data);
            rq.end();
        });
    }

    @Override
    public HttpClientResponse processResponse(HttpClientResponse response) {
        return response;
    }

    @Override
    public HttpClientRequest processRequest(HttpClientRequest request) {
        return request;
    }

    private HttpClientRequest processFormDataRequest(HttpClientRequest request, String data) {
        return processRequest(request)
                .putHeader("Content-Length", Integer.toString(data.getBytes().length))
                .putHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    private HttpClientRequest processJsonDataRequest(HttpClientRequest request, String jsonData) {
        return processRequest(request)
                .putHeader("Content-Length", Integer.toString(jsonData.getBytes().length))
                .putHeader("Content-Type", "application/json");
    }

    private class RequestTimer {
        private Timer.Context context;
        private final Timer timer;

        private RequestTimer(String name) {
            timer = metricsRegistry.timer(name);
        }

        private void start() {
            context = timer.context();
        }

        private void stop() {
            context.stop();
        }
    }
}
