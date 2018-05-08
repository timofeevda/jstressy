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

package com.github.timofeevda.jstressy.metrics;

import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Metrics publisher. Writes metrics in Prometheus format to the supplied HTTP response
 *
 * @author timofeevda
 */
class StressyMetricsPublisher {

    private StressyMetricsRegistry stressyMetricsRegistry;

    StressyMetricsPublisher(StressyMetricsRegistry stressyMetricsRegistry) {
        this.stressyMetricsRegistry = stressyMetricsRegistry;
    }

    void publish(HttpServerRequest request, HttpServerResponse response) throws IOException {
        final BufferWriter writer = new BufferWriter();
        TextFormat.write004(writer,
                stressyMetricsRegistry.getMetricRegistry().filteredMetricFamilySamples(parse(request)));
        response
                .setStatusCode(200)
                .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                .end(writer.getBuffer());
    }

    private Set<String> parse(HttpServerRequest request) {
        return new HashSet<>(request.params().getAll("name[]"));
    }

    /**
     * Simple wrapper over {@link Writer} to get string buffer
     */
    private static class BufferWriter extends Writer {

        private final Buffer buffer = Buffer.buffer();

        @Override
        public void write(char[] cbuf, int off, int len) {
            buffer.appendString(new String(cbuf, off, len));
        }

        @Override
        public void flush() {
            // do nothing
        }

        @Override
        public void close() {
            // do nothing
        }

        Buffer getBuffer() {
            return buffer;
        }
    }
}
