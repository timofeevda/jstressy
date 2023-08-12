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

package com.github.timofeevda.jstressy.config.parameters

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.timofeevda.jstressy.api.config.parameters.StressyConfiguration
import com.github.timofeevda.jstressy.api.config.parameters.StressyGlobals

/**
 * Stressy configuration
 *
 * @author timofeevda
 */
@JsonPropertyOrder("globals", "globalParameters", "stressPlan")
class Config : StressyConfiguration {
    constructor(init: Config.() -> Unit): this() {
        init()
    }

    constructor()

    /**
     * Global Stressy configuration
     */
    override var globals: StressyGlobals = Globals()

    /**
     * Stress plan configuration
     */
    override var stressPlan: StressPlan = StressPlan()

    /**
     * Arbitrary parameters defined globally
     */
    override var globalParameters: Map<String, String> = emptyMap()

    fun globals(init: Globals.() -> Unit) {
        globals = Globals(init)
    }

    fun plan(init: StressPlan.() -> Unit) {
        stressPlan = StressPlan(init)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Config

        if (globals != other.globals) return false
        if (stressPlan != other.stressPlan) return false
        return globalParameters == other.globalParameters
    }

    override fun hashCode(): Int {
        var result = globals.hashCode()
        result = 31 * result + stressPlan.hashCode()
        result = 31 * result + globalParameters.hashCode()
        return result
    }


}
