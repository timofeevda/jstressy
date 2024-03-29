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

package com.github.timofeevda.jstressy.websocket.utils

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.http.WebSocket

object WebSocketUtils {

    /**
     * Converts [WebSocket] to [Observable] stream of text messages. It assumes that [WebSocket] is opened in text mode
     *
     */
    fun webSocketToMessages(webSocket: WebSocket): Observable<WebSocketTextMessage> {
        return Observable.create { emitter -> webSocketFramesHandler(webSocket, emitter) }
    }

    /**
     * Converts [WebSocket] to [Observable] stream of binary messages. It assumes that [WebSocket] is opened in binary mode
     *
     */
    fun webSocketToBinaryMessages(webSocket: WebSocket): Observable<ByteArray> {
        return Observable.create { emitter -> webSocketBinaryFramesHandler(webSocket, emitter) }
    }

    /**
     * Adds frame handler to [WebSocket] instance and uses [ObservableEmitter] to notify subscribers with messages.
     *
     * Handles continuation and final WebSocket frames, on receiving "close" frame completes observable stream
     */
    private fun webSocketBinaryFramesHandler(webSocket: WebSocket, emitter: ObservableEmitter<ByteArray>) {
        var buffer = Buffer.buffer()
        webSocket.exceptionHandler { e -> emitter.onError(e)}
        webSocket.frameHandler { frame ->
            if (frame.isClose) {
                // WebSocket is closed, complete the stream
                emitter.onComplete()
            } else {
                if (!frame.isFinal) {
                    buffer.appendBuffer(frame.binaryData())
                } else {
                    if (buffer.length() == 0 && frame.isFinal) {
                        // we've got single full frame, pass it directly as message
                        emitter.onNext(frame.binaryData().bytes)
                    } else {
                        // join all frames and pass it as single message
                        buffer.appendBuffer(frame.binaryData())
                        try {
                            emitter.onNext(buffer.bytes)
                        } finally {
                            buffer = Buffer.buffer()
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds frame handler to [WebSocket] instance and uses [ObservableEmitter] to notify subscribers with messages.
     *
     * Handles continuation and final WebSocket frames, on receiving "close" frame completes observable stream
     */
    private fun webSocketFramesHandler(webSocket: WebSocket, emitter: ObservableEmitter<WebSocketTextMessage>) {
        val frames = ArrayList<String>()
        var bytes = 0
        webSocket.exceptionHandler { e -> emitter.onError(e)}
        webSocket.frameHandler { frame ->
            if (frame.isClose) {
                // WebSocket is closed, complete the stream
                emitter.onComplete()
            } else {
                if (!frame.isFinal) {
                    frames.add(frame.textData())
                    bytes += frame.binaryData().length()
                } else {
                    if (frames.isEmpty() && frame.isFinal) {
                        // we've got single full frame, pass it directly as message
                        emitter.onNext(WebSocketTextMessage(frame.textData(), frame.binaryData().length()))
                    } else {
                        // join all frames and pass it as single message
                        frames.add(frame.textData())
                        bytes += frame.binaryData().length()
                        try {
                            val message = frames.joinToString("")
                            emitter.onNext(WebSocketTextMessage(message, bytes))
                        } finally {
                            frames.clear()
                            bytes = 0
                        }
                    }
                }
            }
        }
    }
}
