package com.github.timofeevda.jstressy.api.config.parameters

interface StressyRenderedMetrics {
    val port: Int
    val host: String
    val useSsl: Boolean
    val folder: String
    val filePrefix: String
    val fileExtension: String
    val cumulative: Boolean
    val period: String
    val uri: String
    val fromParameter: String?
    val toParameter: String?
    val dateTimeFormat: String
    val timeZone: String?
}