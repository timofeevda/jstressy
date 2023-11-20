@file:Import("utils.kt")
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import com.github.timofeevda.jstressy.api.config.parameters.ScenarioContext
import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.config.dsl.Import
import com.github.timofeevda.jstressy.config.dsl.config
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository

val targetHost = "localhost"
val targetPort = 8082

val gson = Gson()

val counter = AtomicInteger(0)

config {
    globals {
        host = targetHost
        port = targetPort
        stressyMetricsPort = 8089
        stressyMetricsPath = "/metrics"
        useSsl = false
        insecureSsl = false
        maxConnections = 3000

        plan {
            stage {
                name = "Echo"
                scenarioName = "HTTPEcho"
                delay = "1s"
                duration = 1.seconds()
                arrivalRate = 1.0
                action {
                    arrivalRate = 0.5
                    duration = "30s"
                    run = { requestExecutor, metricsRegistry, handle ->
                        requestExecutor.makeRequest(metricsRegistry, handle.ctx())
                    }
                }

                action {
                    arrivalRate = 1/3.0
                    duration = "30s"
                    run = { requestExecutor, metricsRegistry, handle ->
                        println("Response from context: ${handle.ctx().get("response")}")
                    }
                }
            }
        }
    }

}

fun RequestExecutor.makeRequest(metricsRegistry: MetricsRegistry, ctx: ScenarioContext) =
    this.post(targetHost, targetPort, "/", gson.toJson(listOf(counter.incrementAndGet()))) {
        it.putHeader("X-Test", "test")
    }.subscribe { r ->
        metricsRegistry.counter("response_counter", "response counter").inc()
        r.bodyHandler {
            //println("Got response: \n$it")
            ctx.put("response", it)
        }
    }