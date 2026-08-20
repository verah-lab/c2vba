# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.12.0
### Changed
- de.heuboe.asfinag:vmis2-base-parent ................ 5.2.0 -> 7.1.1
- Observe the allChanged flag in DataChange. Is set if the processes are to be completely 
  reinitialised (instead of reacting to individual changes).
**Added**
- DataChangeAllTest: Tests the observance of the allChanged flag.
  

## 1.11.0
### Changed
- change to pipeline .gitlab-ci.yml
- spring boot3: update base parent (5.2.0) and other dependencies:
    - de.heuboe.asfinag:vmis2-base-parent ............... 2.16.3 -> 5.0.1 (spring boot 3)
    - de.heuboe.asfinag:vmis2-app-base ..................... 0.10.0 -> 2.0.0
    - de.heuboe.asfinag:vmis2-synchronize-vd ....... 1.4.0 -> 1.5.0
    - de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.20 -> 1.16.27
    - de.heuboe.asfinag:vmis2-control-datamodel ............. 3.1.6 -> 3.5.1
    - de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.5.0 -> 2.0.1
    - de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.6.1 -> 4.6.2
    - de.heuboe.asfinag:vmis2-kafka-protopojo-converter ..... 1.9.0 -> 2.0.0
    - de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.8.1 -> 1.1.0
    - de.heuboe.asfinag:vmis2-tls-proto-interface ........... 3.1.1 -> 3.3.0
    - eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 1.10.2 -> 1.18.0
		
- rename javax => jakarta
- rename parameter methods
- rename kafka headers
- remove completeble-call()
- replace de.heuboe.vmis2.id.generator.IDGenerator by de.heuboe.idgenerator.generator.IDGenerator
- test adjustments
- not required Property(topicReplicationFactor) removed
- KafkaConfig.java: ProtoPojoKafkaMessageConverter deleted, because of circular references
- remove application-default*.yml
- organize imports


- **Working for all configured UZs, if required**
    - New property systemWideShortcut (default VRZ)
    - New property centreIdAllUZ (default -ALL-)=>If the centreId contains this string, then work for all UZs
    - Using other topic names. 
       - The system-wide shortcut is used to read the time synchronization parameters and 
       other topic names that previously contained the shortcut VRZ.
    - Time synchronization is sent with system-wide shortcut (kafka key)
    - The rVmzId is not checked(DataChange)
    - JUnit test ReadAllUZTest added
    - maven-surfire-plugin: a.o. forkCount:1 => makes the pipeline .gitlab-ci.yml work
    - The JUnit integration tests were mainly adapted with regard to setting the properties. These now correspond more to "real life" (use of centreTopic/systemWideShortCut)..
    


## v1.10.3
### Changed
- Renaming a tag(key/value) for a micrometer counter: numberLanes/total lanes + value of counter => 
  numberSensors/total vd sensors + value of counter.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.17 -> 1.16.20
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.1.2 -> 3.1.6
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.4.6 -> 1.5.0
- de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.6.0 -> 4.6.1
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.8.0 -> 0.8.1
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 3.1.0 -> 3.1.1
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 1.10.1 -> 1.10.2

## v1.10.2
- Control for not allowed interval length in TLS input adjusted.

## v1.10.1
- The lane id is now printed if the TLS input contains an incorrect interval length.

## v1.10.0
### Added
- MissingParamTest to test the new handling with the AbstractParameterActor(app-base v.0.10.0)

### Changed
- Now it is checked whether the centreId/centreTopic (property/secret) contains spaces and 
  if so a warning/error is logged.
- Changing handling with AbstractParameterActor
- Prevent consumer warnings in JUnit integration tests: 
  set @TestPropertySource(properties = {"spring.kafka.consumer.enable-auto-commit=false"})
- Add destroyMethod to ActorSystem configuration to force shutdown id context is shut down.
  (for JUnit integration tests)
- Logging
- de.heuboe.asfinag:vmis2-app-base ...................... 0.9.1 -> 0.10.0
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.0.9 -> 3.1.2
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 3.0.0 -> 3.1.0
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface ...... 1.5.0 -> 1.10.1
- de.heuboe.asfinag:vmis2-synchronize-vd ....... 1.3.0 -> 1.4.0


## v1.9.8
### Changed
- bugfix: Preventing an IndexOutOfBoundException in TlsSyncVdPublisher, if the number of speed classes in   
  the parameters does not match the number in the input data(version 4). 
  => corrected: if the number of speed classes + 1 in  the parameters does not match 
  the number in the input data(version 4)

## v1.9.7
### Changed
- Adjustments for new panel "short term collected data per road" in dashboard

