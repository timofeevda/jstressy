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

package com.github.timofeevda.jstressy.plugin.core;

/**
 *
 * @author timofeevda
 */
public class Constants {
    public static final String ARCHIVE_TYPE_TAR = "tar";
    public static final String ARCHIVE_TYPE_ZIP = "zip";
    public static final String BUNDLES_APPLICATION = "bundles/application/";
    public static final String BUNDLES_SYSTEM = "bundles/system/";
    public static final String BUNDLES_PLUGIN = "bundles/plugins/";
    public static final String CONFIGURATION_FOLDER = "/conf";
    public static final String FELIX_CONFIG = "config.properties";
    public static final String FELIX_GROUP_ID = "org.apache.felix";
    public static final String FELIX_ARTIFACT_ID = "org.apache.felix.main";
    public static final String FELIX_BIN = "bin";
    public static final String PATH_TO_FELIX_PROPERTIES ="conf/config.properties";
    public static final String FELIX_BUNDLES_DEFAULT_PROTOCOL = "file:";
}
