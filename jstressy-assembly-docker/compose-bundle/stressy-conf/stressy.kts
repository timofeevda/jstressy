import com.github.timofeevda.jstressy.config.dsl.config

config {
    globals {
        host = "postman-echo.com"
        port = 433
        stressyMetricsPort = 8089
        stressyMetricsPath = "/metrics"
        useSsl = true
        insecureSsl = false
        maxConnections = 3000

        plan {
            stage {
                name = "Echo"
                scenarioName = "PostmanEcho"
                delay = "10s"
                duration = "48h"
                arrivalRate = 0.5
            }
        }
    }
}