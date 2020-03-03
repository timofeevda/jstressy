package com.github.timofeevda.jstressy.scenario.echowebsocket.internal

import com.github.timofeevda.jstressy.api.scenario.ScenarioProviderService
import com.github.timofeevda.jstressy.scenario.echowebsocket.EchoWebSocketScenarioProviderService
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import java.util.Hashtable

class Activator : BundleActivator {
    companion object : LazyLogging()

    override fun start(context: BundleContext) {
        logger.info("Registering EchoWebSocket scenario provider service")
        context.registerService(ScenarioProviderService::class.java.name, EchoWebSocketScenarioProviderService(), Hashtable<Any, Any>())
    }

    override fun stop(context: BundleContext) {

    }
}
