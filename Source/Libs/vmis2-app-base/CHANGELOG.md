# Changelog

## v2.0.0
- Make CheckerHealthIndicator a component
  - Do not auto-configure CheckerHealthIndicator. Use component scan or create your own bean.
- upgrade to Spring Boot 3
- update dependencies:
  - ${akka.version} 2.6.20 -> 2.6.21
  - de.heuboe.asfinag:vmis2-configservice-interface 1.16.21 -> 1.16.22
  - de.heuboe.asfinag:vmis2-control-datamodel 3.1.9 -> 3.2.1
  - eu.vmis-ehe.asfinag:vmis2-paramservice-interface 1.11.0 -> 1.13.0
  - io.vavr:vavr 0.9.2 -> 0.10.4
  - de.heuboe.asfinag:vmis2-base-parent 3.2.0 -> 4.1.0
- remove dependencies:
  - io.micrometer:micrometer-registry-prometheus 
  - com.fasterxml.jackson.datatype:jackson-datatype-jsr310
  - com.fasterxml.jackson.core:jackson-databind
  - com.fasterxml.jackson.dataformat:jackson-dataformat-yaml
  - net.devh:grpc-client-spring-boot-starter
  - org.springframework.boot:spring-boot-starter-validation
  - org.springframework.boot:spring-boot-starter-log4j2
- add dependencies:
  - org.springframework.boot:spring-boot-autoconfigure
  - org.springframework.boot:spring-boot-actuator
- replace vmis2-id-generator by heuboe-idgenerator
- use test scoped:
  - org.springframework.boot:spring-boot-starter-web
  - org.springframework.boot:spring-boot-starter-actuator
  - com.typesafe.akka:akka-testkit_2.13
- Add new constructor for InitialTopicReader that contains specific topics instead of a pattern.
  - Specifying concrete topics can be more performant, since only the specified topics have to be requested.
    Using a pattern always results in all topics being requested and then filtered according to the pattern.
- Make some minor refactoring.
- Use java 17 features
  - record instead of @Value (lombok) 
  - toList() instead of Collectors.toList()
- add initial readme


## v1.3.0
- add new ParameterUtils method to get value from a Double Array.
-  de.heuboe.asfinag:vmis2-base-parent ................ 3.0.0 -> 3.2.0


## v1.2.0
- add HealthProperties, set default CheckerHealthIndicator-timeout to 5000ms (instead of 100ms) ->
    add "@ComponentScan(basePackages = {"de.heuboe.asfinag.control.base.config"})" to your app configuration


## v1.1.1
- make actor system in AbstractInfraObjPublishActor public

## v1.1.0
- change input parameter of AbstractInfraObjPublishActor, change InfrastructurManager to List of infrastructure object.
- Copy List to new list to get rid of concurrent modification exception
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.20 -> 1.16.21
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.1.6 -> 3.1.9


## v1.0.1
- de.heuboe.asfinag:vmis2-infrastructure-base ... 1.5.0 -> 1.5.1
- change abstractInfraObjPublishActor, use iterator to iterate over infra structure objects, 
- to get rid of concurrent modification exception.

## v1.0.0
- AbstractParameterActor works only with topic names as described in the constructor. Patterns are no longer permitted.
- A new parameter was added to AbstractParameterActor to restrict the concurrency.
- A new instance handler was added (MemoryReducedParametersInstanceHandler). It should be used to reduce the memory
  consumption of cached parameters in AbstractParameterActor.
- use java 17
- dependency updates:
  - de.heuboe.asfinag:vmis2-base-parent 2.19.0 -> 3.0.0
  - eu.vmis-ehe.asfinag:vmis2-paramservice-interface 1.10.2 -> 1.11.0

## v0.11.2
- change  InfraObjPublishActor to abstract class 
- remove tunnel datamodel
- remove geo manager interface
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.19 -> 1.16.20


## v0.11.1
- ParameterUtils.getParameter: private -> public

