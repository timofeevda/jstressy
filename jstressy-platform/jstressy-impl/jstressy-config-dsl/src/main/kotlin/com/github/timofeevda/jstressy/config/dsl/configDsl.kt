package com.github.timofeevda.jstressy.config.dsl

import com.github.timofeevda.jstressy.config.parameters.Config


fun config(init: Config.() -> Unit) = Config(init)
