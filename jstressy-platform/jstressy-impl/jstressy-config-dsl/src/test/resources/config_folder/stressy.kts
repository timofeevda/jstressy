@file:Import("utils.kt")

import com.github.timofeevda.jstressy.api.config.parameters.ActionDistributionMode.ROUND_ROBIN
import com.github.timofeevda.jstressy.config.dsl.Import
import com.github.timofeevda.jstressy.config.dsl.config

config {

    globals {
        host = "google.com"
        port = 443
        stressyMetricsPort = 8089
        stressyMetricsPath = "/metrics"
        useSsl = true
        insecureSsl = true
        maxConnections = 1000
        maxWebSockets = 1000
        maxWebSocketMessageSize = 64516
        maxWebSocketFrameSize = 16324
        webSocketPerMessageDeflate = true
        webSocketCompressionLevel = 6
        connectionKeepAlive = true
        logNetworkActivity = true
        overwriteWithDSLGeneratedConfig = true
    }

    plan {

        stage {
            name = "First"
            scenarioName = "PostmanEcho"
            delay = "1s"
            duration = 20.minutes()
            arrivalRate = 1.0
            rampArrival = 0.5
            rampArrivalRate = 0.0169
            rampArrivalPeriod = 10.minutes()
            rampDuration = 1.minutes()
            poissonArrival = true
            poissonMinRandom = 0.5
            randomizeArrival = true
            scenariosLimit = 100
            scenarioParameters = mapOf("foo" to "bar")
            scenarioProviderParameters = mapOf("foo" to "bar")

            arrivalInterval {
                id = "test"
                delay = "1s"
                duration = 20.minutes()
                arrivalRate = 1.0
                rampArrival = 0.5
                rampArrivalRate = 0.0169
                rampArrivalPeriod = 10.minutes()
                rampDuration = 1.minutes()
                poissonArrival = true
                poissonMinRandom = 0.5
                randomizeArrival = true
            }

            action {
                name = "action"
                delay = "1s"
                duration = 20.minutes()
                arrivalRate = 1.0
                rampArrival = 0.5
                rampArrivalRate = 0.0169
                rampArrivalPeriod = 10.minutes()
                rampDuration = 1.minutes()
                poissonArrival = true
                poissonMinRandom = 0.5
                randomizeArrival = true
                distributionMode = ROUND_ROBIN
                actionParameters = mapOf("foo" to "bar")

                arrivalInterval {
                    id = "test"
                    delay = "1s"
                    duration = 20.minutes()
                    arrivalRate = 1.0
                    rampArrival = 0.5
                    rampArrivalRate = 0.0169
                    rampArrivalPeriod = 10.minutes()
                    rampDuration = 1.minutes()
                    poissonArrival = true
                    poissonMinRandom = 0.5
                    randomizeArrival = true
                }

            }
        }

    }

}
