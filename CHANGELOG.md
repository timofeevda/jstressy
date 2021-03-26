# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project is trying to adhere to [Semantic Versioning](http://semver.org/).

## Unreleased
[1.1.0]
### Added
- new Kotlin version
- removed OSGi support
- migrated to Vert.X 4.0.3
- micrometer metric is a provided dependency so that it can be change to any required

## [1.0.12] - 2019-01-08
### Added
- advance dependencies versions to satisfy GitHub alerts

## [1.0.11] - 2019-01-08
### Added
- migrate to 3.6.2 VertX version - fixes OSGI assembly
- added VertX metrics SPI implementation

## [1.0.10] - 2018-12-13
### Added
- added ability to assembly JStressy as Spring Boot application
- migrate to 3.6.0 VertX version
- added websocket compression options

## [1.0.9] - 2018-10-26
### Added
- fixed gauge definition in metrics registry
- added number of bytes to websocket message (to track data being passed)

## [1.0.8] - 2018-10-23
### Failed release

## [1.0.7] - 2018-10-23
### Added
- added put/delete methods to request executor

## [1.0.6] - 2018-10-22
### Added
- get rid of Kotlin's module functions - too much effort to OSGIfy them

## [1.0.5] - 2018-10-22
### Added
- added session manager
- migrate to Kotlin
- improved websocket utils

## [1.0.4] - 2018-05-23
### Added
- configuration service uses folder instead of file path

## [1.0.3] - 2018-05-21
### Added
- added quantiles to timers in micrometer metrics registry
- added scenario implementation written in Kotlin as example
- added separate HTTP session manager

## [1.0.2] - 2018-05-10
### Added
- added metrics registry implementation based on micrometer

## [1.0.1] - 2018-05-08
### Added
- first release, released maven plugin and default services implementations
