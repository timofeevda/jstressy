package com.github.timofeevda.jstressy.metrics.micrometer

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.type.Counter
import com.github.timofeevda.jstressy.api.metrics.type.Gauge
import com.github.timofeevda.jstressy.api.metrics.type.Timer
import com.github.timofeevda.jstressy.metrics.micrometer.summary.SummaryMeterRegistry
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.prometheus.PrometheusRenameFilter
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * Metrics registry based on micrometer library with Prometheus metrics format
 *
 * @author timofeevda
 */
class MicrometerMetricsRegistry internal constructor(configurationService: ConfigurationService) : MetricsRegistry {

    companion object : LazyLogging()

    val prometheusRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val compositeMetricsRegistry: CompositeMeterRegistry = CompositeMeterRegistry(Clock.SYSTEM)

    init {
        if (configurationService.configuration.globals.yamlSummary != null
            || configurationService.configuration.globals.loggerSummary != null
        ) {
            val summaryMeterRegistry = SummaryMeterRegistry(configurationService.configuration.globals)
            compositeMetricsRegistry.add(summaryMeterRegistry)
            compositeMetricsRegistry.add(prometheusRegistry)
        } else {
            compositeMetricsRegistry.add(prometheusRegistry)
        }

        compositeMetricsRegistry.config()
            .meterFilter(PrometheusRenameFilter())
            .meterFilter(object : MeterFilter {
                override fun configure(
                    id: Meter.Id,
                    config: DistributionStatisticConfig
                ): DistributionStatisticConfig? {
                    return DistributionStatisticConfig.builder()
                        .percentiles(0.5, 0.75, 0.95, 0.99)
                        .build()
                        .merge(config)
                }
            })

        // expose system-level metrics only to Prometheus registry, we don't need it in summary registry
        JvmMemoryMetrics().bindTo(prometheusRegistry)
        JvmGcMetrics().bindTo(prometheusRegistry)
        JvmThreadMetrics().bindTo(prometheusRegistry)
        ClassLoaderMetrics().bindTo(prometheusRegistry)
        LogbackMetrics().bindTo(prometheusRegistry)
        ProcessorMetrics().bindTo(prometheusRegistry)
        UptimeMetrics().bindTo(prometheusRegistry)
        FileDescriptorMetrics().bindTo(prometheusRegistry)
    }

    override fun counter(name: String, description: String, vararg tags: String): Counter {
        val counter = io.micrometer.core.instrument.Counter
            .builder(name)
            .description(description)
            .tags(Tags.of(*tags))
            .register(compositeMetricsRegistry)

        return object : Counter {
            override fun inc() {
                counter.increment()
            }

            override fun inc(value: Int) {
                counter.increment(value.toDouble())
            }
        }
    }

    override fun timer(name: String, description: String, vararg tags: String): Timer {
        val timer = io.micrometer.core.instrument.Timer
            .builder(name)
            .description(description)
            .tags(Tags.of(*tags))
            .register(compositeMetricsRegistry)

        return object : Timer {
            override fun context(): Timer.Context {
                val start = System.nanoTime()
                var time = -1L
                return object : Timer.Context {
                    override fun stop() {
                        time = System.nanoTime() - start
                        timer.record(time, TimeUnit.NANOSECONDS)
                    }

                    override fun getRecordedTime() = time
                }
            }

            override fun record(duration: Long, timeUnit: TimeUnit) {
                timer.record(duration, timeUnit)
            }
        }
    }

    override fun gauge(name: String, description: String, valueSupplier: Supplier<Double>, vararg tags: String): Gauge {
        io.micrometer.core.instrument.Gauge.builder(name) { valueSupplier.get() }
            .description(description)
            .tags(Tags.of(*tags)).register(compositeMetricsRegistry)

        return object : Gauge {
            override val value: Double
                get() = valueSupplier.get()
        }
    }

}
