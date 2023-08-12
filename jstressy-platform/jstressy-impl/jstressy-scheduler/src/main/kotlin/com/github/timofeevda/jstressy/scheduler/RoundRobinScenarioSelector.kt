package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import io.reactivex.Observable
import java.util.*

class RoundRobinScenarioSelector(
    actionDefinition: ScenarioActionDefinition,
    observeScenarioActionArrivals: Observable<String>
) : AbstractDistributedActionSelector(actionDefinition, observeScenarioActionArrivals) {

    private val scenarios: Queue<ScenarioActionReceiver> = LinkedList()

    @Synchronized
    override fun chooseScenarioActionReceiver(): ScenarioActionReceiver? {
        var scenario: ScenarioActionReceiver? = null

        var searchAttempts = 0

        while (scenario == null && scenarios.isNotEmpty() && searchAttempts <= scenarios.size) {
            scenario = scenarios.poll()?.let { if (it.scenario.isAvailableForActionDistribution()) it else null }
            searchAttempts++
        }

        if (scenario != null) {
            scenarios.add(scenario)
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