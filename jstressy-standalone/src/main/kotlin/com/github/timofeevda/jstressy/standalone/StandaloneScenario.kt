package com.github.timofeevda.jstressy.standalone

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.scenario.ScenarioWithActions

class StandaloneScenario(private val metricsRegistry: MetricsRegistry,
                         private val requestExecutor: RequestExecutor,
                         private val schedulerService: ScenarioSchedulerService
) : Scenario {

    private var actionDistributionId: String? = null
    override fun start(actions: List<ScenarioActionDefinition>) {
        schedulerService.observeScenarioActions(ScenarioWithActions(this, actions))
            .subscribe {
                it.run()
            }
    }

    override fun stop() {
        // do nothing
    }

    override fun withArrivalInterval(intervalId: String) = this

    override fun withParameters(parameters: Map<String, String>) = this

    override fun createAction(action: String, parameters: Map<String, String>, run: ((metricsRegistry: MetricsRegistry, requestExecutor: RequestExecutor) -> Unit)?, intervalId: String): ScenarioAction {
        return object : ScenarioAction {
            override fun run() {
                run?.invoke(metricsRegistry, requestExecutor)
            }
        }
    }

    override fun withActionDistributionId(id: String): Scenario {
        this.actionDistributionId = id
        return this
    }

    override fun getActionDistributionId() = actionDistributionId

    override fun isAvailableForActionDistribution() = true

}