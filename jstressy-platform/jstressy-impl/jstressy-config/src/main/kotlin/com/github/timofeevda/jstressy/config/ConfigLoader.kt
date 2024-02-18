/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Denis Timofeev <timofeevda@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *
 */

package com.github.timofeevda.jstressy.config

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.config.parameters.StressyYamlSummaryDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyArrivalInterval
import com.github.timofeevda.jstressy.api.config.parameters.StressyConfiguration
import com.github.timofeevda.jstressy.api.config.parameters.StressyGlobals
import com.github.timofeevda.jstressy.api.config.parameters.StressyLoggerSummaryDefinition
import com.github.timofeevda.jstressy.api.config.parameters.StressyRenderedMetrics
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage
import com.github.timofeevda.jstressy.api.config.parameters.StressyStressPlan
import com.github.timofeevda.jstressy.config.parameters.ArrivalInterval
import com.github.timofeevda.jstressy.config.parameters.Config
import com.github.timofeevda.jstressy.config.parameters.Globals
import com.github.timofeevda.jstressy.config.parameters.YamlSummaryDefinition
import com.github.timofeevda.jstressy.config.parameters.LoggerSummaryDefinition
import com.github.timofeevda.jstressy.config.parameters.RenderedMetrics
import com.github.timofeevda.jstressy.config.parameters.Stage
import com.github.timofeevda.jstressy.config.parameters.StressPlan
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

const val STRESSY_YML = "stressy.yml"

/**
 * Example implementation of configuration service. Reads JStressy configuration from YAML file
 *
 * @author timofeevda
 */
open class ConfigLoader : ConfigurationService {

    companion object : LazyLogging()

    override var configuration: Config = Config()

    override var configurationFolder: String? = null

    private val mapper = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build()

    protected val typeResolver = SimpleAbstractTypeResolver()
        .addMapping(StressyConfiguration::class.java, Config::class.java)
        .addMapping(StressyGlobals::class.java, Globals::class.java)
        .addMapping(StressyStage::class.java, Stage::class.java)
        .addMapping(StressyStressPlan::class.java, StressPlan::class.java)
        .addMapping(StressyArrivalInterval::class.java, ArrivalInterval::class.java)
        .addMapping(StressyRenderedMetrics::class.java, RenderedMetrics::class.java)
        .addMapping(StressyLoggerSummaryDefinition::class.java, LoggerSummaryDefinition::class.java)
        .addMapping(StressyYamlSummaryDefinition::class.java, YamlSummaryDefinition::class.java)

    init {
        val configurationModule = SimpleModule("StressyConfiguration", Version.unknownVersion())
        configurationModule.setAbstractTypes(typeResolver)

        mapper.registerModule(configurationModule)

    }

    @Throws(IOException::class)
    override fun readConfiguration(configurationFolder: String) {
        this.configurationFolder = configurationFolder
        val configFile = File(configurationFolder + File.separator + STRESSY_YML)
        readConfigurationFile(FileInputStream(configFile))
    }

    internal fun readConfigurationFile(configFileStream: InputStream) {
        processConfiguration(mapper.readValue(configFileStream, Config::class.java))
    }

    protected fun processConfiguration(config: Config) {
        configuration = config
    }

}
