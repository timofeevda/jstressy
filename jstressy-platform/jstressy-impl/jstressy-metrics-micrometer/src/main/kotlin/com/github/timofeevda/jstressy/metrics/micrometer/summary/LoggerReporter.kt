package com.github.timofeevda.jstressy.metrics.micrometer.summary

import com.github.timofeevda.jstressy.utils.logging.LazyLogger
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.binder.BaseUnits
import io.micrometer.core.instrument.distribution.ValueAtPercentile
import io.micrometer.core.instrument.util.DoubleFormat
import io.micrometer.core.instrument.util.TimeUtils
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.math.ln
import kotlin.math.pow


class LoggerReporter(private val logger: LazyLogger, private val timeUnit: TimeUnit) : AbstractReporter() {
    override fun report(meters: Stream<Meter>) {
        meters
            .forEach { meter ->
                meter.use({ gauge ->
                    logger.info("${getMeterId(gauge)} value=${hurBaseUnit(gauge.value(), gauge.id.baseUnit)}")
                },
                    { counter ->
                        logger.info("${getMeterId(counter)} value=${hurBaseUnit(counter.count(), counter.id.baseUnit)}")
                    },
                    { timer ->
                        val snapshot = timer.takeSnapshot()
                        val count = snapshot.count()
                        val mean = snapshot.mean(timeUnit)
                        val max = snapshot.max(timeUnit)
                        val percentiles = snapshot.percentileValues()
                        logger.info(
                            "${getMeterId(timer)} count=$count mean=${formatTime(mean, timeUnit)} max=${
                                formatTime(
                                    max,
                                    timeUnit
                                )
                            } ${
                                formatPercentiles(
                                    percentiles, timeUnit
                                )
                            }"
                        )
                    },
                    { summary ->
                        val snapshot = summary.takeSnapshot()
                        val count = snapshot.count()
                        val mean = snapshot.mean(timeUnit)
                        val max = snapshot.max(timeUnit)
                        val baseUnit = summary.id.baseUnit
                        logger.info(
                            "${getMeterId(summary)} count=$count mean=${hurBaseUnit(mean, baseUnit)} max=${
                                hurBaseUnit(
                                    max,
                                    baseUnit
                                )
                            }"
                        )
                    },
                    { taskTimer ->
                        val mean = taskTimer.mean(timeUnit)
                        val max = taskTimer.max(timeUnit)
                        val duration = taskTimer.duration(timeUnit)
                        logger.info(
                            "${getMeterId(taskTimer)} active=${taskTimer.activeTasks()} mean=${
                                formatTime(
                                    mean,
                                    timeUnit
                                )
                            } max=${
                                formatTime(
                                    max, timeUnit
                                )
                            } duration=${formatTime(duration, timeUnit)}"
                        )
                    },
                    { timeGauge ->
                        logger.info("${getMeterId(timeGauge)} value=${formatTime(timeGauge.value(), timeUnit)}")
                    },
                    { functionCounter ->
                        logger.info("${getMeterId(functionCounter)} value=${functionCounter.count()}")
                    },
                    { functionTimer ->
                        val mean = functionTimer.mean(timeUnit)
                        val total = functionTimer.totalTime(timeUnit)
                        logger.info(
                            "${getMeterId(functionTimer)} count=${functionTimer.count()} mean=${
                                formatTime(
                                    mean,
                                    timeUnit
                                )
                            } total=${
                                formatTime(
                                    total, timeUnit
                                )
                            }"
                        )
                    },
                    { _ -> })
            }
    }

    private fun formatTime(time: Double, timeUnit: TimeUnit) =
        TimeUtils.format(Duration.ofNanos(TimeUtils.convert(time, timeUnit, TimeUnit.NANOSECONDS).toLong()))

    private fun formatPercentiles(percentiles: Array<ValueAtPercentile>, timeUnit: TimeUnit) =
        percentiles.joinToString(separator = " ") {
            it.percentile().toString() + "pt=" + formatTime(it.value(timeUnit), timeUnit)
        }

    // see https://stackoverflow.com/a/3758880/510017
    private fun hurByteCount(bytes: Double): String {
        val unit = 1024.0
        if (bytes < unit || bytes.isNaN()) return DoubleFormat.decimalOrNan(bytes) + " B"
        val exp = (ln(bytes) / ln(unit)).toInt()
        val pre = "KMGTPE"[exp - 1].toString() + "i"
        return DoubleFormat.decimalOrNan(bytes / unit.pow(exp.toDouble())) + " " + pre + "B"
    }

    private fun hurBaseUnit(value: Double, baseUnit: String?) =
        if (BaseUnits.BYTES == baseUnit)
            hurByteCount(value)
        else DoubleFormat.decimalOrNan(value) + if (baseUnit != null) " $baseUnit" else ""


}
