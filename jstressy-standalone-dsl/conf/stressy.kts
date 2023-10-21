import com.github.timofeevda.jstressy.config.dsl.config

val targetHost = "localhost"
val targetPort = 8082

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
                duration = "48h"
                arrivalRate = 1.0
                action {
                    arrivalRate = 0.5
                    duration = "1m"
                    run = { metricsRegistry, requestExecutor ->
                        requestExecutor.get(targetHost, targetPort, "/")
                            .subscribe { r ->
                                metricsRegistry.counter("response_counter", "response counter").inc()
                                println("Got headers in response:" + r.headers())
                            }
                    }
                }
            }
        }

    }
}