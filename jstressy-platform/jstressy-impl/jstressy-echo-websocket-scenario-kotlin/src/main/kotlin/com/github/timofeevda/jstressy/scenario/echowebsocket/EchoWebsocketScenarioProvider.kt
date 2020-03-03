package com.github.timofeevda.jstressy.scenario.echowebsocket

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.vertx.VertxService

class EchoWebSocketScenarioProvider : ScenarioProvider {
    private lateinit var metricsRegistry: MetricsRegistry
    private lateinit var requestExecutorService: RequestExecutorService
    private lateinit var configurationService: ConfigurationService

    override fun get(): Scenario {
        return EchoWebSocketScenario(metricsRegistry, requestExecutorService.get(), configurationService)
    }

    override fun init(metricsRegistry: MetricsRegistry,
                      requestExecutorService: RequestExecutorService,
                      configurationService: ConfigurationService,
                      vertxService: VertxService) {
        this.metricsRegistry = metricsRegistry
        this.requestExecutorService = requestExecutorService
        this.configurationService = configurationService
    }
}