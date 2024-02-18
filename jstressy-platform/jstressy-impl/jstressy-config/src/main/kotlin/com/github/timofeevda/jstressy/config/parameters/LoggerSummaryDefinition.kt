package com.github.timofeevda.jstressy.config.parameters

import com.github.timofeevda.jstressy.api.config.parameters.StressyLoggerSummaryDefinition

class LoggerSummaryDefinition() :
    StressyLoggerSummaryDefinition {

    constructor(init: LoggerSummaryDefinition.() -> Unit) : this() {
        init()
    }

    override var interval: String = "1m"
}