## v0.11.0
- add a synchronized version of the InitialTopicReader
- update dependencies
  - de.heuboe.asfinag:vmis2-base-parent 2.16.3 -> 2.19.0
  - ${akka.version} 2.6.17 -> 2.6.20
  - de.heuboe.asfinag:vmis2-configservice-interface 1.16.17 -> 1.16.19
  - eu.vmis-ehe.asfinag:vmis2-paramservice-interface 1.8.0 -> 1.10.2
  - org.awaitility:awaitility 4.0.0 -> 4.2.0

## v0.10.1
**Add**
- add InfraObjPublishActor that publish initialized infrsatructure object and infrastructure objects with a failure.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.17 -> 1.16.19
- de.heuboe.asfinag:vmis2-control-datamodel .... 3.1.3 -> 3.1.4
- de.heuboe.asfinag:vmis2-infrastructure-base ... 1.4.8 -> 1.4.9
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface ..... 1.8.0 -> 1.10.2


## v0.10.0
**Changed**
- ParameterActor sends MissingParameter messages in publishParameters() if no parameter exists, 
  where the parameter topic itself exists. Usually the parameters of one instance of one algorithm 
  for one road are missing. 

## v0.9.3
**Changed**
- Logging
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface ...... 1.5.0 -> 1.8.0

## v0.9.2
**Changed**
- CheckerHealthIndicator returns OUT_OF_SERVICE instead of UNKNOWN (okd ignores UNKNOWN)

## v0.9.1
**Changed**
- Nullpointer-safe logging, if consumer finds NO topics.

## v0.9.0
**Changed**
- initialize AbstractParameterActor.waitForInitialParameters with TRUE to prevent wrong InitialParametersRead-answers

## v0.8.0
- ParameterUtils: add getStringArrayParameter

## v0.7.2
**Changed**
- ParameterUtils: use Optional.ofNullable instead of Optional.of

## v0.7.1
**Changed**
- Update dependencies
    - de.heuboe.asfinag:vmis2-base-parent 2.13.1 -> 2.14.0
    - de.heuboe.asfinag:vmis2-configservice-interface 1.16.12 -> 1.16.13
    - akka.version 2.6.15 -> 2.6.17
- Reduce memory usage of initial topic reader

## v0.7.0
**Added**
- InitialTopicReader: add isInitialized-method with headerValues-map

## v0.6.0
**Added**
- ParameterUtils: getLongParameter
- Utils: createActorName

## v0.5.2
**Changed**
- Fix checker health indicator test

## v0.5.1
**Changed**
- Adapt checker health indicator
    - Add autoconfiguration
    - Set most of the logs to debug.
    - Add parameter for timeout
    - Add tests

## v0.5.0
**Changed**
- mandatory version update, because of a major change in the updated akka version below
- de.heuboe.asfinag:vmis2-paramservice-interface ...... 0.59.0 -> 0.60.0

## v0.4.3
**Changed**
- de.heuboe.asfinag:vmis2-base-parent ................. 2.11.7 -> 2.12.0
- com.typesafe.akka:akka-actor_2.12 to com.typesafe.akka:akka-actor_2.13
- com.typesafe.akka:akka-slf4j_2.12 to com.typesafe.akka:akka-slf4j_2.13
- com.typesafe.akka:akka-testkit_2.12 to com.typesafe.akka:akka-testkit_2.13

## v0.4.2
**Changed**
- ParameterActor: log missing topics
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.3 -> 1.16.9
- de.heuboe.asfinag:vmis2-control-datamodel ............. 2.0.2 -> 2.1.0
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.51.0 -> 0.59.0

## v0.4.1
**Changed**

- Better Logging

## v0.4.0
** Added **
- HealthMessage and Health Indicator

## v0.3.1
** Added **
- Feature to read initial data from a certain timestamp with the InitialTopicReader

## v0.3.0
** Changed**
- ParameterUtils: add logMarker to methods

## v0.2.1
** Added**
- add additional Utils method to check if parameter value are legal.


## v0.2.0
** Added**
- ParameterUtils to read parameter

** Changed**
- New seek to latest. Don't seek but calculate offset by using endOffsets.

## v0.1.24
- adapted the exitingSuperVisorActor to get the child supervisorActor

## v0.1.24
- add additional Utils method to check if parameter value are legal.

## v0.1.23
- add Utils methods to check if parameter value are legal.

