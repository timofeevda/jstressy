import com.github.timofeevda.jstressy.config.dsl.config

config {
    globals {
        host = "stressy-http-echo"
        port = 8082
        stressyMetricsPort = 8089
        stressyMetricsPath = "/metrics"
        useSsl = false
        insecureSsl = false
        maxConnections = 3000

//        renderedMetrics {
//            host = "stressy-grafana"
//            port = 3000
//            useSsl = false
//            folder = "/etc/stressy/rendered-metrics"
//            cumulative = true
//            period = "10s"
//            uri = "/render/d-solo/jbwEuZkmk/prometheus?orgId=1&refresh=5s&panelId=72&width=1000&height=500"
//            filePrefix = "requests-"
//            fileExtension = ".png"
//            dateTimeFormat = "yyyyMMdd-HHmmssZ"
//            timeZone = "Europe/Oslo"
//        }

        loggerSummary {
            interval = "30s"
        }

//        yamlSummary {
//            folder = "/etc/stressy/yaml-metrics"
//            interval = "30s"
//        }

    }

    plan {
        stage {
            name = "Echo"
            scenarioName = "HTTPEcho"
            delay = "10s"
            duration = "48h"
            arrivalRate = 0.5
        }
    }

}