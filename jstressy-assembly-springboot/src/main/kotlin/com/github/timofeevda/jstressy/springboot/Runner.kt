package com.github.timofeevda.jstressy.springboot

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.scenario.ScenarioProvider
import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.dummy.scenario.TouchGoogleScenarioProvider
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Application demonstrating ability to run JStressy using Spring Boot
 */
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
        metricsRegistryService.setConfigurationService(configService)
        metricsRegistryService.startServingMetrics(vertxService)

        val demoScenario = "TouchGoogle"
        scenarioRegistry.registerScenarioProviderService(
                demoScenario, object : ScenarioProviderService {
            override val scenarioName: String
                get() = demoScenario

            override fun get(scenarioProviderParameters: Map<String, String>): ScenarioProvider {
                return TouchGoogleScenarioProvider()
            }
        })

        scenarioScheduler.observeScenarios()
                .subscribe(
                        { it.start() },
                        { error -> logger.error("Error in scenarios stream", error) })
    }

}

fun main(args: Array<String>) {
    runApplication<Runner>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}