## v0.1.22
- add ExitingSupervisorActor
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.4 -> 2.11.6
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.15.8 -> 1.16.2
- de.heuboe.asfinag:vmis2-control-datamodel ......... 1.19.12 -> 1.19.14
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.46.0 -> 0.51.0



## v0.1.21
- add ApplicationContext to ConcurrentMessageListenerContainer in AbstractParameterActor
  to fix a bug occurred with new Spring version. 

## v0.1.20
- fixed a bug in the Utils SeekToLatest Function with Consumer multiple topics usage

## v0.1.19
- InitialTopicReader: Reset Offset in onPartitionAssigned as long as the partition is not initially read.
- de.heuboe.asfinag:vmis2-control-datamodel ......... 1.19.10 -> 1.19.12


## v0.1.18
** Changed **
- Added BugFix of BugFix for VMIS2 InternalBug with ID 15589

  InitialTopicReader didn't work correctly if not all partitions of one Topic-Pattern
  were assigned at once.
  
   
## v0.1.17
** Changed**
- bugfix: RawParameterActor only Work for Topic with one Key, change to run also with multi keys. 
** Remove**
- remove SignOverlayAppInstanceHandler because he only chache paramter for one street.

## v0.1.16
** Changed**
- bugfix: reading kafka client id
- clean up parameter actor on stopping

## v0.1.15
** Changed**
- ParameterActor: working without concrete system (use system pattern)
- ParameterActor: working without concrete instances
- added overloaded isInitialized() function

## v0.1.14
- change log level info to debug where possible.
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.2 -> 2.11.3
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.15.7 -> 1.15.8
- de.heuboe.asfinag:vmis2-control-datamodel ........... 1.19.6 -> 1.19.7
- de.heuboe.asfinag:vmis2-kafka-protopojo-converter ..... 1.6.0 -> 1.6.1
- de.heuboe.asfinag:vmis2-log4j2-extension .............. 1.0.3 -> 1.1.0
- eu.vmis-ehe.asfinag:vmis2-paramservice-interface .... 0.36.0 -> 0.44.0

## v0.1.13
- add fix string to kafka group id for parameter actors to avoid CommitFailedException

## v0.1.12
- use kafka group id for parameter actors
- parameter actors answer to sender instead of akka event bus

## v0.1.11
**Added**
- add additional writePb2Json-method with additional parameter path

## v0.1.10
- bugfix not reading null payloads

## v0.1.9
- bugfix in AbstractParameterActor: no matching topicPartitions in startReadParameters

## v0.1.8
- de.heuboe.asfinag:vmis2-base-parent .............. 2.10.4 -> 2.11.0

## v0.1.7
- add timestamp to the consumer name in initial topic reader.

## v0.1.6
- add log to see which topic in initial topic reader doesn't read.
- de.heuboe.asfinag:vmis2-base-parent .............. 2.10.3 -> 2.10.4
- de.heuboe.asfinag:vmis2-constants ..................... 0.0.2 -> 0.0.4
- de.heuboe.asfinag:vmis2-control-datamodel ........... 1.18.5 -> 1.19.1
- de.heuboe.asfinag:vmis2-kafka-protopojo-converter ..... 1.5.1 -> 1.6.0

## v0.1.5
- ParameterActor: Send IntialParametersRead message after PublishParametersRequest
## v0.1.4
- edit initial topic reader to handle multiple topics in one.

## v0.1.3
**Changed**
- add in abstractParmeterActor.startReadParameters method an unambiguously consumer name.
- vmis2-control-datamodel v1.18.5
- Sonar


## v0.1.2
**Changed**
- vmis2-base-parent v2.10.3
- vmis2-log4j2-extension v1.0.3
- vmis2-control-datamodel v1.18.0
- Generalize ParameterActor
   - AbstractParameterActor--> Generic base class
   - ParameterActor --> Actor that handles PParameterClass
   - RawParameterActor --> Actor that handles ConsumerRecords
- Correct javadocs documentation
- Fixed some RAW-Type warnings etc.
- Add message InitialParametersRead published to the akka event stream after the actor has read all initial parameters.

## v0.1.1
* Added a new general purpose ParameterActor class

## v0.1.0
* initial commit 
