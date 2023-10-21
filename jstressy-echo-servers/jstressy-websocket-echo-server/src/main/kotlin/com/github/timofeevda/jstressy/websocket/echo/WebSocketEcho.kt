package com.github.timofeevda.jstressy.websocket.echo

import io.vertx.reactivex.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
open class WebSocketEcho(@Value("\${websocket.listen.port}") private val port: Int) : ApplicationRunner {

    private val log: Logger = LoggerFactory.getLogger(WebSocketEcho::class.java)

    override fun run(args: ApplicationArguments?) {
        log.info("Starting listening WebSocket connections on $port")
        Vertx.vertx().createHttpServer()
            .webSocketHandler { socket ->
                socket.textMessageHandler { message ->
                    log.info("Responding back with echo message to ${socket.authority()}")
                    socket.writeTextMessage(message)
                }
            }.listen(port)
    }

}

fun main(args: Array<String>) {
    runApplication<WebSocketEcho>(*args) {
    }
}