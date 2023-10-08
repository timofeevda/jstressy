import com.github.timofeevda.jstressy.config.dsl.config

config {
    globals {
        host = "ws.postman-echo.com"
        port = 433
        stressyMetricsPort = 8089
        stressyMetricsPath = "/metrics"
        useSsl = true
        insecureSsl = false
        maxConnections = 3000

        plan {
            stage {
                name = "Echo"
                scenarioName = "EchoWebSocket"
                delay = "1s"
                duration = "48h"
                arrivalRate = 0.1
            }
        }
    }
}