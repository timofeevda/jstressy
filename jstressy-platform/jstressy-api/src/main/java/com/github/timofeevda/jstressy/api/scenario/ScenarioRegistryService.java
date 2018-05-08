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

package com.github.timofeevda.jstressy.api.scenario;

/**
 * Scenario registry. Provides access to registered scenario providers
 *
 * @author timofeevda
 */
public interface ScenarioRegistryService {

    /**
     * Get {@link ScenarioProviderService} instance based on scenario name
     *
     * @param scenarioName scenario name
     * @return {@link ScenarioProviderService}
     */
    ScenarioProviderService get(String scenarioName);

    /**
     * Registers {@link ScenarioProviderService} for the specified scenario name
     *
     * @param scenarioName            scenario name
     * @param scenarioProviderService scenario provider service to register
     */
    void registerScenarioProviderService(String scenarioName, ScenarioProviderService scenarioProviderService);

}
