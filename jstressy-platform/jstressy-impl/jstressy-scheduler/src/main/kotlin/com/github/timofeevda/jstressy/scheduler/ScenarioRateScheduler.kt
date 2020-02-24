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

import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalDefinition
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

    private const val constantRateId = "ConstantRate"
    private const val rampingRateId = "RampingRate"

    fun observeScenarioTicks(stage: StressyStage): Observable<String> {
        val delay = StressyUtils.parseDuration(stage.stageDelay ?: "0ms").toMilliseconds()
        val duration = StressyUtils.parseDuration(stage.stageDuration).toMilliseconds()
        val scenariosLimit = stage.scenariosLimit
        val timeBoundedScenariosStream = Observable
                .timer(delay, TimeUnit.MILLISECONDS)
                .flatMap { observeArrivalIntervals(stage) ?: observeWithRamping(stage) ?: observeWithoutRamping(stage) }
                .take(delay + duration, TimeUnit.MILLISECONDS)
        return if (scenariosLimit != null) timeBoundedScenariosStream.take(scenariosLimit.toLong()) else timeBoundedScenariosStream
    }

    /**
     * Creates observable for the list of arbitrary configured arrival intervals
     */
    private fun observeArrivalIntervals(stage: StressyStage): Observable<String>? {
        return if (stage.arrivalIntervals.isEmpty()) {
            null
        } else {
            Observable.merge(stage.arrivalIntervals.map { arrivalInterval ->
                val delay = StressyUtils.parseDuration(arrivalInterval.delay ?: "0ms").toMilliseconds()
                val observable = Observable.timer(delay, TimeUnit.MILLISECONDS)
                        .flatMap {
                            observeWithRamping(arrivalInterval, arrivalInterval.id)
                                    ?: observeWithoutRamping(arrivalInterval, arrivalInterval.id)
                        }
                        .take(delay + parseDuration(arrivalInterval.duration).toMilliseconds(), TimeUnit.MILLISECONDS)
                observable
            })
        }
    }

    /**
     * Observe "tick" events for the case when constant scenario invocation rate is specified in configuration
     */
    private fun observeWithoutRamping(arrivalDefinition: StressyArrivalDefinition,
                                      arrivalIntervalId: String = constantRateId): Observable<String> {
        return Observable.interval(0, toPeriod(arrivalDefinition.arrivalRate ?: 1.0), TimeUnit.MILLISECONDS)
                .map { arrivalIntervalId }
    }

    /**
     * Observe "tick" events for the case when non-constant scenario invocation rate is specified in configuration.
     * Invocation rate can be "ramped" during some interval with constant "ramping" rate to the target one
     */
    private fun observeWithRamping(arrivalDefinition: StressyArrivalDefinition,
                                   arrivalIntervalId: String = rampingRateId): Observable<String>? {
        return if (arrivalDefinition.rampArrival != null
                && (arrivalDefinition.rampArrivalRate != null || arrivalDefinition.rampArrivalPeriod != null)
                && arrivalDefinition.rampDuration != null) {

            // get ramp period - ramp arrival rate property has higher priority
            val rampPeriod: Long = when {
                arrivalDefinition.rampArrivalRate != null -> toPeriod(arrivalDefinition.rampArrivalRate ?: 1.0)
                arrivalDefinition.rampArrivalPeriod != null -> parseDuration(arrivalDefinition.rampArrivalPeriod
                        ?: "0m").toMilliseconds()
                else -> toPeriod(1.0)
            }

            val rampDuration = parseDuration(arrivalDefinition.rampDuration ?: "0ms").toMilliseconds()
            val rampSteps = (rampDuration / rampPeriod).toInt()
            val rampIncrease = (arrivalDefinition.rampArrival!! - (arrivalDefinition.arrivalRate ?: 1.0)) / rampSteps
            Observable
                    .interval(0, rampPeriod, TimeUnit.MILLISECONDS)
                    .take(rampSteps.toLong())
                    .map { (arrivalDefinition.arrivalRate ?: 1.0) + rampIncrease * it }
                    .switchMap { newRate -> Observable.interval(toPeriod(newRate), TimeUnit.MILLISECONDS) }
                    .map { arrivalIntervalId }
        } else {
            null
        }
    }

    private fun toPeriod(rate: Double) = (1000 / rate).toLong()
}