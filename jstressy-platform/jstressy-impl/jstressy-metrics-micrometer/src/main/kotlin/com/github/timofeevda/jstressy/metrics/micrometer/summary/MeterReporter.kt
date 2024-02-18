package com.github.timofeevda.jstressy.metrics.micrometer.summary

import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.config.NamingConvention
import java.util.function.Predicate
import java.util.stream.Stream

interface MeterReporter {
    fun report(meters: Stream<Meter>)

    fun getMeterId(meter: Meter) =
        meter.id.getConventionName(NamingConvention.dot) + meter.id.getConventionTags(NamingConvention.dot)
            .joinToString(separator = ",", prefix = "{", postfix = "}") {
                it.key + "=" + it.value
            }

}

abstract class AbstractReporter : MeterReporter {

}