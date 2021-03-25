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
package com.github.timofeevda.jstressy.utils

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

private val DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)")

private val TIME_UNITS = hashMapOf(
        Pair("ns", TimeUnit.NANOSECONDS),
        Pair("nanosecond", TimeUnit.NANOSECONDS),
        Pair("nanoseconds", TimeUnit.NANOSECONDS),
        Pair("um", TimeUnit.MICROSECONDS),
        Pair("microsecond", TimeUnit.MICROSECONDS),
        Pair("microseconds", TimeUnit.MICROSECONDS),
        Pair("ms", TimeUnit.MILLISECONDS),
        Pair("millisecond", TimeUnit.MILLISECONDS),
        Pair("milliseconds", TimeUnit.MILLISECONDS),
        Pair("s", TimeUnit.SECONDS),
        Pair("second", TimeUnit.SECONDS),
        Pair("seconds", TimeUnit.SECONDS),
        Pair("m", TimeUnit.MINUTES),
        Pair("min", TimeUnit.MINUTES),
        Pair("mins", TimeUnit.MINUTES),
        Pair("minute", TimeUnit.MINUTES),
        Pair("minutes", TimeUnit.MINUTES),
        Pair("h", TimeUnit.HOURS),
        Pair("hour", TimeUnit.HOURS),
        Pair("hours", TimeUnit.HOURS),
        Pair("d", TimeUnit.DAYS),
        Pair("day", TimeUnit.DAYS),
        Pair("days", TimeUnit.DAYS)
)

object StressyUtils {

    /**
     * Parse duration represented by human-readable string
     *
     * Available time interval descriptors:
     *
     * "ns", "nanosecond","nanoseconds",
     * "um","microsecond","microseconds",
     * "ms","millisecond","milliseconds",
     * "s","second","seconds",
     * "m","min","mins","minute","minutes",
     * "h","hour","hours",
     * "d","day","days"
     *
     * @param duration human-readable duration representation
     * @return [Duration] instance
     */
    fun parseDuration(duration: String): Duration {
        val matcher = DURATION_PATTERN.matcher(duration)
        if (!matcher.matches()) {
            throw IllegalArgumentException("Invalid duration format: $duration")
        }
        val count = java.lang.Long.parseLong(matcher.group(1))
        val unit = TIME_UNITS[matcher.group(2)] ?: throw IllegalArgumentException("Invalid duration format: $duration")
        return Duration(count, unit)
    }

    fun httpTimeout() = parseDuration(System.getProperty("httpTimeout", "1m"))

    fun getBlockedEventLoopThreadTimeout() =
            parseDuration(System.getProperty("vertx.blocked.event.loop.timeout", "500ms"))

}


