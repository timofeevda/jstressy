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

package com.github.timofeevda.jstressy.api.config.parameters

/**
 * Stress stage description
 *
 * @author timofeevda
 */
interface StressyStage : StressyArrivalDefinition {
    /**
     * Stress stage name
     *
     * @return stress stage name
     */
    val name: String

    /**
     * Scenario name
     *
     * @return scenario name
     */
    val scenarioName: String

    /**
     * Stage delay
     *
     * @return stage delay
     */
    val stageDelay: String?

    /**
     * Stage duration
     *
     * @return stage duration
     */
    val stageDuration: String

    /**
     * Scenario parameters. Can be used to pass arbitrary parameters for each scenario invocation
     *
     * @return map of scenario parameters
     */
    val scenarioParameters: Map<String, String>

    /**
     * Scenario provider parameters. Can be used to pass arbitrary parameters for each scenario provider. This can be
     * used for scenario provider initialization logic if it requires some long-running tasks
     *
     * @return map of scenario provider parameters
     */
    val scenarioProviderParameters: Map<String, String>

    /**
     * Limits the number of scenarios to run
     *
     * @return max number of scenarios to run
     */
    val scenariosLimit: Int?

    /**
     * Defines several intervals with different arrival rate to configure different
     * scenario arrival rates within different time intervals
     */
    val arrivalIntervals: MutableList<StressyArrivalInterval>

    val arrivalIntervalsPath: String?
}
