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

import com.github.timofeevda.jstressy.api.config.parameters.StressyStage;

import java.util.Map;

/**
 * Stage configuration
 *
 * @author timofeevda
 */
public class Stage implements StressyStage {
    private String name = "";
    private String scenarioName;
    private String stageDelay;
    private String stageDuration;
    private double arrivalRate;
    private double rampArrival = -1;
    private double rampArrivalRate = -1;
    private String rampInterval;
    private Map<String, String> scenarioParameters;
    private Map<String, String> scenarioProviderParameters;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScenarioName() {
        return scenarioName;
    }

    @Override
    public String getStageDelay() {
        return stageDelay;
    }

    @Override
    public String getStageDuration() {
        return stageDuration;
    }

    @Override
    public double getArrivalRate() {
        return arrivalRate;
    }

    @Override
    public double getRampArrival() {
        return rampArrival;
    }

    @Override
    public double getRampArrivalRate() {
        return rampArrivalRate;
    }

    @Override
    public String getRampInterval() {
        return rampInterval;
    }

    @Override
    public Map<String, String> getScenarioParameters() {
        return scenarioParameters;
    }

    @Override
    public Map<String, String> getScenarioProviderParameters() {
        return scenarioProviderParameters;
    }

}
