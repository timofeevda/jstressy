# jstressy
[![Build Status](https://travis-ci.org/timofeevda/jstressy.svg?branch=master)](https://travis-ci.org/timofeevda/jstressy)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy)

# JStressy
Lightweight framework for prototyping load/stress tools for Web applications. Each framework's component can be separately changed and reimplemented based on your needs. Such modularity with the ability to replace almost everything is achieved by incorporating [OSGi framework](https://www.osgi.org/developer/architecture/). Current implementation has hardcoded dependencies on [Vert.x](https://vertx.io/) and [RxJava 2.x](https://github.com/ReactiveX/RxJava) (can be changed in future). [Apache Felix](http://felix.apache.org/) is used as an OSGi framework implementation. Scenarios are meant to be implemented as OSGi bundles, allowing you to implement hot code reload and other useful OSGi framework's features.

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

## JStressy assembly
JStressy provides Maven plugin which can be used to easily create and distribute custom JStressy builds. Developer just needs to describe the set of dependencies representing OSGi bundles (forming custom JStressy build).
JStressy Maven plugin configuration example (full example can be seen in [jstressy-assembly-osgi pom.xml](https://github.com/timofeevda/jstressy/blob/master/jstressy-assembly/pom.xml)):
```xml
<plugin>
    <groupId>com.github.timofeevda.jstressy</groupId>
    <artifactId>jstressy-maven-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>build</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <target>${project.basedir}/target</target>        
        <systemBundles>
            <systemBundle>
                <groupId>com.github.timofeevda.jstressy</groupId>
                <artifactId>jstressy-logger</artifactId>
            </systemBundle>
            <systemBundle>
                <groupId>org.apache.felix</groupId>
                <artifactId>org.apache.felix.fileinstall</artifactId>
            </systemBundle>
        </systemBundles>
        <scenarioBundles>
            <scenarioBundle>
                <groupId>com.github.timofeevda.jstressy</groupId>
                <artifactId>jstressy-dummy-scenario-kotlin</artifactId>
            </scenarioBundle>
        </scenarioBundles>
    </configuration>
</plugin>
```
JStressy Maven plugin creates folders structure with all required component needed to run Apache Felix with provided JStressy bundles. Almost everything can be customized, especially configuration folder and run scripts:
```
|--- bin/
|    |--- felix.jar
|--- bundles/
|    |--- system/       # basic bundles which are loaded first during Apache Felix start (logback initialization etc.)
|    |--- application/  # all bundles representing custom JStressy framework build with all required dependencies
|    |--- plugins/      # scenarios bundles, each bundles can be replaced in folder for hot reload
|--- config/
|    |--- config.properties # Apache Felix config
|    |--- logback.xml       # basic logback config
|--- run.sh                 # basic run script
|--- stressy.yml            # basic configuration example
```

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
JStressy can combine different arrival types using *arrivalIntervals* property allowing to model different use cases and make scenario arrivals close to real life. Below is the example of mixing different arrivals type for one stage. Constant rate is mixed with Poisson arrivals and ramping rate in the end.
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
