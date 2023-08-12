package com.github.timofeevda.jstressy.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.timofeevda.jstressy.config.dsl.StressyConfigScriptDefinition
import com.github.timofeevda.jstressy.config.parameters.Config
import java.io.File
import java.io.IOException
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

const val STRESSY_KTS = "stressy.kts"

/**
 * Example implementation of configuration service. Reads JStressy configuration from YAML file
 *
 * @author timofeevda
 */
open class DSLConfigLoader : ConfigLoader() {

    private val yamlMapper = YAMLMapper.builder(
        YAMLFactory()
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    ).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true).build()

    init {
        val configurationModule = SimpleModule("StressyConfiguration", Version.unknownVersion())
        configurationModule.setAbstractTypes(typeResolver)

        yamlMapper.registerModule(configurationModule)
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    @Throws(IOException::class)
    override fun readConfiguration(configurationFolder: String) {
        this.configurationFolder = configurationFolder
        val dslConfigFile = File(configurationFolder + File.separator + STRESSY_KTS)

        if (dslConfigFile.exists()) {
            evaluateDSLAndWriteConfigFile(dslConfigFile, configurationFolder)
        } else {
            super.readConfiguration(configurationFolder)
        }
    }

    fun evaluateDSLAndWriteConfigFile(dslConfigFile: File, configurationFolder: String, printToSTDOut: Boolean = false) {
        when (val result = evalConfigDSL(dslConfigFile)) {
            is ResultWithDiagnostics.Failure -> {
                logEvaluationReports(result, printToSTDOut)
                throw IllegalStateException("Couldn't evaluate configuration DSL script")
            }

            is ResultWithDiagnostics.Success -> {
                logEvaluationReports(result, printToSTDOut)
                when (val returnValue = result.value.returnValue) {
                    is ResultValue.Value -> processConfiguration(returnValue.value as Config)
                    is ResultValue.Error -> throw IllegalStateException(
                        "Couldn't evaluate configuration DSL script",
                        returnValue.error
                    )

                    ResultValue.NotEvaluated -> throw IllegalStateException("Couldn't evaluate configuration DSL script")
                    is ResultValue.Unit -> throw IllegalStateException("DSL script evaluated to void value. Check your script")
                }
            }
        }
        if (!File(configurationFolder + File.separator + STRESSY_YML).exists()) {
            writeConfigFile(configurationFolder)
        } else if (configuration.globals.overwriteWithDSLGeneratedConfig) {
            writeConfigFile(configurationFolder)
        }
    }

    private fun writeConfigFile(configurationFolder: String) {
        val configFile = File(configurationFolder + File.separator + STRESSY_YML)
        yamlMapper.writeValue(configFile, configuration)
    }

    private fun logEvaluationReports(result: ResultWithDiagnostics<EvaluationResult>, printToSTDOut: Boolean = false) {
        if (printToSTDOut) {
            result.reports.forEach {
                println(it.message)
                it.exception?.printStackTrace()
            }
        } else {
            result.reports.forEach {
                if (it.severity == ScriptDiagnostic.Severity.ERROR
                    || it.severity == ScriptDiagnostic.Severity.FATAL
                ) {
                    logger.error(it.message, it.exception)
                }
                if (it.severity == ScriptDiagnostic.Severity.DEBUG
                    || it.severity == ScriptDiagnostic.Severity.WARNING
                    || it.severity == ScriptDiagnostic.Severity.INFO
                ) {
                    logger.info(it.message)
                }
            }
        }
    }

    private fun evalConfigDSL(dslConfigFile: File) : ResultWithDiagnostics<EvaluationResult> {
        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<StressyConfigScriptDefinition>()
        return BasicJvmScriptingHost().eval(dslConfigFile.toScriptSource(), compilationConfiguration, null)
    }

}