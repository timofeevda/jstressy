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

package com.github.timofeevda.jstressy.metrics

import io.prometheus.client.exporter.common.TextFormat
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.http.HttpServerRequest
import io.vertx.reactivex.core.http.HttpServerResponse

import java.io.IOException
import java.io.Writer
import java.util.HashSet

/**
 * Metrics publisher. Writes metrics in Prometheus format to the supplied HTTP response
 *
 * @author timofeevda
 */
internal class StressyMetricsPublisher(private val stressyMetricsRegistry: StressyMetricsRegistry) {

    @Throws(IOException::class)
    fun publish(request: HttpServerRequest, response: HttpServerResponse) {
        val writer = BufferWriter()
        TextFormat.write004(writer,
                stressyMetricsRegistry.getCollectorMetricRegistry().filteredMetricFamilySamples(parse(request)))
        response
                .setStatusCode(200)
                .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                .end(writer.buffer)
    }

    private fun parse(request: HttpServerRequest): Set<String> {
        return HashSet(request.params().getAll("name[]"))
    }

    /**
     * Simple wrapper over [Writer] to get string buffer
     */
    private class BufferWriter : Writer() {

        internal val buffer = Buffer.buffer()

        override fun write(cbuf: CharArray, off: Int, len: Int) {
            buffer.appendString(String(cbuf, off, len))
        }

        override fun flush() {
            // do nothing
        }

        override fun close() {
            // do nothing
        }
    }
}
