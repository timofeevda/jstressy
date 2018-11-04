package com.github.timofeevda.jstressy.metrics.micrometer.internal

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.metrics.micrometer.MicrometerMetricsRegistryService
import com.github.timofeevda.jstressy.utils.StressyUtils.observeService as observeService
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

class Activator : BundleActivator {

    override fun start(context: BundleContext) {
        logger.info("Starting metrics registry service activator")

        val vertxService = observeService<VertxService>(VertxService::class.java.name, context)
        val configurationService = observeService<ConfigurationService>(ConfigurationService::class.java.name, context)

        Observable.combineLatest<VertxService, ConfigurationService, MetricsRegistryService>(
                vertxService.toObservable(),
                configurationService.toObservable(),
                BiFunction<VertxService, ConfigurationService, MetricsRegistryService> { vxService, configService -> this.toMetricsRegistryService(vxService, configService) })
                .doOnSubscribe { logger.info("Metric Registry service subscribes on VertX and Configuration services") }
                .doOnNext { logger.info("Registering metrics registry service") }
                .timeout(10, TimeUnit.SECONDS)
                .subscribe { metricsRegistryService ->
                    context.registerService(MetricsRegistryService::class.java.name, metricsRegistryService, Hashtable<Any, Any>())
                }
    }

    override fun stop(context: BundleContext) {

    }

    private fun toMetricsRegistryService(vertxService: VertxService, configurationService: ConfigurationService): MetricsRegistryService {
        val metricsRegistryService = MicrometerMetricsRegistryService()
        metricsRegistryService.publishMetrics(vertxService, configurationService)
        return metricsRegistryService
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Activator::class.java)
    }

}
