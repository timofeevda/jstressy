package com.github.timofeevda.jstressy.metrics.micrometer

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.type.Counter
import com.github.timofeevda.jstressy.api.metrics.type.Gauge
import com.github.timofeevda.jstressy.api.metrics.type.Timer
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.prometheus.PrometheusRenameFilter
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.function.Supplier

/**
 * Metrics registry based on micrometer library with Prometheus metrics format
 *
 * @author dtimofeev
 */
class MicrometerMetricsRegistry internal constructor() : MetricsRegistry {

    internal var prometheusRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    init {
        prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        prometheusRegistry.config()
                .meterFilter(PrometheusRenameFilter())
                .meterFilter(object : MeterFilter {
                    override fun configure(id: Meter.Id?, config: DistributionStatisticConfig): DistributionStatisticConfig? {
                        return DistributionStatisticConfig.builder()
                                .percentiles(0.5, 0.75, 0.95)
                                .build()
                                .merge(config)
                    }
                })
        JvmMemoryMetrics().bindTo(prometheusRegistry)
        JvmGcMetrics().bindTo(prometheusRegistry)
        JvmThreadMetrics().bindTo(prometheusRegistry)
        ClassLoaderMetrics().bindTo(prometheusRegistry)
        LogbackMetrics().bindTo(prometheusRegistry)
        ProcessorMetrics().bindTo(prometheusRegistry)
        UptimeMetrics().bindTo(prometheusRegistry)
        FileDescriptorMetrics().bindTo(prometheusRegistry)
    }

    override fun counter(name: String): Counter {
        val counter = prometheusRegistry.counter(name)
        return object : Counter {
            override fun inc() {
                counter.increment()
            }
        }
    }

    override fun timer(name: String): Timer {
        val timer = prometheusRegistry.timer(name)

        return object : Timer {
            override fun context(): Timer.Context {
                val start = System.nanoTime()
                return object : Timer.Context {
                    override fun stop() {
                        timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS)
                    }
                }
            }

            override fun record(duration: Long, timeUnit: TimeUnit) {
                timer.record(duration, timeUnit)
            }
        }
    }

    override fun gauge(name: String, valueSupplier: Supplier<Double>): Gauge {
        prometheusRegistry.gauge(name, valueSupplier) { v -> v.get() }
        return object : Gauge {
            override val value: Double
                get() = valueSupplier.get()
        }
    }

    override fun gauge(name: String, ref: Any, valueSupplier: Function<Any, Double>): Gauge {
        prometheusRegistry.gauge(name, ref) { value -> valueSupplier.apply(value) }
        return object : Gauge {
            override val value: Double
                get() = valueSupplier.apply(ref)
        }
    }
}
