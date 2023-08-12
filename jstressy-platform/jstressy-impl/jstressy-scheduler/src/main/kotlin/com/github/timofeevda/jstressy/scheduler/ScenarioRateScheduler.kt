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

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
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

    private const val constantRateId = "ConstantArrivaRate"
    private const val constantPoissonId = "ConstantPoissonArrival"
    private const val constantRandomizedId = "ConstantRandomizedArrival"

    private const val rampingRateId = "RampingArrivalRate"
    private const val rampingPoissonId = "RampingPoissonArrival"
    private const val rampingRandomizedId = "RampingRandomizedArrival"

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
        val scenariosLimit = stage.scenariosLimit
        val timeBoundedScenariosStream =
            observeArrivalIntervals(stage) ?: observeWithRamping(stage) ?: observeWithoutRamping(stage)
        return if (scenariosLimit != null) timeBoundedScenariosStream.take(scenariosLimit.toLong()) else timeBoundedScenariosStream
    }

    fun observeScenarioActionArrivals(actionDefinition: ScenarioActionDefinition): Observable<String> {
        return observeArrivalIntervals(actionDefinition) ?: observeWithRamping(actionDefinition)
        ?: observeWithoutRamping(actionDefinition)
    }

    /**
     * Creates observable for the list of arbitrary configured arrival intervals
     */
    private fun observeArrivalIntervals(arrivalDefinition: StressyArrivalDefinition): Observable<String>? {
        return if (arrivalDefinition.arrivalIntervals.isEmpty()) {
            null
        } else {
            Observable.merge(arrivalDefinition.arrivalIntervals.map { arrivalInterval ->
                observeWithRamping(arrivalInterval, arrivalInterval.id) ?: observeWithoutRamping(
                    arrivalInterval,
                    arrivalInterval.id
                )
            })
        }
    }

    /**
     * Observe arrival events for the case when constant scenario invocation rate is specified in configuration
     */
    private fun observeWithoutRamping(
        arrivalDefinition: StressyArrivalDefinition,
        arrivalIntervalId: String = constantRateId
    ): Observable<String> {
        val delay = parseDuration(arrivalDefinition.delay ?: "0ms").toMilliseconds()
        return if (isPoissonArrival(arrivalDefinition)) {
            Observable.timer(delay, TimeUnit.MILLISECONDS)
                .flatMap {
                    observePoissonArrivals(
                        arrivalDefinition.arrivalRate,
                        arrivalDefinition.poissonMinRandom,
                        if (arrivalIntervalId == constantRateId) constantPoissonId else arrivalIntervalId
                    )
                }
                .takeForDuration(arrivalDefinition)
        } else if (isRandomArrival(arrivalDefinition)) {
            Observable.timer(delay, TimeUnit.MILLISECONDS)
                .flatMap {
                    observeRandomArrivals(
                        arrivalDefinition.arrivalRate ?: 1.0,
                        if (arrivalIntervalId == constantRateId) constantRandomizedId else arrivalIntervalId
                    )
                }
                .takeForDuration(arrivalDefinition)
        } else {
            Observable.timer(delay, TimeUnit.MILLISECONDS)
                .flatMap { Observable.interval(0, toPeriod(arrivalDefinition.arrivalRate ?: 1.0), TimeUnit.MILLISECONDS)}
                .takeForDuration(arrivalDefinition)
                .map { arrivalIntervalId }
        }
    }

    /**
     * Observe arrival events for the case when non-constant scenario invocation rate is specified in configuration.
     * Invocation rate can be "ramped" during some interval with constant "ramping" rate to the target one
     */
    private fun observeWithRamping(
        arrivalDefinition: StressyArrivalDefinition,
        arrivalIntervalId: String = rampingRateId
    ): Observable<String>? {
        return if (arrivalDefinition.rampArrival != null
            && (arrivalDefinition.rampArrivalRate != null || arrivalDefinition.rampArrivalPeriod != null)
            && arrivalDefinition.rampDuration != null
        ) {

            // get ramp period - ramp arrival rate property has higher priority
            val rampPeriod: Long = when {
                arrivalDefinition.rampArrivalRate != null -> toPeriod(arrivalDefinition.rampArrivalRate ?: 1.0)
                arrivalDefinition.rampArrivalPeriod != null -> parseDuration(
                    arrivalDefinition.rampArrivalPeriod
                        ?: "0m"
                ).toMilliseconds()
                else -> toPeriod(1.0)
            }

            val rampDuration = parseDuration(arrivalDefinition.rampDuration ?: "0ms").toMilliseconds()
            val rampSteps = (rampDuration / rampPeriod).toInt()
            val rampIncrease = (arrivalDefinition.rampArrival!! - (arrivalDefinition.arrivalRate ?: 1.0)) / rampSteps
            val delay = parseDuration(arrivalDefinition.delay ?: "0ms").toMilliseconds()
            Observable
                .interval(delay, rampPeriod, TimeUnit.MILLISECONDS)
                .take(rampSteps.toLong())
                .map { (arrivalDefinition.arrivalRate ?: 1.0) + rampIncrease * it }
                .switchMap { newRate ->
                    if (isPoissonArrival(arrivalDefinition)) {
                        observePoissonArrivals(newRate, arrivalDefinition.poissonMinRandom, rampingPoissonId)
                    } else if(isRandomArrival(arrivalDefinition)) {
                        observeRandomArrivals(newRate, rampingRandomizedId)
                    } else {
                        Observable.interval(0, toPeriod(newRate), TimeUnit.MILLISECONDS)
                    }
                }
                .map { arrivalIntervalId }
                .takeForDuration(arrivalDefinition)
        } else {
            null
        }
    }

    /**
     * Observe arrival events randomly within the time interval defined by arrival rate
     *
     * @param arrivalRate arrival rate which is used to determine the next arrival
     * @param arrivalIntervalId arrival interval reference id
     */
    private fun observeRandomArrivals(
        arrivalRate: Double,
        arrivalIntervalId: String
    ): Observable<String> {
        return Observable.create { emitter ->
            observeRandomArrivals(
                emitter,
                arrivalRate,
                arrivalIntervalId
            )
        }
    }

    /**
     * Observe arrival events randomly within the time interval defined by arrival rate
     *
     * @param emitter arrival event consumer
     * @param arrivalRate arrival rate which is used to determine the next arrival
     * @param arrivalIntervalId arrival interval reference id
     */
    private fun observeRandomArrivals(
        emitter: ObservableEmitter<String>,
        arrivalRate: Double,
        arrivalIntervalId: String
    ) {
        if (emitter.isDisposed) {
            return
        }
        val nextArrivalRandom = (toPeriod(arrivalRate) * Math.random()).toLong()
        val delayAfterArrival = toPeriod(arrivalRate) - nextArrivalRandom
        val nextArrival = Observable.timer(nextArrivalRandom, TimeUnit.MILLISECONDS)
        nextArrival
            .map { arrivalIntervalId }
            .doAfterNext {
                Observable.timer(delayAfterArrival, TimeUnit.MILLISECONDS)
                    .subscribe { _ -> observeRandomArrivals(emitter, arrivalRate, arrivalIntervalId) }
            }
            .subscribe { id ->
                if (!emitter.isDisposed) {
                    emitter.onNext(id)
                }
            }
    }

    /**
     * Observe Poisson arrival events recursively scheduling next Poisson arrival after processing the previous one
     *
     * @param arrivalRate arrival rate which is used to determine the next Poisson arrival
     * @param poissonMinRandom min random value which can be used to achieve smaller intervals between Poisson arrivals
     * @param arrivalIntervalId arrival interval reference id
     */
    private fun observePoissonArrivals(
        arrivalRate: Double?,
        poissonMinRandom: Double?,
        arrivalIntervalId: String
    ): Observable<String> {
        return Observable.create { emitter ->
            observePoissonArrivals(
                emitter,
                arrivalRate,
                poissonMinRandom,
                arrivalIntervalId
            )
        }
    }

    /**
     * Observe Poisson arrival events recursively scheduling next Poisson arrival after processing the previous one
     *
     * @param emitter Poisson arrival event consumer
     * @param arrivalRate arrival rate which is used to determine the next Poisson arrival
     * @param poissonMinRandom min random value which can be used to achieve smaller intervals between Poisson arrivals
     * @param arrivalIntervalId arrival interval reference id
     */
    private fun observePoissonArrivals(
        emitter: ObservableEmitter<String>,
        arrivalRate: Double?,
        poissonMinRandom: Double?,
        arrivalIntervalId: String
    ) {
        if (emitter.isDisposed) {
            return
        }
        val nextPoissonArrival = if (poissonMinRandom == null) {
            SchedulerUtils.observeNextPoissonArrival(
                arrivalRate
                    ?: 1.0
            )
        } else {
            SchedulerUtils.observeNextPoissonArrival(arrivalRate ?: 1.0, poissonMinRandom)
        }
        nextPoissonArrival
            .map { arrivalIntervalId }
            .doAfterNext { observePoissonArrivals(emitter, arrivalRate, poissonMinRandom, arrivalIntervalId) }
            .subscribe { id ->
                if (!emitter.isDisposed) {
                    emitter.onNext(id)
                }
            }
    }

    /**
     * Check if arrival interval should be treated as Poisson arrivals
     */
    private fun isPoissonArrival(arrivalDefinition: StressyArrivalDefinition) =
        arrivalDefinition.poissonArrival != null && arrivalDefinition.poissonArrival == true

    private fun isRandomArrival(arrivalDefinition: StressyArrivalDefinition) =
        arrivalDefinition.randomizeArrival != null && arrivalDefinition.randomizeArrival == true

    private fun toPeriod(rate: Double) = (1000 / rate).toLong()
    private fun <T> Observable<T>.takeForDuration(arrivalDefinition: StressyArrivalDefinition) : Observable<T> {
        val delay = parseDuration(arrivalDefinition.delay ?: "0ms").toMilliseconds()
        return this.take(
            delay + parseDuration(
                arrivalDefinition.duration
                    ?: throw IllegalStateException("Arrival interval duration is not specified for $arrivalDefinition")
            ).toMilliseconds(), TimeUnit.MILLISECONDS
        )
    }

}