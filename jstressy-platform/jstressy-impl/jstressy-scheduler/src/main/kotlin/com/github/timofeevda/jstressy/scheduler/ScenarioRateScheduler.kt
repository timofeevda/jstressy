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
import com.github.timofeevda.jstressy.utils.SchedulerUtils
import com.github.timofeevda.jstressy.utils.StressyUtils.parseDuration
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.concurrent.TimeUnit

/**
 * Provides observable stream of events generated at the moments when scenario defined in stage is needed to be
 * executed. Works with stage configuration, both with constant and non-constant scenario invocation rates
 */
object ScenarioRateScheduler {

    private const val constantRateId = "ConstantArrivalRate"
    private const val constantPoissonId = "ConstantPoissonArrival"

    private const val rampingRateId = "RampingArrivalRate"
    private const val rampingPoissonId = "RampingPoissonArrival"

    /**
     * Observe scenario arrivals based on arrival definitions. Observable stream generates arrival interval identifier
     * which can be used to implement custom scenario logic based on arrival interval
     *
     * In case arrival interval defition doesn't have specific identifier one of the default identifier set is selected
     * based on the type of arrival interval (constant, ramping, constant Poisson, ramping Poisson)
     *
     * @param stage stage definition
     */
    fun observeScenarioArrivals(stage: StressyStage): Observable<String> {
        val delay = parseDuration(stage.stageDelay ?: "0ms").toMilliseconds()
        val duration = parseDuration(stage.stageDuration).toMilliseconds()
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
                val delay = parseDuration(arrivalInterval.delay ?: "0ms").toMilliseconds()
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
     * Observe arrival events for the case when constant scenario invocation rate is specified in configuration
     */
    private fun observeWithoutRamping(arrivalDefinition: StressyArrivalDefinition,
                                      arrivalIntervalId: String = constantRateId): Observable<String> {
        return if (isPoissonArrival(arrivalDefinition)) {
            observePoissonArrivals(arrivalDefinition.arrivalRate,
                    arrivalDefinition.poissonMaxRandom,
                    if (arrivalIntervalId == constantRateId) constantPoissonId else arrivalIntervalId)
        } else {
            Observable.interval(0, toPeriod(arrivalDefinition.arrivalRate ?: 1.0), TimeUnit.MILLISECONDS)
                    .map { arrivalIntervalId }
        }
    }

    /**
     * Observe arrival events for the case when non-constant scenario invocation rate is specified in configuration.
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
                    .switchMap { newRate ->
                        if (isPoissonArrival(arrivalDefinition)) {
                            observePoissonArrivals(newRate, arrivalDefinition.poissonMaxRandom, rampingPoissonId)
                        } else {
                            Observable.interval(toPeriod(newRate), TimeUnit.MILLISECONDS)
                        }
                    }
                    .map { arrivalIntervalId }
        } else {
            null
        }
    }

    /**
     * Observe Poisson arrival events recursively scheduling next Poisson arrival after processing the previous one
     *
     * @param arrivalRate arrival rate which is used to determine the next Poisson arrival
     * @param poissonMaxRandom max random value which can be used to achieve bigger intervals between Poisson arrivals
     * @param arrivalIntervalId arrival interval reference id
     */
    private fun observePoissonArrivals(arrivalRate: Double?, poissonMaxRandom: Double?, arrivalIntervalId: String): Observable<String> {
        return Observable.create { emitter -> observePoissonArrivals(emitter, arrivalRate, poissonMaxRandom, arrivalIntervalId) }
    }

    /**
     * Observe Poisson arrival events recursively scheduling next Poisson arrival after processing the previous one
     *
     * @param emitter Poisson arrival event consumer
     * @param arrivalRate arrival rate which is used to determine the next Poisson arrival
     * @param poissonMaxRandom max random value which can be used to achieve bigger intervals between Poisson arrivals
     * @param arrivalIntervalId arrival interval reference id
     */
    private fun observePoissonArrivals(emitter: ObservableEmitter<String>,
                                       arrivalRate: Double?,
                                       poissonMaxRandom: Double?,
                                       arrivalIntervalId: String) {
        val nextPoissonArrival = if (poissonMaxRandom == null) {
            SchedulerUtils.observeNextPoissonArrival(arrivalRate
                    ?: 1.0)
        } else {
            SchedulerUtils.observeNextPoissonArrival(arrivalRate ?: 1.0, poissonMaxRandom)
        }
        nextPoissonArrival
                .map { arrivalIntervalId }
                .doAfterNext { observePoissonArrivals(emitter, arrivalRate, poissonMaxRandom, arrivalIntervalId) }
                .subscribe { id -> emitter.onNext(id) }
    }

    /**
     * Check if arrival interval should be treated as Poisson arrivals
     */
    private fun isPoissonArrival(arrivalDefinition: StressyArrivalDefinition) =
            arrivalDefinition.poissonArrival != null && arrivalDefinition.poissonArrival == true

    private fun toPeriod(rate: Double) = (1000 / rate).toLong()
}