## v1.9.6
### Changed
- bugfix: Preventing an IndexOutOfBoundException in TlsSyncVdPublisher, if the number of speed classes in   
  the parameters does not match the number in the input data(version 4). Since the implementation of the
  slow moving vehicle, the individual speed classes are accessed.
- Adjustments for new panel "short term collected data per road" in dashboard
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.0.8 -> 3.0.9
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.4.4 -> 1.4.6
  
## v1.9.5
### Changed
- de.heuboe.asfinag:vmis2-app-base ........ 0.9.0 -> 0.9.1 (Nullpointer-safe logging, if consumer finds NO topics.)
  
## v1.9.4
### Changed
- de.heuboe.asfinag:vmis2-app-base ........ 0.8.0 -> 0.9.0 (initialize
  AbstractParameterActor.waitForInitialParameters with TRUE to prevent wrong InitialParametersRead-answers)

## v1.9.3
### Added
- src/main/resources/template.config/application.yml: Added missing properties

### Changed
- Disturbing logging in the counter(metric) removed
- de.heuboe.asfinag:vmis2-constants ..................... 0.0.5 -> 0.0.6
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.0.7 -> 3.0.8


## v1.9.2
### Added
- Special metrics(counters) for Grafana dashboard, e.g. for correct, missing or discarded (too early, too late) FG1 short term data.

## v1.9.0
### Added
- Slow driving vehicles:
  Subscribe to single vehicle data and path them to the algorithm to determine the speed of the 
  slowest vehicle at the detector. This slowest vehicle per detector is passed on to the publisher.
  If no single vehicle data is available, the slowest vehicle may be determined from the speed 
  classes(version4).
  
### Changed
- The infrastructure is now no longer recopied every time you publish, but only on restart and when the 
  infrastructure has changed.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.16 -> 1.16.17
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.7.2 -> 0.8.0
  

## v1.8.1
### Changed
- BugFix: Nullpointer safety for vde_sensors without roadId

## v1.8.0
### Changed
- bugfix VM2-34010: re-init infrastructure (lane to road data) in TlsSyncVdPublisher after config update
- rebuild infrastructure:
    - simplify config-service requesting methods (use GetAllItems)
    - remove MQ layer from infrastructure requests (MQs do not appear in the whole app any more)

## v1.7.2
### Changed
- Switch from SupervisiorActor to ExitingSupervisorActor

## v1.7.1
### Changed
- de.heuboe.asfinag:vmis2-base-parent ....2.16.2 -> 2.16.3 (log4j: CVE-2021-44228)
- de.heuboe.asfinag:vmis2-log4j2-extension .............. 1.3.0 -> 1.3.1
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface ...... 1.3.0 -> 1.4.0
- log4j.xml changed for JUnit test (pattern: marker)
- logging adjustments
  - logging marker inserted
  - The received short time data version 1-6 are now logged with id and the latency for 
    processing is now inserted for all versions.
### Removed
   - property ignoreIntervals and its implementation, because it was not correct anyway

## v1.7.0
### Changed
- fix memory leak in algo
- More logging adjustments
- dependency updates:
    - de.heuboe.asfinag:vmis2-base-parent ....2.13.0 -> 2.16.2 (log4j: CVE-2021-44228)
    - de.heuboe.asfinag:vmis2-app-base ...................... 0.6.0 -> 0.8.0
    - de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.12 -> 1.16.15
    - de.heuboe.asfinag:vmis2-control-datamodel ............. 2.2.0 -> 3.0.1
    - de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.3.2 -> 4.6.0
    - de.heuboe.asfinag:vmis2-kafka-protopojo-converter ..... 1.7.0 -> 1.9.0
    - de.heuboe.asfinag:vmis2-log4j2-extension .............. 1.2.0 -> 1.3.0
    - de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.4.2 -> 2.4.5
    - eu.vmis-ehe.asfinag:vmis2-paramservice-interface ..... 0.61.0 -> 1.3.0

## v1.6.5
### Changed
- changed default error value of floats to -99999.0 and made it configurable in app-properties.

## v1.6.4
### Changed
- de.heuboe.asfinag:vmis2-synchronize-vd ................ 1.1.1 -> 1.1.2
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.59.0 -> 0.60.0
- Adjustments in tests

## v1.6.3
### Changed
- de.heuboe.asfinag:vmis2-app-base ...................... 0.4.1 -> 0.4.2

## v1.6.2
- Logging
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.7 -> 1.16.9
- de.heuboe.asfinag:vmis2-control-datamodel ............ 2.0.10 -> 2.1.0
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.6.2 -> 0.7.0
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.3.5 -> 2.3.6
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.57.0 -> 0.59.0
- de.heuboe.asfinag:vmis2-synchronize-vd ....... 1.1.0 -> 1.1.1

