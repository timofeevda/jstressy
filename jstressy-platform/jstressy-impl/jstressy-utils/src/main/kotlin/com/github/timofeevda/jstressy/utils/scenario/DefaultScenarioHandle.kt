package com.github.timofeevda.jstressy.utils.scenario

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioContext
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioHandle
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

open class DefaultScenarioHandle(private val scenario: Scenario) : ScenarioHandle {

    private val disposables = mutableListOf<Disposable>()

    private val scenarioContext = object : ScenarioContext {

        private val map = ConcurrentHashMap<String, Any>()

        override fun put(key: String, value: Any) {
            map[key] = value
        }

        override fun get(key: String): Any? = map[key]
    }
    override fun ctx(): ScenarioContext = scenarioContext

    override fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    override fun stop() {
        disposables.forEach { it.dispose() }
        scenario.stop()
    }
}