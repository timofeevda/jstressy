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

package com.github.timofeevda.jstressy.api.config.parameters

interface StressyArrivalDefinition {
    /**
     * Determines how ofter scenario is invoked
     *
     * @return scenario invocation rate
     */
    val arrivalRate: Double?

    /**
     * Determines target value of arrival rate. Must be used along with rampArrivalRate and rampDuration
     *
     * @return target value of arrival rate
     */
    val rampArrival: Double?

    /**
     * Determines how often arrival rate must be adjusted to match target value of arrival rate (rampArrival)
     * in the end of ramp interval
     *
     * @return arrivalRate adjustment rate
     */
    val rampArrivalRate: Double?

    /**
     * Determines arrival rate adjustment period for achieving target arrival rate (rampArrival). Exists for
     * convenience only, the same thing can be achieved by defining rampArrivalRate which has higher priority
     * than rampArrivalPeriod
     *
     * Note: if rampArrivalRate is defined it has higher priority than rampArrivalPeriod
     *
     * @return arrival rate adjustment period
     */
    val rampArrivalPeriod: String?

    /**
     * Determines time interval within which arrival rate must match target arrival rate (rampArrivalRate)
     *
     * @return arrayRate adjustment interval
     */
    val rampDuration: String?

    /**
     * Denotes if arrival process is Poisson arrival process and should be modelled as random
     * arrivals with corresponding arrival rate
     */
    val poissonArrival: Boolean?

    /**
     * Allows to redefine max random number in Poisson arrivals formula. It can be used to achieve bigger time
     * intervals between arrival invocations
     */
    val poissonMaxRandom: Double?

}