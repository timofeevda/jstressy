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

package com.github.timofeevda.jstressy.api.config.parameters;

import java.util.Map;

/**
 * Stress stage description
 *
 * @author timofeevda
 */
public interface StressyStage {
    /**
     * Stress stage name
     *
     * @return stress stage name
     */
    String getName();

    /**
     * Scenario name
     *
     * @return scenario name
     */
    String getScenarioName();

    /**
     * Stage delay
     *
     * @return stage delay
     */
    String getStageDelay();

    /**
     * Stage duration
     *
     * @return stage duration
     */
    String getStageDuration();

    /**
     * Determines how ofter scenario is invoked
     *
     * @return scenario invocation rate
     */
    double getArrivalRate();

    /**
     * Determines target value of arrival rate. Must be used along with rampArrivalRate and rampInterval
     *
     * @return target value of arrival rate
     */
    double getRampArrival();

    /**
     * Determines how often arrival rate must be adjusted to match target value of arrival rate (rampArrival)
     * in the end of ramp interval
     *
     * @return arrivalRate adjustment rate
     */
    double getRampArrivalRate();

    /**
     * Determines time interval within which arrival rate must match target arrival rate (rampArrivalRate)
     *
     * @return arrayRate adjustment interval
     */
    String getRampInterval();

    /**
     * Scenario parameters. Can be used to pass arbitrary parameters for each scenario invocation
     *
     * @return map of scenario parameters
     */
    Map<String, String> getScenarioParameters();

    /**
     * Scenario provider parameters. Can be used to pass arbitrary parameters for each scenario provider. This can be
     * used for scenario provider initialization logic if it requires some long-running tasks
     *
     * @return map of scenario provider parameters
     */
    Map<String, String> getScenarioProviderParameters();
}
