package com.github.timofeevda.jstressy.config.parameters

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.timofeevda.jstressy.api.config.parameters.ActionDistributionMode
import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval


@JsonPropertyOrder("name", "delay", "duration", "arrivalRate", "rampArrival", "rampArrivalRate",
    "rampArrivalPeriod", "rampDuration", "poissonArrival", "poissonMinRandom", "randomizeArrival", "distributionMode",
    "actionParameters", "arrivalIntervals")
class StressScenarioActionDefinition : ScenarioActionDefinition {

    override val name: String = ""

    override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()

    override var randomizeArrival: Boolean = false

    override val actionParameters: Map<String, String> = emptyMap()

    override val arrivalRate: Double = 1.0

    override val rampArrival: Double? = null

    override val rampArrivalRate: Double? = null

    override val rampArrivalPeriod: String? = null

    override val rampDuration: String? = null

    override val poissonArrival: Boolean? = null

    override val poissonMinRandom: Double? = null

    override val duration: String? = null

    override val delay: String = "0ms"

    override var distributionMode: ActionDistributionMode? = null
    override fun toString(): String {
        return "StressScenarioActionDefinition(name='$name', arrivalIntervals=$arrivalIntervals, randomizeArrival=$randomizeArrival, actionParameters=$actionParameters, arrivalRate=$arrivalRate, rampArrival=$rampArrival, rampArrivalRate=$rampArrivalRate, rampArrivalPeriod=$rampArrivalPeriod, rampDuration=$rampDuration, poissonArrival=$poissonArrival, poissonMinRandom=$poissonMinRandom, duration=$duration, delay='$delay')"
    }


}