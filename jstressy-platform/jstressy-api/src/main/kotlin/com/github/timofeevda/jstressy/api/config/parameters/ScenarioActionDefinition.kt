package com.github.timofeevda.jstressy.api.config.parameters

import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.ScenarioHandle

interface ScenarioActionDefinition : StressyArrivalDefinition {
    /**
     * Scenario action name
     */
    val name: String

    /**
     * Parameters map to pass into method creating the actual ScenarioAction
     */
    val actionParameters: Map<String, String>

    /**
     * Determines action distribution mode allowing to distribute action invocation among all active scenarios
     */
    val distributionMode: ActionDistributionMode?

    val run: ((requestExecutor: RequestExecutor, metricsRegistry: MetricsRegistry, scenarioHandle: ScenarioHandle) -> Unit)?

}