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

package com.github.timofeevda.jstressy.config.parameters;

import com.github.timofeevda.jstressy.api.config.parameters.StressyGlobals;

/**
 * Global configuration
 *
 * @author timofeevda
 */
public class Globals implements StressyGlobals {
    private int port;
    private String host;
    private int stressyMetricsPort;
    private String stressyMetricsPath;
    private String websocketURI;
    private boolean useSsl = false;
    private boolean insecureSsl = false;
    private int maxConnections = 1000;

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getStressyMetricsPort() {
        return stressyMetricsPort;
    }

    @Override
    public String getStressyMetricsPath() {
        return stressyMetricsPath;
    }

    @Override
    public boolean isUseSsl() {
        return useSsl;
    }

    @Override
    public boolean isInsecureSsl() {
        return insecureSsl;
    }

    @Override
    public int getMaxConnections() {
        return maxConnections;
    }

}
