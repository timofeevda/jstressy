package com.github.timofeevda.jstressy.metrics.micrometer.summary

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.timofeevda.jstressy.api.config.parameters.StressyYamlSummaryDefinition
import io.micrometer.core.instrument.Meter
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

private open class Metric(open val name: String)

private data class Percentile(val percentile: String, val value: Double)

private data class Gauge(override val name: String, val value: Double) : Metric(name)

private data class TimeGauge(override val name: String, val value: Double) : Metric(name)

private data class Counter(
    override val name: String,
    val count: Double,
) : Metric(name)

private data class Timer(
    override val name: String,
    val counter: Long,
    val mean: Double,
    val max: Double,
    val percentiles: List<Percentile>
) : Metric(name)

private data class Summary(override val name: String, val count: Long, val mean: Double, val max: Double) : Metric(name)
private data class TaskTimer(
    override val name: String,
    val activeTasks: Int,
    val mean: Double,
    val max: Double,
    val duration: Double
) : Metric(name)

private data class FunctionCounter(
    override val name: String,
    val count: Double,
) : Metric(name)

private data class FunctionTimer(
    override val name: String,
    val count: Double,
    val mean: Double,
    val total: Double
) : Metric(name)

class YamlSummaryReporter(private val summaryDefinition: StressyYamlSummaryDefinition, private val timeUnit: TimeUnit) :
    AbstractReporter() {

    private var lastReport = System.currentTimeMillis();

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(summaryDefinition.dateTimeFormat)
        .withZone(summaryDefinition.timeZone?.let { zone -> ZoneId.of(zone) } ?: ZoneId.systemDefault())

    private val mapper = YAMLMapper.builder().build()

    init {
        val folder = File(summaryDefinition.folder)

        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    override fun report(meters: Stream<Meter>) {
        val to = System.currentTimeMillis()

        val meterList = mutableListOf<Metric>()

        meters.forEach { meter ->
            meter.use({ gauge ->
                meterList.add(Gauge(getMeterId(gauge), gauge.value()))
            },
                { counter ->
                    meterList.add(Counter(getMeterId(counter), counter.count()))
                },
                { timer ->
                    val snapshot = timer.takeSnapshot()
                    meterList.add(Timer(getMeterId(timer), snapshot.count(), snapshot.mean(), snapshot.max(),
                        snapshot.percentileValues().map { Percentile(it.percentile().toString(), it.value(timeUnit)) }
                    ))
                },
                { summary ->
                    val snapshot = summary.takeSnapshot()
                    meterList.add(Summary(getMeterId(summary), snapshot.count(), snapshot.mean(), snapshot.max()))
                },
                { taskTimer ->
                    val mean = taskTimer.mean(timeUnit)
                    val max = taskTimer.max(timeUnit)
                    val duration = taskTimer.duration(timeUnit)

                    meterList.add(TaskTimer(getMeterId(taskTimer), taskTimer.activeTasks(), mean, max, duration))
                },
                { timeGauge ->
                    meterList.add(TimeGauge(getMeterId(timeGauge), timeGauge.value()))
                },
                { functionCounter ->
                    meterList.add(FunctionCounter(getMeterId(functionCounter), functionCounter.count()))
                },
                { functionTimer ->
                    val mean = functionTimer.mean(timeUnit)
                    val total = functionTimer.totalTime(timeUnit)
                    meterList.add(FunctionTimer(getMeterId(functionTimer), functionTimer.count(), mean, total))
                },
                { _ -> }
            )
        }

        val fromDate = dateTimeFormatter.format(Instant.ofEpochMilli(lastReport))
        val toDate = dateTimeFormatter.format(Instant.ofEpochMilli(to))


        writeToFile(meterList, fromDate, toDate)

        lastReport = to

    }

    private fun writeToFile(meterList: List<Metric>, fromDate: String, toDate: String) {
        val yamlFile =
            File(summaryDefinition.folder + File.separator + summaryDefinition.filePrefix + "$fromDate-$toDate" + ".yaml")

        yamlFile.createNewFile()

        mapper.writeValue(yamlFile, meterList)

    }
}