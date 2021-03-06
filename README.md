# jstressy
[![Build Status](https://travis-ci.org/timofeevda/jstressy.svg?branch=master)](https://travis-ci.org/timofeevda/jstressy)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy)

# JStressy
Lightweight framework for prototyping load/stress tools for Web applications. Each framework's component can be separately changed and reimplemented based on your needs. Current implementation has hardcoded dependencies on [Vert.x](https://vertx.io/) and [RxJava 2.x](https://github.com/ReactiveX/RxJava) (can be changed in future).

## How to build
```
mvn clean install
```

## How to run
```
java -DconfigFolder=../jstressy-assembly-docker/compose-bundle/stressy-conf/ -jar ./target/jstressy.jar
```

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

Note: by default, JStressy uses [0,1) random numbers interval to calculation the next Poisson arrival. *poissonMaxRandom* property can be used to set the random number maximum value (exclusive) to control the number of Poisson arrivals and the interval between Poisson arrival. Low max random values give you fewer arrivals with bigger intervals between each arrival.

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
