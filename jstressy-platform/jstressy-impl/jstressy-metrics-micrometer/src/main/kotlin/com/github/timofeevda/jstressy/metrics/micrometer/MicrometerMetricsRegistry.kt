package com.github.timofeevda.jstressy.metrics.micrometer

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.metrics.type.Counter
import com.github.timofeevda.jstressy.api.metrics.type.Gauge
import com.github.timofeevda.jstressy.api.metrics.type.Timer
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.prometheus.PrometheusRenameFilter

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * Metrics registry based on micrometer library with Prometheus metrics format
 *
 * @author dtimofeev
 */
class MicrometerMetricsRegistry internal constructor() : MetricsRegistry {

    internal val prometheusRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    private val timers = ConcurrentHashMap<String, Timer>()

    init {
        prometheusRegistry.config().meterFilter(PrometheusRenameFilter())
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
        return timers.computeIfAbsent(name) {createTimer(name)
        }
    }

    private fun createTimer(name: String): Timer {
        val timer = io.micrometer.core.instrument.Timer.builder(name)
                .publishPercentiles(0.5, 0.75, 0.95)
                .register(prometheusRegistry)

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
        val gauge = io.micrometer.core.instrument.Gauge.builder(name, 0.0) { value -> valueSupplier.get() }.register(prometheusRegistry)
        return object : Gauge {
            override val value: Double
                get() = gauge.value()
        }
    }
}
