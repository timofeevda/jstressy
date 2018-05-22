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

package com.github.timofeevda.jstressy.config;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.timofeevda.jstressy.api.config.ConfigurationService;
import com.github.timofeevda.jstressy.api.config.parameters.StressyConfiguration;
import com.github.timofeevda.jstressy.api.config.parameters.StressyGlobals;
import com.github.timofeevda.jstressy.api.config.parameters.StressyStage;
import com.github.timofeevda.jstressy.api.config.parameters.StressyStressPlan;
import com.github.timofeevda.jstressy.config.parameters.Config;
import com.github.timofeevda.jstressy.config.parameters.Globals;
import com.github.timofeevda.jstressy.config.parameters.Stage;
import com.github.timofeevda.jstressy.config.parameters.StressPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Example implementation of configuration service. Reads JStressy configuration from YAML file
 *
 * @author timofeevda
 */
public class ConfigLoader implements ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    public static final String STRESSY_YML = "stressy.yml";

    private Config config;

    private String configurationFolder;

    @Override
    public void readConfiguration(String configurationFolder) {
        this.configurationFolder = configurationFolder;
        try {
            File configFile = new File(configurationFolder + File.separator + STRESSY_YML);

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            SimpleAbstractTypeResolver typeResolver = new SimpleAbstractTypeResolver()
                    .addMapping(StressyConfiguration.class, Config.class)
                    .addMapping(StressyGlobals.class, Globals.class)
                    .addMapping(StressyStage.class, Stage.class)
                    .addMapping(StressyStressPlan.class, StressPlan.class);

            SimpleModule configurationModule = new SimpleModule("StressyConfiguration", Version.unknownVersion());
            configurationModule.setAbstractTypes(typeResolver);

            mapper.registerModule(configurationModule);

            config = mapper.readValue(configFile, Config.class);
        } catch (Exception e) {
            log.error("Error loading configuration file: " + configurationFolder + File.separator + STRESSY_YML, e);
        }
    }

    public StressyConfiguration getConfiguration() {
        return config;
    }

    public String getConfigurationFolder() {
        return configurationFolder;
    }
}
