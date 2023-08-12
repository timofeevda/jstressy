package com.github.timofeevda.jstressy.api.scenario

/**
 * Scenario action that can be run within particular scenario
 */
interface ScenarioAction {
    /**
     * Run scenario action
     */
    fun run()
}