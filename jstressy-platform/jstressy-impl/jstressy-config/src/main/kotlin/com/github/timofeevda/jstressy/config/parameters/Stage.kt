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
    constructor(init: Stage.() -> Unit): this() {
        init()
    }

    constructor()

    /**
     * Test stage name
     */
    override var name = ""
    /**
     * Name of the scenario used in this test stage
     */
    override var scenarioName: String = ""
    /**
     * Stage invocation delay. Supports basic format like 1m, 1h, 100ms etc.
     */
    override var delay: String = "0ms"
    /**
     * Stage duration. Supports basic format like 1m, 1h, 100ms etc.
     */
    override var duration: String = ""
    /**
     * Rate of scenario invocation
     */
    override var arrivalRate: Double = 1.0
    /**
     * Determines target value of arrival rate.
     *
     * This property can be used to increase or decrease rate of scenario invocation. rampArrivalRate and rampDuration
     * must be set to support this property
     */
    override var rampArrival: Double? = null
    /**
     * Determines how often arrival rate must be adjusted to match target value of arrival rate (rampArrival)
     * in the end of ramp interval
     */
    override var rampArrivalRate: Double? = null
    /**
     * Determines arrival rate adjustment period for achieving target arrival rate (rampArrival). Exists for
     * convenience only, the same thing can be achieved by defining rampArrivalRate which has higher priority
     * than rampArrivalPeriod
     *
     * Note: if rampArrivalRate is defined it has higher priority than rampArrivalPeriod
     *
     * @return arrival rate adjustment period
     */
    override var rampArrivalPeriod: String? = null
    /**
     * Determines time interval within which arrival rate must match target arrival rate (rampArrivalRate)
     */
    override var rampDuration: String? = null
    /**
     * Scenario parameters. Can be used to pass arbitrary parameters for each scenario invocation
     */
    override var scenarioParameters: Map<String, String> = emptyMap()

    /**
     * Scenario provider parameters. Can be used to pass arbitrary parameters for each scenario provider. This can be
     * used for scenario provider initialization logic if it requires some long-running tasks
     */
    override var scenarioProviderParameters: Map<String, String> = emptyMap()

    override var scenariosLimit: Int? = null

    override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()

    override var randomizeArrival: Boolean = false

    override var poissonArrival: Boolean? = null

    override var poissonMinRandom: Double? = null

    override val actions: MutableList<StressScenarioActionDefinition> = mutableListOf()

    fun arrivalInterval(init: ArrivalInterval.() -> Unit) {
        arrivalIntervals.add(ArrivalInterval(init))
    }

    fun action(init: StressScenarioActionDefinition.() -> Unit) {
        actions.add(StressScenarioActionDefinition(init))
    }

    override fun toString(): String {
        return "Stage(name='$name', scenarioName='$scenarioName', delay='$delay', duration='$duration', arrivalRate=$arrivalRate, rampArrival=$rampArrival, rampArrivalRate=$rampArrivalRate, rampArrivalPeriod=$rampArrivalPeriod, rampDuration=$rampDuration, scenarioParameters=$scenarioParameters, scenarioProviderParameters=$scenarioProviderParameters, scenariosLimit=$scenariosLimit, arrivalIntervals=$arrivalIntervals, randomizeArrival=$randomizeArrival, poissonArrival=$poissonArrival, poissonMinRandom=$poissonMinRandom, actions=$actions)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stage

        if (name != other.name) return false
        if (scenarioName != other.scenarioName) return false
        if (delay != other.delay) return false
        if (duration != other.duration) return false
        if (arrivalRate != other.arrivalRate) return false
        if (rampArrival != other.rampArrival) return false
        if (rampArrivalRate != other.rampArrivalRate) return false
        if (rampArrivalPeriod != other.rampArrivalPeriod) return false
        if (rampDuration != other.rampDuration) return false
        if (scenarioParameters != other.scenarioParameters) return false
        if (scenarioProviderParameters != other.scenarioProviderParameters) return false
        if (scenariosLimit != other.scenariosLimit) return false
        if (arrivalIntervals != other.arrivalIntervals) return false
        if (randomizeArrival != other.randomizeArrival) return false
        if (poissonArrival != other.poissonArrival) return false
        if (poissonMinRandom != other.poissonMinRandom) return false
        return actions == other.actions
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + scenarioName.hashCode()
        result = 31 * result + delay.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + arrivalRate.hashCode()
        result = 31 * result + (rampArrival?.hashCode() ?: 0)
        result = 31 * result + (rampArrivalRate?.hashCode() ?: 0)
        result = 31 * result + (rampArrivalPeriod?.hashCode() ?: 0)
        result = 31 * result + (rampDuration?.hashCode() ?: 0)
        result = 31 * result + scenarioParameters.hashCode()
        result = 31 * result + scenarioProviderParameters.hashCode()
        result = 31 * result + (scenariosLimit ?: 0)
        result = 31 * result + arrivalIntervals.hashCode()
        result = 31 * result + randomizeArrival.hashCode()
        result = 31 * result + (poissonArrival?.hashCode() ?: 0)
        result = 31 * result + (poissonMinRandom?.hashCode() ?: 0)
        result = 31 * result + actions.hashCode()
        return result
    }


}
