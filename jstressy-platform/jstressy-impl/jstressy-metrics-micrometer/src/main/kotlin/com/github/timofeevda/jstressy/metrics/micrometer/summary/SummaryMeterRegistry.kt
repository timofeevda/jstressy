package com.github.timofeevda.jstressy.metrics.micrometer.summary

import com.github.timofeevda.jstressy.api.config.parameters.StressyGlobals
import com.github.timofeevda.jstressy.utils.StressyUtils.parseDuration
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import io.micrometer.core.instrument.step.StepMeterRegistry
import io.micrometer.core.instrument.util.NamedThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SummaryMeterRegistry(globals: StressyGlobals) :
    StepMeterRegistry(LoggingRegistryConfig.DEFAULT, Clock.SYSTEM) {

    companion object : LazyLogging()

    private var reporter: MeterReporter? = null

    private val prefixesToSkip = listOf("jvm", "process", "system")

    init {

        if (globals.yamlSummary != null) {
            reporter = YamlSummaryReporter(globals.yamlSummary!!, baseTimeUnit)
            scheduleReporting(globals.yamlSummary!!.interval)
        } else if (globals.loggerSummary != null) {
            reporter = LoggerReporter(logger, baseTimeUnit)
            scheduleReporting(globals.loggerSummary!!.interval)
        }

    }

    private fun scheduleReporting(interval: String) {
        logger.info("Starting summary metrics publishing with $interval interval")

        val intervalMillis = parseDuration(interval).toMilliseconds()
        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("summary-reporter"))
            .scheduleWithFixedDelay({
                try {
                    publish()
                } catch (ex: Exception) {
                    logger.error("Error while publishing summary metrics", ex)
                }
            }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS)
    }

    override fun getBaseTimeUnit(): TimeUnit {
        return TimeUnit.MILLISECONDS
    }

    override fun publish() {
        reporter?.let { rp ->
            val metersToPublish = meters.stream().sorted { m1: Meter, m2: Meter ->
                val typeCompare = m1.id.type.compareTo(m2.id.type)
                if (typeCompare == 0) m1.id.name.compareTo(m2.id.name) else typeCompare
            }.filter { meter ->
                val id = rp.getMeterId(meter)
                prefixesToSkip.none { id.startsWith(it) } && !(id.startsWith("vertx") && id.contains("percentile"))
            }

            rp.report(metersToPublish)
        }

    }
}
