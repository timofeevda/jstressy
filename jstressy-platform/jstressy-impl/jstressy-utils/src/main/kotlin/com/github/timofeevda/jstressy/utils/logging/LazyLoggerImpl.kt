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

package com.github.timofeevda.jstressy.utils.logging

import com.github.structlog4j.ILogger

internal class LazyLoggerImpl(private val jLogger: ILogger) : LazyLogger {

    override fun trace(msg: String?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg)
    }

    override fun trace(msg: String?, vararg params: Any?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg, params)
    }

    override fun debug(msg: String?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg)
    }

    override fun debug(msg: String?, vararg params: Any?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg, params)
    }

    override fun info(msg: String?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg)
    }

    override fun info(msg: String?, vararg params: Any?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg, params)
    }

    override fun warn(msg: String?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg)
    }

    override fun warn(msg: String?, vararg params: Any?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg, params)
    }

    override fun error(msg: String?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg)
    }

    override fun error(msg: String?, vararg params: Any?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg, params)
    }

    override fun trace(msg: () -> Any?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg.toSafeString())
    }

    override fun trace(msg: () -> Any?, vararg params: Any?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg.toSafeString(), params)
    }

    override fun debug(msg: () -> Any?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg.toSafeString())
    }

    override fun debug(msg: () -> Any?, vararg params: Any?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg.toSafeString(), params)
    }

    override fun info(msg: () -> Any?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg.toSafeString())
    }

    override fun info(msg: () -> Any?, vararg params: Any?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg.toSafeString(), params)
    }

    override fun warn(msg: () -> Any?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg.toSafeString())
    }

    override fun warn(msg: () -> Any?, vararg params: Any?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg.toSafeString(), params)
    }

    override fun error(msg: () -> Any?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg.toSafeString())
    }

    override fun error(msg: () -> Any?, vararg params: Any?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg.toSafeString(), params)
    }

    override fun trace(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg.toSafeString(), t)
    }

    override fun trace(msg: () -> Any?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg.toSafeString(), params, t)
    }

    override fun debug(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg.toSafeString(), t)
    }

    override fun debug(msg: () -> Any?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg.toSafeString(), params, t)
    }

    override fun info(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg.toSafeString(), t)
    }

    override fun info(msg: () -> Any?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg.toSafeString(), params, t)
    }

    override fun warn(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg.toSafeString(), t)
    }

    override fun warn(msg: () -> Any?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg.toSafeString(), params, t)
    }

    override fun error(msg: () -> Any?, t: Throwable?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg.toSafeString(), t)
    }

    override fun error(msg: () -> Any?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg.toSafeString(), params, t)
    }

    override fun trace(msg: String?, t: Throwable?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg, t)
    }

    override fun trace(msg: String?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isTraceEnabled) jLogger.trace(msg, params, t)
    }

    override fun debug(msg: String?, t: Throwable?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg, t)
    }

    override fun debug(msg: String?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isDebugEnabled) jLogger.debug(msg, params, t)
    }

    override fun info(msg: String?, t: Throwable?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg, t)
    }

    override fun info(msg: String?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isInfoEnabled) jLogger.info(msg, params, t)
    }

    override fun warn(msg: String?, t: Throwable?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg, t)
    }

    override fun warn(msg: String?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isWarnEnabled) jLogger.warn(msg, params, t)
    }

    override fun error(msg: String?, t: Throwable?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg, t)
    }

    override fun error(msg: String?, vararg params: Any?, t: Throwable?) {
        if (jLogger.isErrorEnabled) jLogger.error(msg, params, t)
    }

    private fun (() -> Any?).toSafeString(): String {
        return try {
            invoke().toString()
        } catch (e: Exception) {
            "Exception while invoking log message: $e"
        }
    }
}