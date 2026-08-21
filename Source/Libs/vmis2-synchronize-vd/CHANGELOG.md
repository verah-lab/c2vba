# Changelog
ll notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## 1.5.0
**Changed**
- change to pipeline.gitlab-ci.yml
- java 21
- new base-parent and other dependencies
- de.heuboe.asfinag:vmis2-base-parent ............... 2.20.0 -> 5.1.0
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.17 -> 1.16.27
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.4.6 -> 2.0.0
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.8.0 -> 1.1.0
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 3.1.0 -> 3.3.0
- change tests


## 1.4.0
**Changed**
- Rebuild the algorithm so that the set methods can be called in any order.
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.4.4 -> 1.4.6
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 3.0.0 -> 3.1.0

## 1.3.0
**Changed**
- de.heuboe.asfinag:vmis2-base-parent .............. 2.16.2 -> 2.17.0
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.4.3 -> 1.4.4
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.4.5 -> 3.0.0
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.15 -> 1.16.17
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ...0.7.2 -> 0.8.0
- Passing individual vehicle data to the algorithm to determine the speed of the slowest vehicle at the detector. 
  This slowest vehicle per detector is passed on to the publisher.
- JUnit test adjustments
- When a new infrastructure is received (setInfrastructure), the publisher is called with the new initInfrastructure method

## 1.2.0
**Changed**
- fix memory leak
- More logging adjustments

## 1.1.2
**Changed**
- Log: "... input records received" moved from info to debug because data is received individually per lane.

## 1.1.1
**Changed**
- Redesign logging: logging marker for lanes, log.info => log.debug
- Wrong error message thrown out: 
   "Data NOT deleted for id interval length {}, interval end {} although data should be available => may not happen !"
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.7 -> 1.16.9
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.6.2 -> 0.7.0
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.3.5 -> 2.3.6

## 1.1.0
**Changed**
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.6.0 -> 0.6.2
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.6 -> 1.16.7
- class InfraParameter: New parameter categoryBoundariesPkw and categoryBoundariesLkw

## 1.0.0
**Changed**
- SyncVdPublisher, interface publish/publishDiscardedData changed: added Maps for infrastructure states/parameter
- Tests
- Changes regarding logical/physical passivation and IntervalLengthValue

## 0.6.1
**Changed**
- see 0.6.0 (is broken)

## 0.6.0
**Changed**
- Interface SyncVdPublisher: Method publish: Changed parameter type of missingIds from List<Tuple3<String, Integer, Instant>> to List<Tuple4<String, Integer, Instant, Integer>>. Tuple4 contains additionally the TLS version.
- Adjust JUnit tests
- Sonar
- vmis2-base-parent v2.11.7
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.2.0 -> 1.4.1
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.3.2 -> 0.6.0
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.1.0 -> 2.3.5


## 0.5.5
**Changed** 
- vmis2-base-parent v2.9.2
- vmis2-tls-proto-interface v2.1.0
- Test adjustments due to new tls-proto-interface.
- vmis2-infrastructure-base v1.2.0

## 0.5.4
**Changed** 
- Methods setInfrastructure, setInfraState and setInfraParameter
  not only check for null, but also whether the container is empty.

## 0.5.3
**Changed** 
- Update various dependencies and e.g. vmis2-base-parent v2.9.1

## v0.5.2

**Changed**
- Logging
- Delete internal collected data in intervalTimeoutTrigger
- IntervalLengthValue: Error-Logging for invalid interval length.
- TlsInputData: Prevent NullPointerException (invalid interval length)

## v0.5.1

**Changed**
- pom.xml: synchronise replaced by synchronize

## v0.5.0

**Added**
- Added azure-pipeline.yaml

**Changed**
- vmis2-base-parent: 2.7.0
- vmis2-infrastructure-base: 1.0.0


