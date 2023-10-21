# jstressy
[![Build Status](https://travis-ci.com/timofeevda/jstressy.svg?branch=master)](https://travis-ci.com/timofeevda/jstressy)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy)

# JStressy
JStressy is a lightweight framework for prototyping load/stress tools for Web applications. Each framework's component can be separately changed and reimplemented based on your needs. Current implementation has hardcoded dependencies on [Vert.x](https://vertx.io/) and [RxJava 2.x](https://github.com/ReactiveX/RxJava) (can be changed in future).

## How to build
```
mvn clean install
```

## How to run
```
cd jstressy-assembly-springboot
java -DconfigFolder=../jstressy-assembly-docker/compose-bundle/stressy-conf/ -jar ./target/jstressy.jar
```
or using predefined run configurations supported by IDE.

## How to build you own bundle with scenarios
Use **jstressy-assembly-sprinboot** as a reference for building Spring Boot application

## JStressy basic configuration
Basic JStressy configuration consists of global settings related to parameters of the host being tested and exposed JStressy mertics. The number of scenarios and test stages is described in stress plan section:
```yaml
globals:
  host: localhost               # required: path to the host
  port: 8080                    # required: port
  stressyMetricsPort: 8089      # required: port for exporting metrics (in Prometheus format)
  stressyMetricsPath: /metrics  # required: metrics path
  useSsl: false                 # optional: turn on/off ssl (default: false)
  insecureSsl: false            # optional: trust all certificates (default: false)
  maxConnections: 1000          # optional: max connections pool size (default: 1000)
stressPlan:                     # describes stress plan
  stages:                       # list of stress plan stages
    - name: First               # optional: stage name
      scenarioName: SearchStore # required: scenario name
      stageDelay: 1s            # required: delay stage execution for the specified time
      stageDuration: 20m        # required: stage duration
      arrivalRate: 1            # required: scenario invocation rate (1 time per second)
      rampArrival: 0.5          # optional: ramp scenario invocation rate (2 timer per second)
      rampArrivalRate: 1        # optional: ramp rate (increase rampArrival each second)
      rampInterval: 10m         # optional: ramping interval
      scenarioParameters:       # optional: parameters map which is passed to the scenario
        foo: bar
        foo1: bar1
```
JStressy doesn't restrict changes to the format of the config if it follows the initial structure which is read by JStressy itself

## Modelling arrivals

JStressy supports several ways to model arrivals: constant rate, ramping rate, interval rate, Poisson arrival, and their combinations.

### Constant rate arrivals
```yaml
stressPlan:                     
  stages:                       
    - name: ConstRate
      ....
      arrivalRate: 1 # constant rate - 1 per second
      ...
```
![](https://github.com/timofeevda/jstressy/blob/master/docs/figures/const_rate.PNG)

### Randomized constant rate arrivals

Randomly distributes arrivals within the time interval defined by arrival rate. Exactly one arrival is generated for each arrival interval. `randomizeArrivals` can be used in any arrival definitions, but not with `poissonArrival` which has higher priority over `randomizeArrivals`.

```yaml
stressPlan:                     
  stages:                       
    - name: ConstRandomizedRate
      ....
      arrivalRate: 1 # constant rate - 1 per second
      randomizeArrival: true
      ...
```

### Ramping rate arrivals
```yaml
stressPlan:                     
  stages:                       
    - name: RampingRate
      ....
      arrivalRate: 1        # constant rate - 1 time per second
      rampArrival: 10       # target constant rate we need to get - 10 times per second
      rampArrivalPeriod: 1m # how often we need to adjust arrival rate before getting the target one (1 time per minute)    
      rampDuration: 10m     # how long it should take to achieve target arrival rate (10 times per second after 10 minutes of 10 adjustments)
      ...
```
Note: *rampArrivalRate* can be used instead of *rampArrivalPeriod* to describe how often we need to adjust arrival rate

![](https://github.com/timofeevda/jstressy/blob/master/docs/figures/ramping_rate.PNG)

### Poisson arrivals

JStressy supports modeling of homogeneous Poisson arrivals by using constant arrival rate and *poissonArrival* option.

```yaml
stressPlan:                     
  stages:                       
    - name: PoissonArrivals
      ....
      arrivalRate: 1 
      poissonArrival: true # turns Poisson arrivals mode on
      ...
```

Note: by default, JStressy uses random double value in [poissonMinRandom, 1) interval for calculating the next Poisson arrival. poissonMinRandom property can be used to set the random number min value (inclusive) to control the number of Poisson arrivals and the interval between these arrivals (default value is 0.0001). Big min random values give you more arrivals with smaller intervals between each arrival.

![](https://github.com/timofeevda/jstressy/blob/master/docs/figures/poisson_rate.PNG)

JStressy also supports modeling of non-homogeneous Poisson arrivals by using ramping arrival rate and *poissonArrival* option. In this mode, the next arrival time will be determined based on the current arrival rate.
```yaml
stressPlan:                     
  stages:                       
    - name: NonHomogeneousPoissonArrivals
      ....
      arrivalRate: 1 
      poissonArrival: true  # turns Poisson arrivals mode on
      rampArrival: 10       # target constant rate we need to get - 10 times per second
      rampArrivalPeriod: 1m # how often we need to adjust arrival rate before getting the target one (1 time per minute)    
      rampDuration: 10m     # how long it should take to achieve target arrival rate (10 timer per second after 10 minutes of 10 adjustments)
      ...
```

### Interval arrivals
JStressy can combine different arrival types using *arrivalIntervals* property allowing to model different use cases and make scenario arrivals close to real life. 

Below is the example of defining arrival rates for one stage. Several constant rates defined.
```yaml
stressPlan:                     
  stages:                       
    - name: IntervalArrivals
      ....
      arrivalIntervals:
      - id: first
        arrivalRate: 1
        duration: 5m
      - id: second
        arrivalRate: 3
        delay: 5m
        duration: 5m
      - id: third
        arrivalRate: 1        
        delay: 10m
        duration: 5m
      ...
```
![](https://github.com/timofeevda/jstressy/blob/master/docs/figures/intervals_rate.PNG)

Below is the example of mixing different arrivals type for one stage. Constant rate is mixed with Poisson arrivals and ramping rate in the end.
```yaml
stressPlan:                     
  stages:                       
    - name: IntervalArrivals
      ....
      arrivalIntervals:
      - id: first
        arrivalRate: 0.016
        duration: 10m
      - id: second
        arrivalRate: 0.016
        poissonArrival: true
        delay: 10m
        duration: 10m
      - id: third
        arrivalRate: 0.016
        rampArrival: 1.0
        rampArrivalPeriod: 1m
        rampDuration: 10m
        delay: 20m
        duration: 10m
      ...
```
![](https://github.com/timofeevda/jstressy/blob/master/docs/figures/mixed_rate.PNG)

### Scenario actions
Scenario represent a runnable activity within stress test stage. Scenario actions provide a way to describe scenario as a set of actions reusable across scenarios with their own arrival rate definitions.

```yaml
stressPlan:
  stages:
    - name: Test 
      scenarioName: Register email
      delay: 10s
      duration: 5m
      arrivalRate: 1
      actions:
        - name: Send confirmation code
          arrivalRate: 1
          delay: 10s
          duration: 5m
          actionParameters:
            code: 12345
        - name: Confirm email
          arrivalRate: 1
          delay: 5m
          duration: 5m
          actionParameters:
            code: 12345
```

### Global arrival rate distribution for scenario actions

By default, all scenarios are independent and run their actions in parallel. In some cases you want to model several simultaneously running scenarios but only one of them to perform a specific action (to maintain global rate of action in the system). Instead of configuring a separate scenario for performing a specific action with its own rate, `distributionMode` configuration mode can be used for scenario actions. If `distributionMode` configuration option is enabled, each time to perform an action, scheduler selects one of the active scenarios based on the chosen policy (round robing or random). Globally distributed action is uniquely identified by stage name and its index in the list of actions.

```yaml
stressPlan:
  stages:
    - name: Test
      scenarioName: Run action
      delay: 10s
      duration: 5m
      arrivalRate: 1
      actions:
        - name: Search files
          arrivalRate: 3
          distributionMode: ROUND_ROBIN
          delay: 10s
          duration: 10m
```

### DSL Support

JStressy allows describing configuration in a form of Kotlin DSL.  By default, JStressy looks for the stressy.kts file in the configuration folder and tries compiling it to build a configuration and optionally write it to the config folder as YAML configuration file, so that generated definitions can be checked if required. If it can't find DSL script file, it fallbacks to reading YAML configuration file stressy.yml which must be present it the config folder in this case.

DSL example for running HTTPEcho scenario with constant arrival rate:
```kotlin
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
            
            stage {
                name = "Email registration"
                scenarioName = "Register email"
                duration = "5m"
                arrivalRate = 1.0
                action {
                    name = "Send confirmation code"
                    arrivalRate = 1.0
                    actionParameters = mapOf("code" to "1234")
                    distributionMode = ActionDistributionMode.ROUND_ROBIN
                }
            }

            for(i in 1 .. 2) {
                stage {
                    name = "Stage $i"
                    scenarioName = "EchoWebSocket"
                    delay = "${10 * i}s"
                    duration =  "10s"
                    arrivalRate = 0.5
                }
            }
        }

    }
}
```
