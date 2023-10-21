package com.github.timofeevda.jstressy.config.dsl

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.RefineScriptCompilationConfigurationHandler
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.collectedAnnotations
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.importScripts
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.CompoundDependenciesResolver
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.FileSystemDependenciesResolver
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.dependencies.resolveFromScriptSourceAnnotations
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Import(vararg val paths: String)

@KotlinScript(
    fileExtension = "kts",
    compilationConfiguration = ConfigScriptCompilationConfiguration::class
)
abstract class StressyConfigScriptDefinition

object ConfigScriptCompilationConfiguration : ScriptCompilationConfiguration({

    defaultImports(Import::class, DependsOn::class, Repository::class)

    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true, unpackJarCollections = true)
    }

    // https://youtrack.jetbrains.com/issue/KT-57907
    compilerOptions.append("-Xadd-modules=ALL-MODULE-PATH")

    refineConfiguration{
        onAnnotations(Import::class, DependsOn::class, Repository::class, handler = RefineConfigurationHandler())
    }
})

class RefineConfigurationHandler : RefineScriptCompilationConfigurationHandler {


    private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> =
        processAnnotations(context)

    private fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

        val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile

        val importedSources = annotations.flatMap {
            (it.annotation as? Import)?.paths?.map { sourceName ->
                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            } ?: emptyList()
        }

        val dependencyAnnotations = annotations.filter { it.annotation is DependsOn || it.annotation is Repository }

        if (dependencyAnnotations.isNotEmpty()) {
            return runBlocking {
                resolver.resolveFromScriptSourceAnnotations(dependencyAnnotations)
            }.onSuccess {
                ScriptCompilationConfiguration(context.compilationConfiguration) {
                    if (importedSources.isNotEmpty()) {
                        importScripts.append(importedSources)
                    }
                    dependencies.append(JvmDependency(it))
                }.asSuccess()
            }
        } else {
            return ScriptCompilationConfiguration(context.compilationConfiguration) {
                if (importedSources.isNotEmpty()) {
                    importScripts.append(importedSources)
                }
            }.asSuccess()
        }

    }
}