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
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage

/**
 * Test stage configuration
 *
 * @author timofeevda
 */
@JsonPropertyOrder("name", "scenarioName", "delay", "duration", "arrivalRate", "rampArrival", "rampArrivalRate",
    "rampArrivalPeriod", "rampDuration", "poissonArrival", "poissonMinRandom", "randomizeArrival", "scenariosLimit",
    "scenarioParameters", "scenarioProviderParameters", "arrivalIntervals", "actions")
class Stage : StressyStage {
    /**
     * Test stage name
     */
    override val name = ""
    /**
     * Name of the scenario used in this test stage
     */
    override val scenarioName: String = ""
    /**
     * Stage invocation delay. Supports basic format like 1m, 1h, 100ms etc.
     */
    override val delay: String = "0ms"
    /**
     * Stage duration. Supports basic format like 1m, 1h, 100ms etc.
     */
    override val duration: String = ""
    /**
     * Rate of scenario invocation
     */
    override val arrivalRate: Double = 1.0
    /**
     * Determines target value of arrival rate.
     *
     * This property can be used to increase or decrease rate of scenario invocation. rampArrivalRate and rampDuration
     * must be set to support this property
     */
    override val rampArrival: Double? = null
    /**
     * Determines how often arrival rate must be adjusted to match target value of arrival rate (rampArrival)
     * in the end of ramp interval
     */
    override val rampArrivalRate: Double? = null
    /**
     * Determines arrival rate adjustment period for achieving target arrival rate (rampArrival). Exists for
     * convenience only, the same thing can be achieved by defining rampArrivalRate which has higher priority
     * than rampArrivalPeriod
     *
     * Note: if rampArrivalRate is defined it has higher priority than rampArrivalPeriod
     *
     * @return arrival rate adjustment period
     */
    override val rampArrivalPeriod: String? = null
    /**
     * Determines time interval within which arrival rate must match target arrival rate (rampArrivalRate)
     */
    override val rampDuration: String? = null
    /**
     * Scenario parameters. Can be used to pass arbitrary parameters for each scenario invocation
     */
    override val scenarioParameters: Map<String, String> = emptyMap()

    /**
     * Scenario provider parameters. Can be used to pass arbitrary parameters for each scenario provider. This can be
     * used for scenario provider initialization logic if it requires some long-running tasks
     */
    override val scenarioProviderParameters: Map<String, String> = emptyMap()

    override val scenariosLimit: Int? = null

    override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()

    override var randomizeArrival: Boolean = false

    override val poissonArrival: Boolean? = null

    override val poissonMinRandom: Double? = null

    override val actions: List<StressScenarioActionDefinition> = emptyList()
    override fun toString(): String {
        return "Stage(name='$name', scenarioName='$scenarioName', delay='$delay', duration='$duration', arrivalRate=$arrivalRate, rampArrival=$rampArrival, rampArrivalRate=$rampArrivalRate, rampArrivalPeriod=$rampArrivalPeriod, rampDuration=$rampDuration, scenarioParameters=$scenarioParameters, scenarioProviderParameters=$scenarioProviderParameters, scenariosLimit=$scenariosLimit, arrivalIntervals=$arrivalIntervals, randomizeArrival=$randomizeArrival, poissonArrival=$poissonArrival, poissonMinRandom=$poissonMinRandom, actions=$actions)"
    }


}
