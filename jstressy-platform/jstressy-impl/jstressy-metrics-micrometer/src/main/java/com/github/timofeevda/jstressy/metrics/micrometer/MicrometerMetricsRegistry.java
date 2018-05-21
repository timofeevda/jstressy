package com.github.timofeevda.jstressy.metrics.micrometer;

import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry;
import com.github.timofeevda.jstressy.api.metrics.type.Counter;
import com.github.timofeevda.jstressy.api.metrics.type.Gauge;
import com.github.timofeevda.jstressy.api.metrics.type.Timer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.prometheus.PrometheusRenameFilter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Metrics registry based on micrometer library with Prometheus metrics format
 *
 * @author dtimofeev
 */
public class MicrometerMetricsRegistry implements MetricsRegistry {

    private final PrometheusMeterRegistry prometheusRegistry;

    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    MicrometerMetricsRegistry() {
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        prometheusRegistry.config().meterFilter(new PrometheusRenameFilter());
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new LogbackMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new UptimeMetrics().bindTo(prometheusRegistry);
        new FileDescriptorMetrics().bindTo(prometheusRegistry);
    }

    @Override
    public Counter counter(String name) {
        io.micrometer.core.instrument.Counter counter = prometheusRegistry.counter(name);
        return new Counter() {
            @Override
            public void inc() {
                counter.increment();
            }

            @Override
            public void dec() {
                throw new UnsupportedOperationException("Micrometer's counter can't be decremented. Use gauge instead");
            }
        };
    }

    @Override
    public Timer timer(String name) {
        return timers.computeIfAbsent(name, s -> {
            io.micrometer.core.instrument.Timer timer = io.micrometer.core.instrument.Timer.builder(name)
                    .publishPercentiles(0.5, 0.75, 0.95)
                    .register(prometheusRegistry);
            return new Timer() {
                @Override
                public Context context() {
                    long start = System.nanoTime();
                    return () -> timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                }

                @Override
                public void record(long duration, TimeUnit timeUnit) {
                    timer.record(duration, timeUnit);
                }
            };
        });
    }

    @Override
    public Gauge gauge(String name, Supplier<Double> valueSupplier) {
        io.micrometer.core.instrument.Gauge gauge = io.micrometer.core.instrument.Gauge.builder(name, 0., value -> valueSupplier.get()).register(prometheusRegistry);
        return gauge::value;
    }

    PrometheusMeterRegistry getPrometheusRegistry() {
        return prometheusRegistry;
    }
}
