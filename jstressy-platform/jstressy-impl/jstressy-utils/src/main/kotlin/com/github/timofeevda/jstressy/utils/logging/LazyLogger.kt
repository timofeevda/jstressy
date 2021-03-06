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

interface LazyLogger {
    fun trace(msg: String?)
    fun trace(msg: String?, vararg params: Any?)
    fun debug(msg: String?)
    fun debug(msg: String?, vararg params: Any?)
    fun info(msg: String?)
    fun info(msg: String?, vararg params: Any?)
    fun warn(msg: String?)
    fun warn(msg: String?, vararg params: Any?)
    fun error(msg: String?)
    fun error(msg: String?, vararg params: Any?)

    fun trace(msg: () -> Any?)
    fun trace(msg: () -> Any?, vararg params: Any?)
    fun debug(msg: () -> Any?)
    fun debug(msg: () -> Any?, vararg params: Any?)
    fun info(msg: () -> Any?)
    fun info(msg: () -> Any?, vararg params: Any?)
    fun warn(msg: () -> Any?)
    fun warn(msg: () -> Any?, vararg params: Any?)
    fun error(msg: () -> Any?)
    fun error(msg: () -> Any?, vararg params: Any?)

    fun trace(msg: () -> Any?, t: Throwable?)
    fun trace(msg: () -> Any?, vararg params: Any?, t: Throwable?)
    fun debug(msg: () -> Any?, t: Throwable?)
    fun debug(msg: () -> Any?, vararg params: Any?, t: Throwable?)
    fun info(msg: () -> Any?, t: Throwable?)
    fun info(msg: () -> Any?, vararg params: Any?, t: Throwable?)
    fun warn(msg: () -> Any?, t: Throwable?)
    fun warn(msg: () -> Any?, vararg params: Any?, t: Throwable?)
    fun error(msg: () -> Any?, t: Throwable?)
    fun error(msg: () -> Any?, vararg params: Any?, t: Throwable?)

    fun trace(msg: String?, t: Throwable?)
    fun trace(msg: String?, vararg params: Any?, t: Throwable?)
    fun debug(msg: String?, t: Throwable?)
    fun debug(msg: String?, vararg params: Any?, t: Throwable?)
    fun info(msg: String?, t: Throwable?)
    fun info(msg: String?, vararg params: Any?, t: Throwable?)
    fun warn(msg: String?, t: Throwable?)
    fun warn(msg: String?, vararg params: Any?, t: Throwable?)
    fun error(msg: String?, t: Throwable?)
    fun error(msg: String?, vararg params: Any?, t: Throwable?)
}

open class LazyLogging {
    val logger: LazyLogger = LazyLoggerFactory.logger(this)

    companion object {
        fun logger(func: () -> Unit): LazyLogger = LazyLoggerFactory.logger(func)
    }
}