## v1.6.1
### Added
- health indicator for app start
### Changed
- fixed duplicate timer problem at actor start

## v1.6.0
### Added
- logical passivation parameter listening
- Integration of speed classes parameters(LVEGeschwindigkeitsklassenKurz)
### Changed
- Send global time synchronization (SYSSteuerSeqeunz), if necessary 
- AlgoActor:handleTlsOperatingParams: Bugfixing: Access to the correct interval length
- Shutdown old timer, when reinit algo; fix DataChangeTest
- Fix bug 18762 param change Zeitsynchronisation; new ParamChangeTest; fixed DataChangeTest;
- class InfraParameter: New parameter categoryBoundariesPkw and categoryBoundariesLkw
- Test adjustments due to the integration of speed classes parameters, writing discarded data and time synchronization
- de.heuboe.asfinag:vmis2-app-base ...................... 0.3.1 -> 0.4.1
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.6 -> 1.16.7
- de.heuboe.asfinag:vmis2-control-datamodel ............ 2.0.9 -> 2.0.10
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.55.1 -> 0.57.0
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.6.0 -> 0.6.2

## v1.5.1
### Changed
- fix default version number issue

## v1.5.0
### Added
- parameter for logical passivation
- supervisor actor with restart behavior; remember tls admin data and params to restart actor
- initial topic readers for administrative topics
- kafka client-id prefix
- property switch on/off the publishing of discarded data (writeDiscarded)
- Added TlsInputData.java, because is deleted in vmis-synchronize-vd v1.0.0.
### Changed
- log output
- properties as YAML
- cleaned code / modernized code according to newer apps
- de.heuboe.asfinag:vmis2-synchronize-vd ........... 0.6.1 -> 1.0.0
- Adjustments due to the change of InfraState(logical passivation) in vmis2-synchronize-vd v1.0.0
- Adjustments due to interface changes of SynVdPublisher(vmis2-synchronize-vd v1.0.0)
- Preparation of writing PShortTermCollectedTrafficCategoriesLane
- Take the TLS version from the parameters if fault values are written
### Removed
- SpringActorProducer

## v1.4.10
### Changed
- fixed message key and logging

## v1.4.9
### Changed
- Logging for missing ids: LOG.info => LOG.debug
- Logging for timeout trigger: LOG.debug => LOG.info
- Use laneId as message key for discarded data
- Asynchronous publishing
- Transfer of the TLS version from the parameters if fault values are written
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.5 -> 1.16.6
- de.heuboe.asfinag:vmis2-control-datamodel ............. 2.0.5 -> 2.0.8
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.4.0 -> 1.4.1
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.54.0 -> 0.55.1
- de.heuboe.asfinag:vmis2-synchronize-vd ........... 0.5.5 -> 0.6.1

## v1.4.8
### Changed
* update infrastructure when receiving relevant DataChanges
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.5 -> 2.11.7

## v1.4.7
### Changed
- de.heuboe.asfinag:vmis2-app-base ..................... 0.1.21 -> 0.3.0
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.1 -> 1.16.5
- de.heuboe.asfinag:vmis2-control-datamodel .......... 1.19.14 -> 2.0.5
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.3.1 -> 1.4.0
- de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.2.0 -> 4.3.2
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.5.6 -> 0.5.9
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.3.1 -> 2.3.5
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.50.0 -> 0.54.0

## v1.4.6
### Changed
- Sytem.exit(0), if data is received but the algo is not yet initialized.
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.4 -> 2.11.5
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.0 -> 1.16.1
- de.heuboe.asfinag:vmis2-control-datamodel ......... 1.19.12 -> 1.19.14
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.3.0 -> 2.3.1


## v1.4.5
- Throwing an exception if data is received but the algo is not yet initialized.
- de.heuboe.asfinag:vmis2-app-base .................... 0.1.14  -> 0.1.21
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.15.8 -> 1.16.0
- de.heuboe.asfinag:vmis2-control-datamodel .......... 1.19.8 -> 1.19.12
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 3.7.3 -> 3.7.6
- de.heuboe.asfinag:vmis2-infrastructure-base ........... 1.2.8 -> 1.3.1
- de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.1.0 -> 4.2.0
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.5.2 -> 0.5.6
- de.heuboe.asfinag:vmis2-tls-proto-interface ........... 2.2.0 -> 2.3.0
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.44.0 -> 0.50.0

## v1.4.4
### Changed
- fixed ignore of interval lenghts (filter operating params to prevent algo of writing expected default values))

## v1.4.3
- new release version because of release problems (-> see 1.4.2)

