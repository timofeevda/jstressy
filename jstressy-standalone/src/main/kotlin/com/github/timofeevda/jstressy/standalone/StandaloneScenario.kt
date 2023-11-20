package com.github.timofeevda.jstressy.standalone

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.api.scenario.ScenarioHandle
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.scenario.ScenarioWithActions
import com.github.timofeevda.jstressy.utils.scenario.DefaultScenarioHandle
import io.reactivex.disposables.Disposable

class StandaloneScenario(
    private val metricsRegistry: MetricsRegistry,
    private val requestExecutor: RequestExecutor,
    private val schedulerService: ScenarioSchedulerService
) : Scenario {

    private var actionDistributionId: String? = null

    private val scenarioHandle = DefaultScenarioHandle(this)

    private var actionsDisposable : Disposable? = null

    override fun start(actions: List<ScenarioActionDefinition>) {
        actionsDisposable = schedulerService.observeScenarioActions(ScenarioWithActions(this, actions))
            .subscribe {
                it.run()
            }
    }

    override fun stop() {
        actionsDisposable?.dispose()
    }

    override fun withArrivalInterval(intervalId: String) = this

    override fun withParameters(parameters: Map<String, String>) = this

    override fun createAction(
        action: String,
        parameters: Map<String, String>,
        run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)?,
        intervalId: String
    ): ScenarioAction {
        return object : ScenarioAction {
            override fun run() {
                run?.invoke(requestExecutor, metricsRegistry, scenarioHandle)
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