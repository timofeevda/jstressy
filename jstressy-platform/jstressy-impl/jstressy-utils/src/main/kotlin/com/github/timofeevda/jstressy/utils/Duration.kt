package com.github.timofeevda.jstressy.utils

import java.util.concurrent.TimeUnit

/**
 * Represents time interval using number of [TimeUnit]s
 */
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