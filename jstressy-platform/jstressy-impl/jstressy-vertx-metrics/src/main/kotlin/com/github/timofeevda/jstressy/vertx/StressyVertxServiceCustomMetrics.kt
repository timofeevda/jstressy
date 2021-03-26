package com.github.timofeevda.jstressy.vertx

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.vertx.VertxService
import com.github.timofeevda.jstressy.utils.StressyUtils
import com.github.timofeevda.jstressy.vertx.metrics.StressyVertexMetricsOptions
import io.vertx.core.VertxOptions
import io.vertx.reactivex.core.Vertx
import java.util.concurrent.TimeUnit

/**
 * The [VertxService] version which can be used for providing Vert.X with custom HTTP client metrics implementation
 */
open class StressyVertxServiceCustomMetrics(private val metricsRegistry: MetricsRegistry) : VertxService {
    override val vertx: Vertx
        get() {
            return Vertx.vertx(
                VertxOptions()
                .setWarningExceptionTime(StressyUtils.getBlockedEventLoopThreadTimeout().toMilliseconds())
                .setWarningExceptionTimeUnit(TimeUnit.MILLISECONDS)
                .setMetricsOptions(
                    StressyVertexMetricsOptions()
                    .setMetricsRegistry(metricsRegistry))
            )
        }

}