## v1.4.2
### Added
- new property to set ignore list of interval length (optional)
- new filter in infrastructure service to filter roadIds with string "null" (set defaultRoadId)

## v1.4.1
### Added
- new property to set 'defaultRoadId', which is set to infrastructure, if mq does not contains a roadId.
### Changed
- ProcessDescription
- made topic template properties clearer:
  SynchronizeVdProperties contains only .*TopicTemplates; AlgoContext contains solved topic names

## v1.4.0
### Changed
- Various adjustments due to the conversion to rVMZ_Wien. Among other things the GeoManger was removed, 
  because the ConfigService now also supplies an assignment of MQs/lanes to roads.
- vmis2-base-parent v2.11.1
- vmis2-configservice-interface v1.15.6
- vmis2-app-base v0.1.10
- vmis2-control-datamodel v1.19.3
- vmis2-grpc-interface-geomanager v3.7.1
- vmis2-infrastructure-base v1.2.6
- vmis2-tls-proto-interface v2.2.0


## v1.3.3
### Added
- AlgoActor:createReceive: match call for PSYSFehlerDUEList.class

### Changed
- vmis2-base-parent v2.11.0
- vmis2-app-base v0.1.7
- vmis2-configservice-interface v1.15.1
- vmis2-constants v0.0.5
- vmis2-control-datamodel v1.19.2
- vmis2-grpc-interface-geomanager v3.7.0
- vmis2-id-generator v1.1.6
- vmis2-infrastructure-base v1.2.5
- vmis2-jprotoc-transferinterface v4.1.0
- vmis2-kafka-protopojo-converter v1.6.0
- vmis2-receiving-processing-datamodel v0.4.1
- vmis2-tls-proto-interface v2.1.2
- vmis2-paramservice-interface v0.35.0

## v1.3.2
### Changed
- Sonar

## v1.3.1
### Changed
- Prevent a null pointer exception in the handle..-methods.
- vmis2-infrastructure-base v1.2.2
- vmis2-id-generator v1.1.5
- vmis2-paramservice-interface v0.31.0

## v1.3.0
### Changed
- Get parameter from Kafka with ParameterActor
- vmis2-base-parent v2.10.3
- vmis2-control-datamodel v1.18.5
- vmis2-id-generator v1.1.4
- vmis2-infrastructure-base v1.2.1
- vmis2-receiving-processing-datamodel v0.3.7
- geomanager.interface v3.5.1
- configservice.interface v1.13.4
- vmis2-constants v0.0.4
- vmis2-kafka-protopojo-converter v1.5.2

### Added
- vmis2-app-base 0.1.3

### Removed
- Tests in package *.it

## v1.2.0
### Changed
- geomanager.interface v3.4.1
- configservice.interface v1.13.3
- receiving-processing.datamodel v0.3.3
- control.datamodel v1.18.0
- Insert /heuboe/vmis2/ into docker image path
 
## v1.1.0
### Changed
- Seek to end for LVE input data topics. Read only new data.
- 
## v1.0.0
### Changed
- Adapted to new topic structure: Use only one topic for a datakind and use headers to allow filtering on streets. 
- vmis2-base-parent v2.9.2
- vmis2-tls-proto-interface v2.1.0
- vmis2-infrastructure-base v1.2.0
- Bugfixing and harmonization infrastructure initialization 
- Adapt logging
- Adapt tests

### Added
- Consideration (Kafka listener etc.) of SYSFehlerDUE. This status of the remote station is now also included in the InfraState. For this purpose, the status calculation has been changed and a part has been outsourced to a method.

### Removed
- Infrastructure.java


## v0.1.8
### Changed 
- Update various dependencies a.o. vmis2-base-parent v2.9.1

## v0.1.7
- change field 'id' to 'iid'
- Bugfix: use VERSION_INCORRECT instaed of UNRECOGNIZED

## v0.1.6
- Bugfix: interval end scheduler triggered with empty interval length list

## v0.1.5
- log4j2.xml created in src/test/resources, 
- Logging adjusted

## v0.1.4
- fixed bug to prevent quartz scheduler from creating multiple worker threads in order to not trigger timeouts and interval ends multiple times
- filter tls input data: take no input data with incompatible interval length (e.g. '129')
- report only changes of incoming 'DeFehler' to the algo. If error state did not change: no reporting!
- new version of vmis2-synchronize-vd (algo)
- updated heuboe dependencies

## v0.1.3

### Added
- CHANGELOG.md

### Changed
- Added new releases of libraries
- Major adjustments due to infrastructure harmonisation
- Adjustments due to the change: "type parameter deleted in the constructors for Road, DetectionSite, Lane, RouteStation"



