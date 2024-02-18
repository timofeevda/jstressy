package com.github.timofeevda.jstressy.api.config.parameters

interface StressyYamlSummaryDefinition : StressyMetricsSummaryDefinition {
    var folder: String
    var timeZone: String?
    var dateTimeFormat: String
    var filePrefix: String
}