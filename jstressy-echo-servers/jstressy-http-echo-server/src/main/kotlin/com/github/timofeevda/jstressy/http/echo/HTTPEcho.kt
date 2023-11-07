package com.github.timofeevda.jstressy.http.echo

import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.http.HttpServerRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
open class HTTPEcho(@Value("\${http.listen.port}") private val port: Int) : ApplicationRunner {

    private val log: Logger = LoggerFactory.getLogger(HTTPEcho::class.java)

    override fun run(args: ApplicationArguments?) {
        log.info("Starting listening WebSocket connections on $port")
        Vertx.vertx().createHttpServer()
            .requestHandler { request ->
                log.info("Got request with path: ${request.path()} params: ${request.params()} headers: ${request.headers()}")
                // trivial implementation of body handler for testing purposes
                request.bodyHandler { buffer ->
                    val body = String(buffer.bytes)
                    val report = responseReport(request, body)
                    val responseBuffer = Buffer.buffer(report)

                    request.response().statusCode = 200
                    request.response().headers()["Content-Length"] = responseBuffer.length().toString()
                    request.response().write(responseBuffer)
                    request.response().end()
                }
            }.listen(port)
    }

    private fun responseReport(request: HttpServerRequest, body: String) =
        "Path: ${request.path()}\n" +
                "Params: ${request.params()}\n" +
                "Headers:\n" +
                "${request.headers()}" +
                "Body:\n" +
                body
}

fun main(args: Array<String>) {
    runApplication<HTTPEcho>(*args) {
    }
}