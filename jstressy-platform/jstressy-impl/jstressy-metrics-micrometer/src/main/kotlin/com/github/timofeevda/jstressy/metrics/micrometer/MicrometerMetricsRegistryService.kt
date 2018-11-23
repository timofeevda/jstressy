package com.github.timofeevda.jstressy.metrics.micrometer

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistryService
import com.github.timofeevda.jstressy.api.vertx.VertxService
import io.prometheus.client.exporter.common.TextFormat
import io.vertx.reactivex.ext.web.Router

class MicrometerMetricsRegistryService : MetricsRegistryService {

    private val metricsRegistry: MicrometerMetricsRegistry = MicrometerMetricsRegistry()

    override fun get(): MetricsRegistry {
        return metricsRegistry
    }

    fun publishMetrics(vertxService: VertxService, configurationService: ConfigurationService) {
        val router = Router.router(vertxService.vertx)
        val metricsPath = configurationService.configuration.globals.stressyMetricsPath
        router.get(metricsPath).handler { event ->
            try {
                val metricsData = metricsRegistry.prometheusRegistry.scrape()
                event.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
                        .end(metricsData)
            } catch (e: Exception) {
                event.fail(e)
            }
        }

        val port = configurationService.configuration.globals.stressyMetricsPort
        if (port != null) {
            vertxService.vertx.createHttpServer().requestHandler { router.accept(it) }.listen(port)
        }
    }
}
