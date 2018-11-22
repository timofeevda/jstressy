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

@file:JvmName("StressyUtils")

package com.github.timofeevda.jstressy.utils

import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceEvent
import org.slf4j.MDC
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

data class Duration(val count: Long, val timeUnit: TimeUnit) {

    fun toNanoseconds(): Long {
        return TimeUnit.NANOSECONDS.convert(count, timeUnit)
    }

    fun toMicroseconds(): Long {
        return TimeUnit.MICROSECONDS.convert(count, timeUnit)
    }

    fun toMilliseconds(): Long {
        return TimeUnit.MILLISECONDS.convert(count, timeUnit)
    }

    fun toSeconds(): Long {
        return TimeUnit.SECONDS.convert(count, timeUnit)
    }

    fun toMinutes(): Long {
        return TimeUnit.MINUTES.convert(count, timeUnit)
    }

    fun toHours(): Long {
        return TimeUnit.HOURS.convert(count, timeUnit)
    }

    fun toDays(): Long {
        return TimeUnit.DAYS.convert(count, timeUnit)
    }
}

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
        throw IllegalArgumentException("Invalid duration format: $duration");
    }
    val count = java.lang.Long.parseLong(matcher.group(1))
    val unit = TIME_UNITS[matcher.group(2)] ?: throw IllegalArgumentException("Invalid duration format: $duration")
    return Duration(count, unit)
}

/**
 * Observe OSGI service by service's class name. Looks for the registered service or
 * subscribes to service registration events. Doesn't provide notifications about service
 * state changes
 *
 * @param className service's class name
 * @param bundleContext OSGI bundle context
 * @return [Single] instance which completes when specified service appears in OSGI context
 */
@Suppress("UNCHECKED_CAST")
fun <T> observeService(className: String, bundleContext: BundleContext): Single<T> {
    return Single.create { singleEmitter ->
        val serviceListener = { event: ServiceEvent ->
            val ref = event.serviceReference
            if (event.type == ServiceEvent.REGISTERED) {
                singleEmitter.onSuccess(bundleContext.getService(ref) as T)
            }
        }
        bundleContext.addServiceListener(serviceListener, "(objectClass=$className)")
        synchronized(singleEmitter) {
            val ref = bundleContext.getServiceReference(className)
            if (ref != null) {
                singleEmitter.onSuccess(bundleContext.getService(ref) as T)
            }
        }
        singleEmitter.setCancellable { bundleContext.removeServiceListener(serviceListener) }
    }
}

/**
 * Configure RxJava plugins to enable MDC context passing between scheduled threads
 */
fun setupRxJavaMDC() {

    class MDCRunnable(private val runnable: Runnable) : Runnable {

        private var context: Map<String, String>? = MDC.getCopyOfContextMap()

        override fun run() {
            val currentMDC = MDC.getCopyOfContextMap()
            try {
                if (context != null) {
                    MDC.setContextMap(context)
                }
                runnable.run()
            } finally {
                MDC.clear()
                if (currentMDC != null) {
                    MDC.setContextMap(currentMDC)
                }
            }
        }
    }

    RxJavaPlugins.setScheduleHandler { r -> MDCRunnable(r) }
}

