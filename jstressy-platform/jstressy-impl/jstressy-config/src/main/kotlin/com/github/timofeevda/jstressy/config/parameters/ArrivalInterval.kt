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

package com.github.timofeevda.jstressy.config.parameters

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval

@JsonPropertyOrder("id", "delay", "duration", "arrivalRate", "rampArrival", "rampArrivalRate",
    "rampArrivalPeriod", "rampDuration", "poissonArrival", "poissonMinRandom", "randomizeArrival", "arrivalIntervals")
class ArrivalInterval : StressyArrivalInterval {
    constructor(init: ArrivalInterval.() -> Unit): this() {
        init()
    }

    constructor()

    override var poissonArrival: Boolean? = null
    override var poissonMinRandom: Double? = null
    override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
    override var randomizeArrival: Boolean = false
    override var id: String = ""
    override var duration: String =  "1min"
    override var delay: String = "0ms"
    override var arrivalRate: Double? = null
    override var rampArrival: Double? = null
    override var rampArrivalRate: Double? = null
    override var rampArrivalPeriod: String? = null
    override var rampDuration: String? = null
    override fun toString(): String {
        return "ArrivalInterval(poissonArrival=$poissonArrival, poissonMaxRandom=$poissonMinRandom, arrivalIntervals=$arrivalIntervals, id='$id', duration='$duration', delay=$delay, arrivalRate=$arrivalRate, rampArrival=$rampArrival, rampArrivalRate=$rampArrivalRate, rampArrivalPeriod=$rampArrivalPeriod, rampDuration=$rampDuration)"
    }

    fun arrivalInterval(init: ArrivalInterval.() -> Unit) {
        arrivalIntervals.add(ArrivalInterval(init))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrivalInterval

        if (poissonArrival != other.poissonArrival) return false
        if (poissonMinRandom != other.poissonMinRandom) return false
        if (arrivalIntervals != other.arrivalIntervals) return false
        if (randomizeArrival != other.randomizeArrival) return false
        if (id != other.id) return false
        if (duration != other.duration) return false
        if (delay != other.delay) return false
        if (arrivalRate != other.arrivalRate) return false
        if (rampArrival != other.rampArrival) return false
        if (rampArrivalRate != other.rampArrivalRate) return false
        if (rampArrivalPeriod != other.rampArrivalPeriod) return false
        return rampDuration == other.rampDuration
    }

    override fun hashCode(): Int {
        var result = poissonArrival?.hashCode() ?: 0
        result = 31 * result + (poissonMinRandom?.hashCode() ?: 0)
        result = 31 * result + arrivalIntervals.hashCode()
        result = 31 * result + randomizeArrival.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + delay.hashCode()
        result = 31 * result + (arrivalRate?.hashCode() ?: 0)
        result = 31 * result + (rampArrival?.hashCode() ?: 0)
        result = 31 * result + (rampArrivalRate?.hashCode() ?: 0)
        result = 31 * result + (rampArrivalPeriod?.hashCode() ?: 0)
        result = 31 * result + (rampDuration?.hashCode() ?: 0)
        return result
    }


}