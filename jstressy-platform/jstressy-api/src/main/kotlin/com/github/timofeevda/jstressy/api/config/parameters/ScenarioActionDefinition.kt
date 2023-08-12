package com.github.timofeevda.jstressy.api.config.parameters

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

}