package com.github.timofeevda.jstressy.config.dsl

import com.github.timofeevda.jstressy.config.parameters.Config

/**
 * "Static" context for exposing config in DSL evaluator, so that DSL doesn't require to return config instance
 * as a last expression
 */
object DSLContext {
    var config: Config? = null
}

fun config(init: Config.() -> Unit) {
    DSLContext.config = Config(init)
}
