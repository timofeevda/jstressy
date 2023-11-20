package com.github.timofeevda.jstressy.api.config.parameters

interface ScenarioContext {
    fun put(key: String, value: Any)

    fun get(key: String) : Any?

}