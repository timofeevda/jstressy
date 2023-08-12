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

package com.github.timofeevda.jstressy.api.scenario

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition

/**
 * Scenario
 *
 * @author timofeevda
 */
interface Scenario {
    /**
     * Method for starting scenario
     * @param actions list of scenario action definitions
     */
    fun start(actions: List<ScenarioActionDefinition>)

    /**
     * Method for stopping scenario
     */
    fun stop()

    /**
     * Passes arrival interval definition within which this scenario was invoked
     *
     * @param intervalId arrival interval definition id
     */
    fun withArrivalInterval(intervalId: String): Scenario

    /**
     * Passes key/value pair as scenario parameters
     * @param parameters  key/value pairs
     * @return
     */
    fun withParameters(parameters: Map<String, String>): Scenario

    /**
     * Create scenario action instance
     *
     * @param action scenario action name
     * @param parameters arbitrary parameters for scenario action
     * @param intervalId arrival interval identifier that can be used to distinguish arrival interval when scenario action instance was created
     * @return scenario action instance
     */
    fun createAction(action: String, parameters: Map<String, String>, intervalId: String) : ScenarioAction

    /**
     * Allows to set global actions distribution identifier which is used to coordinate action invocation among scenarios
     * with the same distribution id
     *
     * @param id global actions distribution identifier
     */
    fun withActionDistributionId(id: String) : Scenario

    /**
     * Get current global actions distribution identifier which is used to coordinate action invocation among scenarios
     *
     * @return global actions distribution identifier
     */
    fun getActionDistributionId(): String?

    /**
     * Return true is scenario is available for global action invocation
     *
     * @return true is scenario is available for global action invocation, otherwise false
     */
    fun isAvailableForActionDistribution() : Boolean
}
