import com.github.timofeevda.jstressy.config.dsl.config

config {
    globals {
        host = "localhost"
        port = 8081
        stressyMetricsPort = 8089
        stressyMetricsPath = "/metrics"
        useSsl = false
        insecureSsl = false
        maxConnections = 3000

        plan {
            stage {
                name = "Echo"
                scenarioName = "WebSocketEcho"
                delay = "1s"
                duration = "48h"
                arrivalRate = 0.1
            }
        }
    }
}