package com.github.timofeevda.jstressy.springboot

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.httpclient.HttpClientService
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutorService
import com.github.timofeevda.jstressy.api.httpsession.HttpSessionManagerService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.scenario.ScenarioRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.config.ConfigLoader
import com.github.timofeevda.jstressy.cookiesession.CookieSessionManagerServiceImpl
import com.github.timofeevda.jstressy.httpclient.StressyHttpClientService
import com.github.timofeevda.jstressy.httprequest.StressyRequestExecutorService
import com.github.timofeevda.jstressy.metrics.micrometer.MicrometerMetricsRegistryService
import com.github.timofeevda.jstressy.scenario.registry.StressyScenarioRegistry
import com.github.timofeevda.jstressy.scheduler.StressyScenariosScheduler
import com.github.timofeevda.jstressy.vertx.StressyVertxService
import org.springframework.stereotype.Service
import java.io.IOException
import javax.annotation.PostConstruct

@Service
class ConfigurationService() : ConfigLoader() {
    @PostConstruct
    fun init() {
        val configFolder = System.getProperty("configFolder")
        logger.info("Reading configuration from folder $configFolder")
        try {
            readConfiguration(configFolder)
        } catch (e: IOException) {
            logger.error("Error reading configuration file", e)
        }
    }
}

@Service
class VertxService(metricsRegistryService: MicrometerMetricsRegistryService) : StressyVertxService(metricsRegistryService)

@Service
class HttpSessionManager : CookieSessionManagerServiceImpl()

@Service
class HTTPClient(
        vertxService: VertxService,
        configurationService: ConfigurationService)
    : StressyHttpClientService(vertxService, configurationService)

@Service
class HTTPRequestExecutorService(
        httpClientService: HttpClientService,
        metricsRegistryService: MetricsRegistryService,
        httpSessionManagerService: HttpSessionManagerService)
    : StressyRequestExecutorService(httpClientService, metricsRegistryService, httpSessionManagerService)

@Service
class ScenarioRegistry : StressyScenarioRegistry()

@Service
class ScenarioScheduler(
        vertxService: VertxService,
        requestExecutorService: RequestExecutorService,
        metricsRegistryService: MetricsRegistryService,
        configurationService: ConfigurationService,
        scenarioRegistryService: ScenarioRegistryService)
    : StressyScenariosScheduler(vertxService, requestExecutorService, configurationService, metricsRegistryService, scenarioRegistryService)

@Service
class MetricsRegistryService : MicrometerMetricsRegistryService()