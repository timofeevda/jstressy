package com.github.timofeevda.jstressy.scenario.echowebsocket

import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService

class EchoWebSocketScenarioProviderService : ScenarioProviderService {
    override val scenarioName: String = "EchoWebSocket"

    override fun get(scenarioProviderParameters: Map<String, String>): ScenarioProvider {
        return EchoWebSocketScenarioProvider()
    }

}