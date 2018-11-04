# jstressy
[![Build Status](https://travis-ci.org/timofeevda/jstressy.svg?branch=master)](https://travis-ci.org/timofeevda/jstressy)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.timofeevda.jstressy/jstressy)

# JStressy
Lightweight framework for prototyping load/stress tools for Web applications. Each framework's component can be separately changed and reimplemented based on you needs. Such modularity with ability to replace almost everything is achieved by incorporating [OSGi framework](https://www.osgi.org/developer/architecture/). Current implementation has hardcoded dependencies on [Vert.x](https://vertx.io/) and [RxJava 2.x](https://github.com/ReactiveX/RxJava) (can be changed in future). [Apache Felix](http://felix.apache.org/) is used as an OSGi framework implemenation. Scenarios are meant to be implemented as OSGi bundles, allowing you to implement hot code reload and other useful OSGi framework's features.

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
JStressy provides Maven plugin which can be used to easily create and distribute custom JStressy builds. Developer just needs to describe set of dependencies representing OSGi bundles (forming custom JStressy build).
JStressy Maven plugin configuration example (full example can be seen in [jstressy-assembly pom.xml](https://github.com/timofeevda/jstressy/blob/master/jstressy-assembly/pom.xml)):
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

