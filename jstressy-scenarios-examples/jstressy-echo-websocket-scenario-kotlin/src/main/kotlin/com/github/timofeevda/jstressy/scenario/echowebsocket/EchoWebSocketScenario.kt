package com.github.timofeevda.jstressy.scenario.echowebsocket

import com.github.timofeevda.jstressy.api.config.ConfigurationService
import com.github.timofeevda.jstressy.api.config.parameters.ScenarioActionDefinition
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.api.scenario.Scenario
import com.github.timofeevda.jstressy.api.scenario.ScenarioAction
import com.github.timofeevda.jstressy.utils.logging.LazyLogging
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

private val echoWebsockets = AtomicLong(0)

private val messagesSent = AtomicLong(0)
private val messagesReceived = AtomicLong(0)

class EchoWebSocketScenario internal constructor(metricsRegistry: MetricsRegistry,
                                                 private val requestExecutor: RequestExecutor,
                                                 configurationService: ConfigurationService) : Scenario {

    companion object : LazyLogging()

    private val host: String = configurationService.configuration.globals.host
    private val port: Int = configurationService.configuration.globals.port

    private var websocketDisposable: Disposable? = null

    private val echoMessageSendExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        metricsRegistry.gauge("echo-websockets", "The number of open echo WebSocket connections ",
            { echoWebsockets.toDouble() })
        metricsRegistry.gauge("messages-sent", "The number of messages sent to WebSocket stream",
            { messagesSent.toDouble() })
        metricsRegistry.gauge("messages-received", "The number of messages received from WebSocket stream",
            { messagesReceived.toDouble() })
    }

    override fun start(actions: List<ScenarioActionDefinition>) {
        websocketDisposable = requestExecutor.websocket(host, port, "/raw")
                .subscribe({ websocket ->
                    websocket.textMessageHandler { text ->
                        logger.info("rcv: $text")
                        messagesReceived.incrementAndGet()
                        // reply back with the same message after delay
                        echoMessageSendExecutor.schedule({
                            websocket.writeTextMessage(text)
                        }, 5, TimeUnit.SECONDS)
                    }
                    websocket.closeHandler {
                        logger.info("closed")
                        echoWebsockets.decrementAndGet()
                    }
                    websocket.writeTextMessage("hello!")
                    messagesSent.incrementAndGet()
                    echoWebsockets.incrementAndGet()
                }, { error ->
                    logger.error("Error establishing websocket connection", error)
                })
    }

    override fun stop() {
        websocketDisposable?.dispose()
    }

    override fun withArrivalInterval(intervalId: String): Scenario {
        return this
    }

    override fun withParameters(parameters: Map<String, String>): Scenario {
        return this
    }

    override fun createAction(action: String, parameters: Map<String, String>, intervalId: String): ScenarioAction {
        return object: ScenarioAction {
            override fun run() {
                // do nothing
            }
        }
    }

    override fun withActionDistributionId(id: String): Scenario {
        return this
    }

    override fun getActionDistributionId(): String? = null

    override fun isAvailableForActionDistribution() : Boolean = false

}