package com.github.timofeevda.jstressy.metrics.micrometer.internal

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.metrics.micrometer.MicrometerMetricsRegistryService
import com.github.timofeevda.jstressy.utils.StressyUtils.observeService
import com.github.timofeevda.jstressy.utils.StressyUtils.serviceAwaitTimeout
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class Activator : BundleActivator {

    companion object {
        private val logger = LoggerFactory.getLogger(Activator::class.java)
    }

    override fun start(context: BundleContext) {
        logger.info("Starting metrics registry service activator")

        val metricsRegistryService = MicrometerMetricsRegistryService()

        observeService<ConfigurationService>(ConfigurationService::class.java.name, context)
                .doOnSubscribe { logger.info("Metric Registry service subscribes on Configuration services") }
                .doOnSuccess { logger.info("Registering metrics registry service") }
                .timeout(serviceAwaitTimeout().toMilliseconds(), TimeUnit.MILLISECONDS)
                .map { configurationService -> metricsRegistryService.setConfigurationService(configurationService) }
                .subscribe(
                        { metricsRegistry ->
                            context.registerService(MetricsRegistryService::class.java.name, metricsRegistry, Hashtable<Any, Any>())
                        },
                        { throwable -> logger.error("Error registering metrics registry service", throwable) }
                )

        observeService<VertxService>(VertxService::class.java.name, context)
                .doOnSubscribe { logger.info("Metric Registry service subscribes on VertX services") }
                .doOnSuccess { logger.info("Initializing Metrics Registry") }
                .timeout(serviceAwaitTimeout().toMilliseconds(), TimeUnit.MILLISECONDS)
                .subscribe(
                        { vertxService -> metricsRegistryService.startServingMetrics(vertxService)},
                        {throwable -> logger.error("Error initializing metrics registry service", throwable)}
                )

    }

    override fun stop(context: BundleContext) {
        logger.info("Stopping metrics registry service")
    }

}
