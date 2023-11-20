package com.github.timofeevda.jstressy.config.parameters

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.timofeevda.jstressy.api.config.parameters.ActionDistributionMode
import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.ScenarioHandle


@JsonPropertyOrder("name", "delay", "duration", "arrivalRate", "rampArrival", "rampArrivalRate",
    "rampArrivalPeriod", "rampDuration", "poissonArrival", "poissonMinRandom", "randomizeArrival", "distributionMode",
    "actionParameters", "arrivalIntervals")
class StressScenarioActionDefinition : ScenarioActionDefinition {

    constructor(init: StressScenarioActionDefinition.() -> Unit): this() {
        init()
    }

    constructor()

    override var name: String = ""

    override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()

    override var randomizeArrival: Boolean = false

    override var actionParameters: Map<String, String> = emptyMap()

    override var arrivalRate: Double = 1.0

    override var rampArrival: Double? = null

    override var rampArrivalRate: Double? = null

    override var rampArrivalPeriod: String? = null

    override var rampDuration: String? = null

    override var poissonArrival: Boolean? = null

    override var poissonMinRandom: Double? = null

    override var duration: String? = null

    override var delay: String = "0ms"

    override var distributionMode: ActionDistributionMode? = null

    override var run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)? = null

    fun arrivalInterval(init: ArrivalInterval.() -> Unit) {
        arrivalIntervals.add(ArrivalInterval(init))
    }

    override fun toString(): String {
        return "StressScenarioActionDefinition(name='$name', arrivalIntervals=$arrivalIntervals, randomizeArrival=$randomizeArrival, actionParameters=$actionParameters, arrivalRate=$arrivalRate, rampArrival=$rampArrival, rampArrivalRate=$rampArrivalRate, rampArrivalPeriod=$rampArrivalPeriod, rampDuration=$rampDuration, poissonArrival=$poissonArrival, poissonMinRandom=$poissonMinRandom, duration=$duration, delay='$delay')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StressScenarioActionDefinition

        if (name != other.name) return false
        if (arrivalIntervals != other.arrivalIntervals) return false
        if (randomizeArrival != other.randomizeArrival) return false
        if (actionParameters != other.actionParameters) return false
        if (arrivalRate != other.arrivalRate) return false
        if (rampArrival != other.rampArrival) return false
        if (rampArrivalRate != other.rampArrivalRate) return false
        if (rampArrivalPeriod != other.rampArrivalPeriod) return false
        if (rampDuration != other.rampDuration) return false
        if (poissonArrival != other.poissonArrival) return false
        if (poissonMinRandom != other.poissonMinRandom) return false
        if (duration != other.duration) return false
        if (delay != other.delay) return false
        return distributionMode == other.distributionMode
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + arrivalIntervals.hashCode()
        result = 31 * result + randomizeArrival.hashCode()
        result = 31 * result + actionParameters.hashCode()
        result = 31 * result + arrivalRate.hashCode()
        result = 31 * result + (rampArrival?.hashCode() ?: 0)
        result = 31 * result + (rampArrivalRate?.hashCode() ?: 0)
        result = 31 * result + (rampArrivalPeriod?.hashCode() ?: 0)
        result = 31 * result + (rampDuration?.hashCode() ?: 0)
        result = 31 * result + (poissonArrival?.hashCode() ?: 0)
        result = 31 * result + (poissonMinRandom?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + delay.hashCode()
        result = 31 * result + (distributionMode?.hashCode() ?: 0)
        return result
    }

}