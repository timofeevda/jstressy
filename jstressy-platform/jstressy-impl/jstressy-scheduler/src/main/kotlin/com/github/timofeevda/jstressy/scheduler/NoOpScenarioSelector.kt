package com.github.timofeevda.jstressy.scheduler

import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import io.reactivex.Observable


object NoOpScenarioSelector : DistributedActionSelector {
    override fun observeActions(scenario: Scenario) : Observable<ScenarioAction> = Observable.empty()
}