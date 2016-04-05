# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added
- command line argument for prefix in test-need analysis
- some more tests added
### Changed
- test-need analysis now outputs csv (instead of console output)
- update to clojure 1.8.0

##[0.6.2] - 2016-03-15
### Fixed
- regex matching for jacoco aggregate not working correctly

##[0.6.1] - 2016-03-14
### Fixed
- jacoco aggregation does not convert packages to pattern
### Changed
- refactored analysis module to be inside test-mate module

##[0.6.0] - 2016-03-13
### Changed
- aggregate coverage now allows regular expressions (jacoco-format only)
### Added
- tests for statistic-server command and core module

## [0.5.0] - 2016-02-21
### Added
- added log output to the statistic-server publish-coverage command
- legacy emma xml support (implemented for cloverage output)

## [0.4.1] - 2016-01-25
### Fixed
- broken package aggregation for jacoco reports

## [0.4.0] - 2016-01-25
### Added
- Cobertura v4 coverage data aggregation support

## [0.3.0] - 2016-01-03
### Added
- configuration can be set via system property fdc.test-mate.config.file

##[0.2.0] - unknown
### Added
- Interfacing with fdc-test-statistic server functionality
