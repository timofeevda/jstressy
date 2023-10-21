package com.github.timofeevda.jstressy.standalone

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.scenario.ScenarioSchedulerService
import com.github.timofeevda.jstressy.api.vertx.VertxService

class StandaloneScenarioProvider : ScenarioProvider {
    private lateinit var metricsRegistry: MetricsRegistry
    private lateinit var requestExecutorService: RequestExecutorService
    private lateinit var configurationService: ConfigurationService
    private lateinit var scenarioSchedulerService: ScenarioSchedulerService

    override fun get(): Scenario {
        return StandaloneScenario(metricsRegistry, requestExecutorService.get(), scenarioSchedulerService)
    }

    override fun init(metricsRegistry: MetricsRegistry,
                      requestExecutorService: RequestExecutorService,
                      configurationService: ConfigurationService,
                      scenarioSchedulerService: ScenarioSchedulerService,
                      vertxService: VertxService
    ) {
        this.metricsRegistry = metricsRegistry
        this.requestExecutorService = requestExecutorService
        this.configurationService = configurationService
        this.scenarioSchedulerService = scenarioSchedulerService
    }
}
