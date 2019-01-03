package com.github.timofeevda.jstressy.vertx.metrics.internal

import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import com.github.timofeevda.jstressy.vertx.metrics.StressyVertxMetricsService
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import java.util.*

class Activator : BundleActivator {

    companion object : LazyLogging()

    override fun start(context: BundleContext) {
        logger.info("Starting Vertx Metrics activator")
        context.registerService(StressyVertxMetricsService::class.java.name, StressyVertxMetricsService(), Hashtable<Any, Any>())

    }

    override fun stop(context: BundleContext) {
        logger.info("Stopping Verx Metrics bundle")
    }
}