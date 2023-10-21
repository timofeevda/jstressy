package com.github.timofeevda.jstressy.standalone

import com.github.structlog4j.StructLog4J
import com.github.structlog4j.json.JsonFormatter
import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Runner(
    private val configService: ConfigurationService,
    private val vertxService: VertxService,
    private val metricsRegistryService: MetricsRegistryService,
    private val scenarioRegistry: ScenarioRegistry,
    private val scenarioScheduler: ScenarioScheduler
) : ApplicationRunner {

    companion object : LazyLogging()
    override fun run(args: ApplicationArguments?) {
        logger.info("Registering JSON log formatting")
        StructLog4J.setFormatter(JsonFormatter.getInstance())

        metricsRegistryService.setConfigurationService(configService)
        metricsRegistryService.startServingMetrics(vertxService)

        configService.configuration.stressPlan.stages
            .map { it.scenarioName }
            .forEach {
               registerStandaloneScenarioProviderService(it)
            }


        scenarioScheduler.observeScenariosWithActions()
            .subscribe(
                { it.scenario.start(it.actions) },
                { error -> logger.error("Error in scenarios stream", error) })
    }

    private fun registerStandaloneScenarioProviderService(scenarioName: String) {
        scenarioRegistry.registerScenarioProviderService(scenarioName, object : ScenarioProviderService {
            override val scenarioName = scenarioName

            override fun get(scenarioProviderParameters: Map<String, String>) = StandaloneScenarioProvider()

        });
    }
}

fun main(args: Array<String>) {
    runApplication<Runner>(*args) {
    }
}

