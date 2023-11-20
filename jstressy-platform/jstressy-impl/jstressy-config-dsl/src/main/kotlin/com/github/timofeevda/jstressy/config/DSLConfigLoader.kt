package com.github.timofeevda.jstressy.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.timofeevda.jstressy.config.dsl.DSLContext
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
            logger.info("DSL config file exists. Reading configuration from DSL file ${dslConfigFile.absolutePath}")
            evaluateDSLAndWriteConfigFile(dslConfigFile, configurationFolder)
        } else {
            super.readConfiguration(configurationFolder)
        }
    }

    fun evaluateDSLAndWriteConfigFile(dslConfigFile: File, configurationFolder: String, printToSTDOut: Boolean = false) {
        val compilationErrorsLogMessage = "Couldn't evaluate configuration DSL script. Please, check compilation reports in logs"
        when (val result = evalConfigDSL(dslConfigFile)) {
            is ResultWithDiagnostics.Failure -> {
                logEvaluationReports(result, printToSTDOut)
                throw IllegalStateException(compilationErrorsLogMessage)
            }

            is ResultWithDiagnostics.Success -> {
                logEvaluationReports(result, printToSTDOut)
                when (val returnValue = result.value.returnValue) {
                    is ResultValue.Error -> throw IllegalStateException(
                        "Couldn't evaluate configuration DSL script",
                        returnValue.error
                    )
                    is ResultValue.NotEvaluated -> throw IllegalStateException(compilationErrorsLogMessage)
                    is ResultValue.Value, is ResultValue.Unit -> {
                        if (DSLContext.config == null) {
                            throw IllegalStateException("DSL script evaluated to null value. Check your script")
                        }
                        processConfiguration(DSLContext.config as Config)
                    }
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
                    logger.error("DSL compilation report: $it.message", it.exception)
                }
                if (it.severity == ScriptDiagnostic.Severity.DEBUG
                    || it.severity == ScriptDiagnostic.Severity.WARNING
                    || it.severity == ScriptDiagnostic.Severity.INFO
                ) {
                    logger.info("DSL compilation report: $it.message")
                }
            }
        }
    }

    private fun evalConfigDSL(dslConfigFile: File) : ResultWithDiagnostics<EvaluationResult> {
        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<StressyConfigScriptDefinition>()
        return BasicJvmScriptingHost().eval(dslConfigFile.toScriptSource(), compilationConfiguration, null)
    }

}
