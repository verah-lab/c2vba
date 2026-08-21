# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 4.3.2 - 2026-03-11

### Changed

- Version for release

## 4.3.1 - 2026-01-08

### Added
- Added groupId to artifact in pom which changed due to other parent pom

## 4.3.0 - 2026-01-08

### Changed
- Update to [tls-proto-interface v5.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=48412) (was 4.3.0)
- Update to [heuboe-base-parent v1.6.3](http://pdb.heuboe.hbintern/pdb/#version;id=51721) (was tls-parent-pom v3.4.0)
- Update to [tls-grammar v3.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=47777) (was 3.0.0)

### Added
- Grammar extension for getAtIndex function
- Test for this function and assignment to array variable

## 4.2.0 - 2025-04-15
### Changed
- Update to [tls-parent-pom v3.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=50293) (was 3.0.0)
- Update to [tls-proto-interface v4.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=50275) (was 4.0.0)
- Update to [heuboe-kafka-test-utils v2.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=50244) (was 2.0.0)

## 4.1.0 - 2024-11-19
### Added
- Add new block property `autoFillTlsTime` to automatically set the current time under certain circumstances
- Add function `dateTime` to set a date and time value for a field

## 4.0.1 - 2024-10-14
### Changed
- Renovate -> Update to de.heuboe.tls:tls-kafka-operator v5.0.1 (was 5.0.0)

## 4.0.0 - 2024-08-07
### Changed
- Update to Java 21 (was Java 11)
- Update to spring-boot 3.3.x (was 2.7.x)
- Update to [tls-parent-pom v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48371) (was 2.2.0)
- Update to [tls-cfglib v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48425) (was 2.3.0)
- Update to [tls-grammar v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48440) (was 2.2.0)
- Update to [tls-kafka-operator v5.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48433) (was 4.0.0)
- Update to [tls-proto-interface v4.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48412) (was 3.5.0)
- Update to [vmis2-kafka-protopojo-converter v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=44713) (was 1.9.0)
- Update to [vmis2-tls-cfggetter v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48432) (was 1.5.0)
- Update to [heuboe-kafka-test-utils v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=44152) (was 1.3.0)
- Update to mockito-inline v5.2.0 (was 3.3.3)

## 3.0.1 - 2024-06-04
### Changed
- Change identifier for previous state of an object from `-` to `*` because `-` can now be part of the lexer rule IDENTIFIER
- Update to [tls-parent-pom v2.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=47725) (was 2.0.2)
- Update to [tls-cfglib v2.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=47731) (was 2.1.0)
- Update to [tls-grammar v2.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=47777) (was 2.1.1)
- Update to [tls-kafka-operator v4.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=47800) (was 3.2.0)
- Update to [tls-proto-interface v3.5.0](http://pdb.heuboe.hbintern/pdb/#version;id=47738) (was 3.3.0)
- Update to [vmis2-tls-cfggetter v1.6.0](http://pdb.heuboe.hbintern/pdb/#version;id=46935) (was 1.5.0)

## 3.0.0 - 2024-02-09
### Added
- Add new block property `name` that supports strings that will be added to the logging if an object is sent

### Changed
- Fix the `processTime` update for every touched object
- Update handling of `tlsTime` to make a correct copy from the source object
- Changed `SequencerConfig` interface to a class and implement most of the methods inside
- Replace e21x-tls-proto-interface with project neutral tls-proto-interface for tests
- Update to [tls-cfglib v2.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=46510) (was 1.1.2)
- Update to [vmis2-tls-cfggetter v1.5.0](http://pdb.heuboe.hbintern/pdb/#version;id=40658) (was 1.1.1)
- Renovate -> Update to de.heuboe.tls:tls-kafka-operator v3.3.0 (was 3.2.0)

## 2.4.0 - 2022-11-10
### Changed
- Update trigger time of flops from seconds to milliseconds
- Update to [tls-parent-pom v2.0.2](http://pdb.heuboe.hbintern/pdb/#version;id=41444) (was 2.0.1)
- Update to [tls-kafka-operator v3.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=42538) (was 3.1.0)

## 2.3.0 - 2022-09-22
### Added
- Add setting `PROTO_OBJECT` values for `GenericProtoObjects`

### Changed
- Update location of keyword `out` in definition block for `copy to` statement in respect to changes in commit [671bcde1](https://gitlab.heuboe.hbintern/Tls/tls-sequencer/-/commit/671bcde15d98eaf5f26f560800f411787ce5960a)
- Update to [tls-grammar v2.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=42090) (was 2.0.0)
- Update to [tls-cfglib v1.1.2](http://pdb.heuboe.hbintern/pdb/#version;id=40969) (was 1.1.1)
- Update to [tls-kafka-operator v3.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=39296) (was 3.0.0)
- Update to [vmis2-configservice-interface v1.16.18](http://pdb.heuboe.hbintern/pdb/#version;id=41867) (was 1.16.17)
- Update to [e21x-tls-proto-interface v1.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=41675) (was 1.2.1)

## 2.2.1 - 2022-05-02
### Changed
- Fix error printing if parsing a script part failed

## 2.2.0 - 2022-05-02
### Added
- Add flop storage clearing schedule
- Add abstract class `Function` as new `Expression` to support function implementations in scripts
- Add function `isKri` to check if a passed id belongs to a KRI device

### Changed
- Update to [vmis2-configservice-interface v1.16.17](http://pdb.heuboe.hbintern/pdb/#version;id=40305) (was 1.16.15)
- Update to [vmis2-tls-cfggetter v1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=40658) (was 1.0.10)
- Update to [e21x-tls-proto-interface v1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=39930) (was 1.1.0)

## 2.1.0 - 2022-02-09
### Changed
- Update `SequencerConfig` to extend `INotificationToApp` to support reaction on config service changes
- Update to [tls-parent-pom v2.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=39224) (was 2.0.0)
- Update to [tls-cfglib v1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=39320) (was 1.1.0)
- Update to [e21x-tls-proto-interface v1.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=39471) (was 1.0.1)

## 2.0.0 - 2022-01-24
### Added
- Add new variable `ArrayVariable` to enable usage of arrays in script as values for parameter

### Changed
- Update to Java 11 (was Java 8)
- Fix message printing if formatting fails because of missing `AccessVariable`
- Move position of message block in script from flop to statement
- Update flop mechanism to support sending multiple objects in one flop context
- Update internal flop key for MonoFlop to enable multiple MonoFlop's for the same request object
- Update message parameter to accept array variables with index definition
- Update array index to accept variables
- Update to [tls-parent-pom v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39224) (was 1.1.0)
- Update to [tls-grammar v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39308) (was 1.1.7)
- Update to [tls-cfglib v1.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=39320) (was 1.0.14)
- Update to [tls-kafka-operator v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39296) (was 2.1.2)
- Update to [vmis2-configservice-interface v1.16.15](http://pdb.heuboe.hbintern/pdb/#version;id=38943) (was 1.16.14)
- Update to [vmis2-tls-cfggetter v1.0.10](http://pdb.heuboe.hbintern/pdb/#version;id=39318) (was 1.0.9)

## 1.10.0 - 2021-12-14
### Added
- Add new ea type `DEs der KRI` to send messages to all data terminals of a KRI

## 1.9.2 - 2021-12-13
### Changed
- Fix memory leak in Parser class
- Update log message if parsing a script fails because of syntax errors
- Update to [tls-kafka-operator v2.1.2](http://pdb.heuboe.hbintern/pdb/#version;id=38712) (was 2.1.1)

## 1.9.1 - 2021-12-06
### Changed
- Fix handling of sequencer messages from other sequencer instances
- Reduce log level for receiving messages from other sequencer instances

## 1.9.0 - 2021-12-02
### Added
- Add new property `de.heuboe.tls.sequencer.header.sequencerContent` to differ between messages of different sequencer instances
- Add error message if parsing of the current object failed

### Changed
- Update location of keyword `out` in definition block
- Update handling of history objects to also accept history results with more than one object
- Move `SequencerSendingService` from `utils` package to `service` package
- Fix problem with target topics that are defined in scripts with the keyword `via`
- Update to [tls-kafka-operator v2.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=38619) (was 1.9.2)
- Update to [vmis2-configservice-interface v1.16.14](http://pdb.heuboe.hbintern/pdb/#version;id=38600) (was 1.16.13)
- Update to [vmis2-tls-cfggetter v1.0.9](http://pdb.heuboe.hbintern/pdb/#version;id=37973) (was 1.0.8)
- Update to [e21x-tls-proto-interface v1.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=38556) (was 0.1.5)

## 1.8.7 - 2021-11-30
### Changed
- Revert to [tls-kafka-operator v1.9.2](http://pdb.heuboe.hbintern/pdb/#version;id=36345) (was 2.0.0)

## 1.8.6 - 2021-11-12
### Changed
- Update string matching in script

## 1.8.5 - 2021-11-08
### Changed
- Fix bug that override the object direction of previous objects
- Switch from [vmis2-tls-proto-interface v2.4.4](http://pdb.heuboe.hbintern/pdb/#version;id=37809) dependency to [e21x-tls-proto-interface v0.1.5](http://pdb.heuboe.hbintern/pdb/#version;id=37796) for tests

## 1.8.4 - 2021-11-02
### Changed
- Update to [tls-parent-pom v1.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=37868) (was 1.0.11)
- Update to [tls-cfglib v1.0.14](http://pdb.heuboe.hbintern/pdb/#version;id=37914) (was 1.0.13)
- Update to [tls-cfgsvc-bridge v0.10.4](http://pdb.heuboe.hbintern/pdb/#version;id=37902) (was 0.10.3)
- Update to [tls-grammar v1.1.7](http://pdb.heuboe.hbintern/pdb/#version;id=37901) (was 1.1.6)
- Update to [tls-proto-parser v1.4.7](http://pdb.heuboe.hbintern/pdb/#version;id=37893) (was 1.4.6)
- Update to [tls-kafka-operator v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=37894) (was 1.9.2)
- Update to [vmis2-configservice-interface v1.16.13](http://pdb.heuboe.hbintern/pdb/#version;id=37713) (was 1.16.12)
- Update to [vmis2-tls-proto-interface v2.4.4](http://pdb.heuboe.hbintern/pdb/#version;id=37809) (was 2.4.3)
- Update to [lombok v1.18.22](https://github.com/rzwitserloot/lombok/releases/tag/v1.18.22) (was 1.8.16)

## 1.8.3 - 2021-10-26
### Added
- Add new ea type `Knoten der KRI` to send messages to all outstation of a KRI

### Changed
- Update script loading mechanism to be case insensitive
- Update to [tls-cfglib v1.0.13](http://pdb.heuboe.hbintern/pdb/#version;id=36768) (was 1.0.11)
- Update to [vmis2-tls-proto-interface v2.4.3](http://pdb.heuboe.hbintern/pdb/#version;id=37453) (was 2.3.6)
- Update to [vmis2-kafka-protopojo-converter v1.9.0](http://pdb.heuboe.hbintern/pdb/#version;id=37218) (was 1.6.1)
- Update to [vmis2-configservice-interface v1.16.12](http://pdb.heuboe.hbintern/pdb/#version;id=36790) (was 1.16.10)
- Update to maven-dependency-plugin v3.2.0 (was 3.1.2)

## 1.8.2 - 2021-10-04
### Changed
- Update string in scripts to support `-` symbol and whitespaces

## 1.8.1 - 2021-08-16
### Changed
- Fix id used for objects that should be checked against a history entry

## 1.8.0 - 2021-08-16
### Changed
- Update handling of `tlsTime` and `processTime` for new objects to take the current time instead of the time of the
  origin object
- Update behaviour of handling history objects to support more than one data type in an if statement
- Update to [tls-kafka-operator v1.9.2](http://pdb.heuboe.hbintern/pdb/#version;id=36345) (was 1.9.1)

### Removed
- Remove copy of `jobnummer` from source object per default

## 1.7.2 - 2021-08-03
### Added
- Add new properties `de.tls.sequencer.responseRetries` and `de.tls.sequencer.responseTimeout`
- Add template `application.yml` to `resources/template/config`

### Changed
- Update logging for failed history requests to print the object id

## 1.7.1 - 2021-07-30
### Changed
- Update to [tls-kafka-operator v1.9.1](http://pdb.heuboe.hbintern/pdb/#version;id=35600) (was 1.9.0)

### Removed
- Remove config change from config interface to let the project decide to use that feature or not

## 1.7.0 - 2021-06-15
### Added
- Add tests for config change

### Changed
- Update `SequencerConfig` to extend `INotificationToApp` to support reaction on config service changes

## 1.6.0 - 2021-06-09
### Added
- Add tests for handling history objects

### Changed
- Update some log messages
- Update to [tls-kafka-operator v1.9.0](http://pdb.heuboe.hbintern/pdb/#version;id=35445) (was 1.7.3)

### Removed
- Remove `akka-actor_2.12` dependency because it will be delivered by `tls-kafka-operator`

## 1.5.6 - 2021-06-07
### Changed
- Fix NullPointerException if `testModePath` does not exist

## 1.5.5 - 2021-06-04
### Changed
- Update to [tls-cfglib v1.0.11](http://pdb.heuboe.hbintern/pdb/#version;id=35303) (was 1.0.10)
- Update to [tls-kafka-operator v1.7.3](http://pdb.heuboe.hbintern/pdb/#version;id=35336) (was 1.7.2)
- Update to [vmis2-tls-proto-interface v2.3.6](http://pdb.heuboe.hbintern/pdb/#version;id=33777) (was 2.3.5)

## 1.5.4 - 2021-05-10
### Changed
- Update to [tls-kafka-operator v1.7.2](http://pdb.heuboe.hbintern/pdb/#version;id=34974) (was 1.7.0)
- Update to [tls-cfglib v1.0.10](http://pdb.heuboe.hbintern/pdb/#version;id=34973) (was 1.0.9)
- Update to [tls-cfgsvc-bridge v0.10.3](http://pdb.heuboe.hbintern/pdb/#version;id=34950) (was 0.10.2)

## 1.5.3 - 2021-04-20
### Changed
- Loading scripts from `testModePath` will disable loading of normal scripts from `path`
- Update to [tls-kafka-operator v1.7.0](http://pdb.heuboe.hbintern/pdb/#version;id=34803) (was 1.6.4)
- Update to [tls-cfgsvc-bridge v0.10.2](http://pdb.heuboe.hbintern/pdb/#version;id=34800) (was 0.10.1)

## 1.5.2 - 2021-03-31
### Added
- Add support for stage and system specific scripts

### Changed
- Duplicated result objects (same data type and same id) will be removed so only the first assembled object remains
  (this enables script block override)
- Update order of loading scripts so global scripts will always be processed last
- Update handling of test scripts to enable loading side loading via specific file path
- Update script loading mechanism to load scripts into memory at sequencer start instead of loading while object parsing
- Update to [tls-kafka-operator v1.6.4](http://pdb.heuboe.hbintern/pdb/#version;id=34586) (was 1.6.3)

## 1.5.1 - 2021-03-18
### Added
- Add properties `de.tls.sequencer.script.stageName` and `de.tls.sequencer.script.testStage` to enable a finer
  granularity of script loading

## 1.5.0 - 2021-03-18
### Changed
- Update handling of scripts to enable script loading via system name definition
- Update to [tls-cfglib v1.0.9](http://pdb.heuboe.hbintern/pdb/#version;id=33947) (was 1.0.6)
- Update to [tls-cfgsvc-bridge v0.10.1](http://pdb.heuboe.hbintern/pdb/#version;id=33691) (was 0.9.1)
- Update to [vmis2-tls-proto-interface v2.3.5](http://pdb.heuboe.hbintern/pdb/#version;id=33777) (was 2.3.4)

## 1.4.2 - 2021-03-10
### Changed
- Fix handling for history objects
- Update to [tls-kafka-operator v1.6.3](http://pdb.heuboe.hbintern/pdb/#version;id=34375) (was 1.6.1)

## 1.4.1 - 2021-02-02
### Changed
- Update path for script loading to default `/config` and make it configurable again via properties

## 1.4.0 - 2021-02-01
### Added
- Add support for ea type definitions without target id to use id of executing object
- Add new ea type `Knoten des DEs` that allows to send a message to the corresponding node of an object
- Add new ea type `KDEs des Knoten` that allows to send a message to the corresponding eas of a specific function group
  for a node
- Add abort condition for script parsing if result was filled by current script

### Changed
- Update to [tls-grammar v1.1.6](http://pdb.heuboe.hbintern/pdb/#version;id=33809) (was 1.1.4)
- Update to [tls-kafka-operator v1.6.1](http://pdb.heuboe.hbintern/pdb/#version;id=33707) (was 1.5.6)
- Update to [tls-proto-parser v1.4.6](http://pdb.heuboe.hbintern/pdb/#version;id=33806) (was 1.4.5)
- Update object handling in script block without previous condition statement
- Update internal handling of objects as set
- Reduce logging from WARN to DEBUG for reading data from config service for an EA id

## 1.3.0 - 2021-01-25
### Added
- Add support for In Out object direction in script to differ between read and send direction of topics
- Add support for target topic definition

### Changed
- Rename property `de.heuboe.tls.sequencer.topicPrefix` to `de.heuboe.tls.sequencer.receive.topic.prefix`
- Rename property `de.heuboe.tls.sequencer.topicSuffix=` to `de.heuboe.tls.sequencer.receive.topic.suffix`
- Update to [tls-kafka-operator v1.5.6](http://pdb.heuboe.hbintern/pdb/#version;id=33628) (was 1.5.3)
- Update to [tls-proto-parser v1.4.5](http://pdb.heuboe.hbintern/pdb/#version;id=33331) (was 1.4.3)
- Update to [tls-grammar v1.1.4](http://pdb.heuboe.hbintern/pdb/#version;id=33657) (was 1.1.3)
- Update to [vmis2-tls-proto-interface v2.3.4](http://pdb.heuboe.hbintern/pdb/#version;id=33440) (was 2.3.3)
- Update to [maven-antrun-plugin v3.0.0](https://github.com/apache/maven-antrun-plugin/releases/tag/maven-antrun-plugin-3.0.0) (was 1.3)

## 1.2.3 - 2020-12-08
### Added
- Add `noSendMode` property to disable sending of messages to Kafka

### Changed
- Update to [tls-parent-pom v1.0.11](http://pdb.heuboe.hbintern/pdb/#version;id=33040) (was 1.0.9)
- Update to [tls-grammar v1.1.3](http://pdb.heuboe.hbintern/pdb/#version;id=33043) (was 1.1.2)
- Update to [tls-kafka-operator v1.5.3](http://pdb.heuboe.hbintern/pdb/#version;id=33042) (was 1.5.2)
- Update to [tls-proto-parser v1.4.3](http://pdb.heuboe.hbintern/pdb/#version;id=33041) (was 1.4.2)
- Update to [vmis2-tls-proto-interface v2.3.3](http://pdb.heuboe.hbintern/pdb/#version;id=32976) (was 2.3.1)

## 1.2.2 - 2020-11-18
### Changed
- Update to [tls-parent-pom v1.0.9](http://pdb.heuboe.hbintern/pdb/#version;id=32753) (was 1.0.8)
- Update to [tls-grammar v1.1.2](http://pdb.heuboe.hbintern/pdb/#version;id=32756) (was 1.1.1)
- Update to [tls-kafka-operator v1.5.2](http://pdb.heuboe.hbintern/pdb/#version;id=32762) (was 1.5.1)
- Update to [tls-proto-parser v1.4.2](http://pdb.heuboe.hbintern/pdb/#version;id=32755) (was 1.4.1)
- Update to [vmis2-tls-proto-interface v2.3.1](http://pdb.heuboe.hbintern/pdb/#version;id=32669) (was 2.3.0)

## 1.2.1 - 2020-11-09
### Added
- Add sending of messages to message management in `ThrowingErrorListener` to send parser errors

### Changed
- Update to [tls-parent-pom v1.0.8](http://pdb.heuboe.hbintern/pdb/#version;id=32172) (was 1.0.5)
- Update to [tls-kafka-operator v1.5.1](http://pdb.heuboe.hbintern/pdb/#version;id=32470) (was 1.4.3)
- Update to [vmis2-tls-proto-interface v2.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=32049) (was 2.2.1)
- Update to [akka-actor\_2.12 v2.6.10](https://akka.io/blog/news/2020/10/09/akka-2.6.10-released) (was 2.6.9)
- Update `snakeyaml` dependency to be used from parent dependency management

### Removed
- Remove `log4j2.xml` resource file

## 1.2.0 - 2020-10-05
### Added
- Add an interface for message management
- Add message management to sequencer message class

### Changed
- Update IDGenerator from bean to local class creation in `SequencerSendingService`
- Update `SequencerConfig` interface to add `FlopStorage` to `KafkaOperatorService` bean creation
- Update to [tls-parent-pom v1.0.5](http://pdb.heuboe.hbintern/pdb/#version;id=31909) (was 1.0.4)
- Update to [tls-grammar v1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=31933) (was 1.1.0)
- Update to [tls-kafka-operator v1.4.3](http://pdb.heuboe.hbintern/pdb/#version;id=31936) (was 1.4.2)
- Update to [tls-proto-parser v1.4.1](http://pdb.heuboe.hbintern/pdb/#version;id=31931) (was 1.4.0)

### Removed
- Remove redundant dependency `heuboe-idgenerator` because `tls-proto-parser` will deliver it as well

## 1.1.3 - 2020-09-22
### Added
- Add an interface for bean configuration

### Removed
- Remove `spring-boot-starter-data-mongodb` dependency
- Remove property `de.heuboe.tls.sequencer.script.path` to define path to script files
- Remove property `de.heuboe.tls.sequencer.script.name` to define list of scripts that should be loaded
- Remove property `de.heuboe.tls.sequencer.spec.path` to define path to specification file
- Remove property `de.heuboe.tls.sequencer.spec.name` to define the name of the specification file

## 1.1.2 - 2020-09-18
### Added
- Add `heuboe-idgenerator` in version 1.0.3 to update `iid` for a sequencer manipulated message before sending to Kafka

## 1.1.1 - 2020-09-18
### Changed
- Update several log levels from info to debug

## 1.1.0 - 2020-09-17
### Added
- Add rule to copy object from one topic to another
- Add new property `de.heuboe.tls.sequencer.script.path` to define path to script files
- Add new property `de.heuboe.tls.sequencer.script.name` to define list of scripts that should be loaded or all if
  property is empty
- Add new property `de.heuboe.tls.sequencer.spec.path` to define path to specification file
- Add new property `de.heuboe.tls.sequencer.spec.name` to define the name of the specification file (default spec.yaml)

### Changed
- Update script parsing logic to compare block definition with topic of received object instead of object itself
- Update log pattern
- Update to [akka-actor\_2.12 v2.6.9](https://akka.io/blog/news/2020/09/09/akka-2.6.9-released) (was 2.6.4)
- Update to [vmis2-kafka-protopojo-converter v1.6.1](http://pdb.heuboe.hbintern/pdb/#version;id=31000) (was 1.5.2)
- Update to [snakeyaml v1.27](https://bitbucket.org/asomov/snakeyaml/wiki/Changes) (was 1.26)
- Update to [tls-parent-pom v1.0.4](http://pdb.heuboe.hbintern/pdb/#version;id=30679) (was 1.0.3)
- Update to [tls-kafka-operator v1.4.2](http://pdb.heuboe.hbintern/pdb/#version;id=31584) (was 1.2.0)
- Update to tls-proto-interface v1.1.1 (was 1.1.0)
- Update to [tls-cfglib v1.0.6](http://pdb.heuboe.hbintern/pdb/#version;id=30677) (was 1.0.3)
- Update to [tls-grammar v1.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=30673) (was 1.0.4)
- Update to [protobuf-dynamic v1.0.1](https://github.com/os72/protobuf-dynamic/releases/tag/v1.0.1) (was 1.0.0)

## 1.0.0 - 2020-04-17
### Added
- Initial release
