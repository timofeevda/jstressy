package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import io.reactivex.Observable
import java.util.*

class RandomScenarioSelector(
    actionDefinition: ScenarioActionDefinition,
    observeScenarioActionArrivals: Observable<String>
) : AbstractDistributedActionSelector(actionDefinition, observeScenarioActionArrivals) {

    private val scenarios = ArrayList<ScenarioActionReceiver>()

    private val random = Random()

    @Synchronized
    override fun chooseScenarioActionReceiver(): ScenarioActionReceiver? {
        var scenario: ScenarioActionReceiver? = null

        var searchAttempt = 0

        while (scenario == null && scenarios.isNotEmpty() && searchAttempt <= scenarios.size) {
            val idx = random.nextInt(scenarios.size)

            scenario = scenarios[idx]

            if (!scenario.scenario.isAvailableForActionDistribution()) {
                scenario = null
            }

            searchAttempt++
        }

        return scenario
    }

    @Synchronized
    override fun addScenario(scenario: ScenarioActionReceiver) {
        scenarios.add(scenario)
    }

    @Synchronized
    override fun removeScenario(scenario: ScenarioActionReceiver) {
        scenarios.remove(scenario)
    }

    @Synchronized
    override fun clear() {
        scenarios.toMutableList().forEach { it.emitter.onComplete() }
        scenarios.clear()
    }

}