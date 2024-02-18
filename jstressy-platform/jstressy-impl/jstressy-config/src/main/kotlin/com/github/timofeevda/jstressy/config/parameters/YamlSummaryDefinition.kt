package com.github.timofeevda.jstressy.config.parameters

import com.github.timofeevda.jstressy.api.config.parameters.StressyYamlSummaryDefinition

class YamlSummaryDefinition() :
    StressyYamlSummaryDefinition {
    constructor(init: YamlSummaryDefinition.() -> Unit) : this() {
        init()
    }

    override var folder: String = ""
    override var interval: String = "1m"
    override var timeZone: String? = null
    override var dateTimeFormat: String = "yyyyMMdd-HHmmssZ"
    override var filePrefix: String = "metrics-"
}