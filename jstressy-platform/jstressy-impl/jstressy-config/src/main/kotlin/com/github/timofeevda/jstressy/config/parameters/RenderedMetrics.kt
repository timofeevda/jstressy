package com.github.timofeevda.jstressy.config.parameters

import com.github.timofeevda.jstressy.api.config.parameters.StressyRenderedMetrics

class RenderedMetrics() : StressyRenderedMetrics {
    constructor(init: RenderedMetrics.() -> Unit): this() {
        init()
    }

    override var port: Int = -1
    override var host: String = ""
    override var useSsl: Boolean = false
    override var folder: String = ""
    override var filePrefix: String = ""
    override var fileExtension: String = ""
    override var cumulative: Boolean = false
    override var period: String = ""
    override var uri: String = ""
    override var fromParameter: String? = null
    override var toParameter: String? = null
    override var dateTimeFormat: String = "yyyyMMdd-HHmmssZ"
    override var timeZone: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RenderedMetrics

        if (port != other.port) return false
        if (host != other.host) return false
        if (useSsl != other.useSsl) return false
        if (folder != other.folder) return false
        if (filePrefix != other.filePrefix) return false
        if (fileExtension != other.fileExtension) return false
        if (cumulative != other.cumulative) return false
        if (period != other.period) return false
        if (uri != other.uri) return false
        if (fromParameter != other.fromParameter) return false
        if (toParameter != other.toParameter) return false
        if (dateTimeFormat != other.dateTimeFormat) return false
        if (timeZone != other.timeZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = port
        result = 31 * result + host.hashCode()
        result = 31 * result + useSsl.hashCode()
        result = 31 * result + folder.hashCode()
        result = 31 * result + filePrefix.hashCode()
        result = 31 * result + fileExtension.hashCode()
        result = 31 * result + cumulative.hashCode()
        result = 31 * result + period.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + (fromParameter?.hashCode() ?: 0)
        result = 31 * result + (toParameter?.hashCode() ?: 0)
        result = 31 * result + dateTimeFormat.hashCode()
        result = 31 * result + (timeZone?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RenderedMetrics(port=$port, host='$host', useSsl=$useSsl, folder='$folder', filePrefix='$filePrefix', fileExtension='$fileExtension', cumulative=$cumulative, period='$period', uri='$uri', fromParameter=$fromParameter, toParameter=$toParameter, dateTimeFormat='$dateTimeFormat', timeZone=$timeZone)"
    }

}