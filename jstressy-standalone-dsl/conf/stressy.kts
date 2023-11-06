@file:Import("utils.kt")
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import com.github.timofeevda.jstressy.api.httprequest.RequestExecutor
import com.github.timofeevda.jstressy.api.metrics.MetricsRegistry
import com.github.timofeevda.jstressy.config.dsl.Import
import com.github.timofeevda.jstressy.config.dsl.config
import com.google.gson.Gson
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository

val targetHost = "localhost"
val targetPort = 8082

val gson = Gson()

fun RequestExecutor.makeRequest(metricsRegistry: MetricsRegistry) =
    this.post(targetHost, targetPort, "/", gson.toJson(listOf("1", "2", "3", "4")))
        .subscribe { r ->
            metricsRegistry.counter("response_counter", "response counter").inc()
            r.bodyHandler {
                println("Got response: \n$it")
            }
        }

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
                delay = "10s"
                duration = 48.hours()
                arrivalRate = 1.0
                action {
                    arrivalRate = 0.5
                    duration = "1m"
                    run = { metricsRegistry, requestExecutor ->
                        requestExecutor.makeRequest(metricsRegistry)
                    }
                }
            }
        }
    }

}