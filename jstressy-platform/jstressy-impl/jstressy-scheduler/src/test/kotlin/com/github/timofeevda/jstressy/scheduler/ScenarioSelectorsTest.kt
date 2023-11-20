package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.ActionDistributionMode
import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioHandle
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ScenarioSelectorsTest {

    private val actionDefinition = object : ScenarioActionDefinition {
        override val name: String = "action"
        override val actionParameters: Map<String, String> = emptyMap()
        override val distributionMode: ActionDistributionMode = ActionDistributionMode.ROUND_ROBIN
        override val run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)? = null
        override val arrivalRate: Double = 2.0
        override val rampArrival: Double? = null
        override val rampArrivalRate: Double? = null
        override val rampArrivalPeriod: String? = null
        override val rampDuration: String? = null
        override val poissonArrival: Boolean? = null
        override val poissonMinRandom: Double? = null
        override val arrivalIntervals: MutableList<StressyArrivalInterval> = mutableListOf()
        override val duration: String = "3min"
        override val delay: String = "4min"
        override val randomizeArrival: Boolean = false
    }

    @Test
    fun testRoundRobinScenarioSelectionPolicy() {
        val selector = RoundRobinScenarioSelector(actionDefinition, Observable.empty())
        for (i in 1..10) {
            val scenarioMock = mockk<Scenario>()
            every { scenarioMock.getActionDistributionId() } returns i.toString()
            every { scenarioMock.isAvailableForActionDistribution() } returns true
            selector.addScenario(ScenarioActionReceiver(scenarioMock, mockk()))
        }

        iterateScenarios(selector)
        // after first iteration the second one must have the same order
        iterateScenarios(selector)
    }

    @Test
    fun testRandomScenarioSelectionPolicy() {
        val selector = RandomScenarioSelector(actionDefinition, Observable.empty())
        for (i in 1..10) {
            val scenarioMock = mockk<Scenario>()
            every { scenarioMock.getActionDistributionId() } returns i.toString()
            every { scenarioMock.isAvailableForActionDistribution() } returns true
            selector.addScenario(ScenarioActionReceiver(scenarioMock, mockk()))
        }

        for (i in 1..10) {
            val scenarioActionReceiver = selector.chooseScenarioActionReceiver()
            val scenario = scenarioActionReceiver?.scenario
            assertNotNull(scenario, "Scenario should be selected if it were added to selector")
        }
    }

    @Test
    fun testSkipNotAvailableScenario() {
        val selector = RoundRobinScenarioSelector(actionDefinition, Observable.empty())
        for (i in 1..10) {
            val scenarioMock = mockk<Scenario>()
            every { scenarioMock.getActionDistributionId() } returns i.toString()

            // every even scenario is available
            if (i % 2 == 0) {
                every { scenarioMock.isAvailableForActionDistribution() } returns true
            } else {
                every { scenarioMock.isAvailableForActionDistribution() } returns false
            }

            selector.addScenario(ScenarioActionReceiver(scenarioMock, mockk()))
        }

        for (i in 1..5) {
            val scenarioActionReceiver = selector.chooseScenarioActionReceiver()
            val scenario = scenarioActionReceiver?.scenario
            assertEquals(
                scenario!!.getActionDistributionId()!!.toInt(), i * 2,
                "Every even scenario must be selected"
            )
        }
    }

    private fun iterateScenarios(selector: RoundRobinScenarioSelector) {
        for (i in 1..10) {
            val scenarioActionReceiver = selector.chooseScenarioActionReceiver()
            val scenario = scenarioActionReceiver?.scenario
            assertNotNull(scenario, "Scenario should be selected if it were added to selector")
            assertEquals(
                scenario!!.getActionDistributionId()!!.toInt(), i,
                "Scenarios must be selected in correct order according to round-robin policy"
            )
        }
    }
}