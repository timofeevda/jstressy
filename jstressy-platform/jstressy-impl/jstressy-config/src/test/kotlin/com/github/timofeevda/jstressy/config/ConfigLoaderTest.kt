package com.github.timofeevda.jstressy.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class ConfigLoaderTest {

    companion object;

    @Test
    fun testFile() {
        val configLoader = ConfigLoader()
        configLoader.readConfigurationFile(ConfigLoaderTest.javaClass.getResource("/stressy.yml").openStream())
        assert(!configLoader.configuration.stressPlan.stages.isEmpty())
    }

    @Test
    fun testConfigFolder() {
        val configLoader = ConfigLoader()
        configLoader.readConfiguration(ConfigLoaderTest.javaClass.getResource("/config_folder").file)
        assert(!configLoader.configuration.stressPlan.stages.isEmpty())
    }
}