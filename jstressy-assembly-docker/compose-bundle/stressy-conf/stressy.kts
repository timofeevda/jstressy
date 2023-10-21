import com.github.timofeevda.jstressy.config.dsl.config

config {
    globals {
        host = "localhost"
        port = 8082
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
                arrivalRate = 20.0
            }
        }

    }
}