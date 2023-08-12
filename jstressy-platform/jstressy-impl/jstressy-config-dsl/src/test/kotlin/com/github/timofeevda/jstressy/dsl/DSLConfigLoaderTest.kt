package com.github.timofeevda.jstressy.dsl

import com.github.timofeevda.jstressy.config.ConfigLoader
import com.github.timofeevda.jstressy.config.DSLConfigLoader
import com.github.timofeevda.jstressy.config.STRESSY_KTS
import org.junit.jupiter.api.Test
import java.io.File

class DSLConfigLoaderTest {

    @Test
    fun dslCompiledToYMLConfigurationFile() {
        val configLoader = DSLConfigLoader()
        val configFolder = DSLConfigLoaderTest::class.java.getResource("/config_folder")!!.file
        val dslConfigFile = File(configFolder + File.separator + STRESSY_KTS)
        configLoader.evaluateDSLAndWriteConfigFile(dslConfigFile, configFolder, true)

        val referenceFileConfigLoader = ConfigLoader()
        referenceFileConfigLoader.readConfiguration(DSLConfigLoaderTest::class.java.getResource("/reference_config_folder")!!.file)

        referenceFileConfigLoader.configuration

        val dslGeneratedFileConfigLoader = ConfigLoader()
        dslGeneratedFileConfigLoader.readConfiguration(DSLConfigLoaderTest::class.java.getResource("/config_folder")!!.file)

        dslGeneratedFileConfigLoader.configuration

        assert(referenceFileConfigLoader.configuration == dslGeneratedFileConfigLoader.configuration) { "DSL generated config must be the same as reference config defined in YAML file" }

    }
}