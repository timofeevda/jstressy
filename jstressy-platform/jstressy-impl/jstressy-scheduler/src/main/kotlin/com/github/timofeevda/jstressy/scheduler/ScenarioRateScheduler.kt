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
package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.utils.StressyUtils
import com.github.timofeevda.jstressy.utils.StressyUtils.parseDuration
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Provides observable stream of events generated at the moments when scenario defined in stage is needed to be
 * executed. Works with stage configuration, both with constant and non-constant scenario invocation rates
 */
object ScenarioRateScheduler {

    fun observeScenarioTicks(stage: StressyStage): Observable<Long> {
        val delay = StressyUtils.parseDuration(stage.stageDelay ?: "0ms").toMilliseconds()
        val duration = StressyUtils.parseDuration(stage.stageDuration).toMilliseconds()
        return Observable
                .timer(delay, TimeUnit.MILLISECONDS)
                .flatMap { observeWithRamping(stage) ?: observeWithoutRamping(stage) }
                .take(delay + duration, TimeUnit.MILLISECONDS)
    }

    /**
     * Observe "tick" events for the case when constant scenario invocation rate is specified in configuration
     */
    private fun observeWithoutRamping(stage: StressyStage): Observable<Long> {
        return Observable.interval(0, toPeriod(stage.arrivalRate), TimeUnit.MILLISECONDS)
    }

    /**
     * Observe "tick" events for the case when non-constant scenario invocation rate is specified in configuration.
     * Invocation rate can be "ramped" during some interval with constant "ramping" rate to the target one
     */
    private fun observeWithRamping(stage: StressyStage): Observable<Long>? {
        return if (stage.rampArrival != null
                && stage.rampArrivalRate != null
                && stage.rampInterval != null) {
            val rampPeriod = toPeriod(stage.rampArrivalRate ?: 1.0)
            val rampDuration = parseDuration(stage.rampInterval ?: "0ms").toMilliseconds()
            val rampSteps = (rampDuration / rampPeriod).toInt()
            val rampIncrease = (stage.rampArrival!! - stage.arrivalRate) / rampSteps
            Observable
                    .interval(0, rampPeriod, TimeUnit.MILLISECONDS)
                    .take(rampSteps.toLong())
                    .map { stage.arrivalRate + rampIncrease * it }
                    .switchMap { newRate -> Observable.interval(toPeriod(newRate), TimeUnit.MILLISECONDS) }
        } else {
            null
        }
    }

    private fun toPeriod(rate: Double) = (1000 / rate).toLong()
}