# Changelog tls-receiver-appcore

## upcoming

## 2.2.0 - 2026-01-26

### Changed
- Move to [heuboe-base-parent 1.6.3](http://pdb.heuboe.hbintern/pdb/#version;id=51721) was tls-base-parent 3.4.0.
- de.heuboe.asfinag:vmis2-configservice-interface 1.19.0 -> [1.21.0]
- de.heuboe.tls:tls-generator-proto 5.0.0 -> [5.1.0]
- de.heuboe.tls:tls-proto-interface 5.0.0 -> [5.1.0]

### Added
- Added tests for Configuration of a map of TlsTypeIds to time offsets
- A comma separated list of time offsets can be configured in the property de.heuboe.asfinag.tls.receiver.timeoffset-map.
  - Each entry in the string must have the format "fg/id/typ!offset"
  - Example: "4/131/120!90, 4/133/120!-30"
  - Those offsets are applied to time items in DE-Blocks of the given TlsTypeId (fg/id/typ).

## 2.1.1

Reworked Receiver.applyTimeOffset to handle DataObjects consisting of multiple DataItems.
Before this change, the time offset was applied to all DataObjects seen as DataItems.
To see them as DataItems was the mistake.

Tests were modified/added accordingly.

## 2.1.0

### Added

NRW can only use UNIX timestamps.
From a ZDF-System they have an offset to UTC (one hour). This offset has to be corrected.

- Method to shift TimeItems in Receiver after transforming a telegram  
  So changes are kept to a minimum in the receiver
- Added bean config for time item shifting (timeoffset). Absence defaults to 0.
- Added tests for time item shifting

### Updated dependencies

- de.heuboe.asfinag:vmis2-configservice-interface 1.17.0 -> [1.19.0](http://pdb.heuboe.hbintern/pdb/#version;id=51460)
- de.heuboe.tls:tls-cgl 2.0.3 -> [3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48538)
- de.heuboe.tls:tls-generator-proto  4.3.0 -> [5.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=50305)
- de.heuboe.tls:tls-proto-interface  4.3.0 -> [5.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=50308)
- de.heuboe.tls:tls-rcvdataconverter 1.2.3 -> [2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48534)
- de.heuboe.tls:tls-tel-io-interface 3.0.0 -> [3.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=50210)

## 2.0.0

### Updated dependencies
- de.heuboe.tls:tls-parent-pom 2.2.0 -> [3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48371)
- de.heuboe.asfinag:vmis2-configservice-interface 1.16.27 -> [1.17.0](http://pdb.heuboe.hbintern/pdb/#version;id=48235)
- de.heuboe.asfinag:vmis2-kafka-protopojo-converter 1.9.0 -> [2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=44713)
- de.heuboe.asfinag:vmis2-tls-cfggetter 1.6.1 -> [2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48405)
- de.heuboe.idgenerator:heuboe-idgenerator 1.1.0 -> [2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=44551)
- de.heuboe.tls:receiver-transformer 1.3.0 -> [2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48522)
- de.heuboe.tls:tls-cfglib 2.3.0 -> [3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48425)
- de.heuboe.tls:tls-tel-io-interface 2.1.0 -> [3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48373)
- de.heuboe.asfinag:vmis2-tls-generator-proto 3.4.0 -> [4.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48511)
- de.heuboe.asfinag:vmis2-tls-proto-interface 3.4.0 -> [4.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48512)

## 1.2.4

Added ci pipeline

Updated dependencies
- de.heuboe.tls:tls-parent-pom 2.1.0 -> [2.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=47725)
- de.heuboe.asfinag:vmis2-tls-generator-proto 3.3.0 -> [3.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=47743)
- de.heuboe.asfinag:vmis2-tls-proto-interface 3.3.0 -> [3.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=47744)
- de.heuboe.tls:receiver-transformer  1.2.5 -> [1.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=48025)
- de.heuboe.tls:tls-cfglib 2.2.0 -> [2.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=47731)
- de.heuboe.tls:tls-tel-io-interface 2.0.2 -> [2.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=47726)
- de.heuboe.asfinag:vmis2-tls-cfggetter 1.6.0 -> [1.6.1](http://pdb.heuboe.hbintern/pdb/#version;id=48154)

## 1.2.3

Updated dependencies
- de.heuboe.asfinag:vmis2-configservice-interface 1.16.25 -> [1.16.27](http://pdb.heuboe.hbintern/pdb/#version;id=46934)
- de.heuboe.asfinag:vmis2-tls-cfggetter 1.5.0 -> [1.6.0](http://pdb.heuboe.hbintern/pdb/#version;id=46935)
- de.heuboe.tls:tls-cfglib 2.1.0 -> [2.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=46945)

## 1.2.2

Moved Autowired member to argument of method
Added conditional bean to be used by receiver/transformer to allow the insertion of Typ48 data into 
data objects. I.e. fake timestamps on receiver side of TLS.

Updated dependencies:
- de.heuboe.asfinag:vmis2-configservice-interface  1.16.24 -> [1.16.25](http://pdb.heuboe.hbintern/pdb/#version;id=46818)
- de.heuboe.tls:tls-cgl 2.0.2 -> [2.0.3](http://pdb.heuboe.hbintern/pdb/#version;id=46694)

Updated dependencies
- de.heuboe.tls:tls-tel-io-interface .................... 2.0.1 -> [2.0.2](http://pdb.heuboe.hbintern/pdb/#version;id=46631)

## 1.2.1

Updated dependencies
- de.heuboe.tls:tls-parent-pom 2.0.1 -> [2.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=43463)
- de.heuboe.asfinag:vmis2-tls-generator-proto 3.1.2 -> [3.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=46571)
- de.heuboe.asfinag:vmis2-tls-proto-interface 3.1.2 -> [3.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=46572)
- de.heuboe.tls:receiver-transformer 1.2.3 -> [1.2.4](http://pdb.heuboe.hbintern/pdb/#version;id=46609)
- de.heuboe.tls:tls-rcvdataconverter 1.2.1 -> [1.2.3](http://pdb.heuboe.hbintern/pdb/#version;id=46619)

[INFO]   de.heuboe.asfinag:vmis2-kafka-protopojo-converter ..... 1.9.0, last Java 11



## 1.2.0

Modified test to work with changes made in between in some libs
- Updated dependencies
  - >>> de.heuboe.tls:tls-cfglib 1.1.3 -> [2.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=46510)
  - de.heuboe.asfinag:vmis2-configservice-interface  1.16.20 -> [1.16.24](http://pdb.heuboe.hbintern/pdb/#version;id=45931)
  - de.heuboe.asfinag:vmis2-tls-cfggetter 1.1.1 -> [1.5.0](http://pdb.heuboe.hbintern/pdb/#version;id=45691)
  - de.heuboe.asfinag:vmis2-tls-generator-proto 3.1.1 -> [3.1.2](http://pdb.heuboe.hbintern/pdb/#version;id=43337)
  - de.heuboe.asfinag:vmis2-tls-proto-interface 3.1.1 -> [3.1.2](http://pdb.heuboe.hbintern/pdb/#version;id=43338)
  - omitted due to version conflict: de.heuboe.asfinag:vmis2-tls-generator-proto [3.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=43850)
  - omitted due to version conflict: de.heuboe.asfinag:vmis2-tls-proto-interface [3.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=43851)
  - de.heuboe.tls:tls-cgl (test only) 2.0.1 -> [2.0.2]()

## 1.1.2

- Added new spring config file for OSI7 configuration including handling of configuration change
- Modified tests accordingly
- Changed dependencies
  - de.heuboe.asfinag:vmis2-configservice-interface 1.16.17 -> [1.16.20](http://pdb.heuboe.hbintern/pdb/#version;id=42535)
  - de.heuboe.asfinag:vmis2-tls-generator-proto  3.0.0 -> [3.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=42475)
  - de.heuboe.asfinag:vmis2-tls-proto-interface 3.0.0 -> [3.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=42476)
  - de.heuboe.tls:tls-cgl 2.0.0 -> [2.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=42562)
  - de.heuboe.tls:tls-tel-io-interface 2.0.0 -> [2.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=41565)
  - de.heuboe.tls:tls-cfglib -> 1.1.2 -> [1.1.3](http://pdb.heuboe.hbintern/pdb/#version;id=42941)

## 1.1.1

- de.heuboe.asfinag:vmis2-configservice-interface 1.16.15 -> 1.16.17
- de.heuboe.asfinag:vmis2-tls-cfggetter 1.1.0 -> 1.1.1
- de.heuboe.asfinag:vmis2-tls-proto-interface 2.4.5 -> 3.0.0
- de.heuboe.asfinag:vmis2-tls-scripts 2.4.5 -> 3.0.0
- de.heuboe.tls:receiver-transformer 1.2.1 -> 1.2.3
- de.heuboe.tls:tls-cfglib 1.1.1 -> 1.1.2
- de.heuboe.tls:tls-cgl 1.1.6 -> 2.0.0

## 1.1.0

- de.heuboe.tls:tls-parent-pom 1.1.0 -> 2.0.1
- de.heuboe.asfinag:vmis2-configservice-interface 1.16.13 -> 1.16.15
- de.heuboe.asfinag:vmis2-log4j2-extension 1.2.0 -> 1.3.1
- de.heuboe.asfinag:vmis2-tls-cfggetter 1.0.9 -> 1.1.0
- de.heuboe.asfinag:vmis2-tls-proto-interface 2.4.4 -> 2.4.5
- de.heuboe.asfinag:vmis2-tls-scripts 2.4.4 -> 2.4.5
- de.heuboe.tls:receiver-transformer 1.1.25 -> 1.2.1
- de.heuboe.tls:tls-cfglib 1.0.14 -> 1.1.1
- de.heuboe.tls:tls-rcvdataconverter 1.0.1 -> 1.2.1
- de.heuboe.tls:tls-tel-io-interface 1.0.3 -> 2.0.0
- de.heuboe.idgenerator:heuboe-idgenerator 1.0.4 -> 1.1.0

## 1.0.23

- Surrounded inner receive of telegrams in run loop with try catch(Exception) in order to avoid ending the run loop (Receiver.java)
- The loop itself is surronded by try/catch(Throwable)

## 1.0.22

- Improved Tests. Esp Fg/Id/Typ = 4/1/3
- Update receiver-transformer 1.1.24 to 1.1.25

## 1.0.21

- Remove pinning of protobuf version
- de.heuboe.tls:receiver-transformer .................. 1.1.23 -> 1.1.24 -> reduced msgs and logs concerning timestamps

## 1.0.20

- ('Umlaute')
- Added and improved tests
- New parent pom: de.heuboe.tls:tls-parent-pom 1.0.11 -> 1.1.0
- Changed dependencies:
- de.heuboe.asfinag:vmis2-tls-cfggetter ................. 1.0.8 -> 1.0.9
- de.heuboe.tls:receiver-transformer .................. 1.1.22 -> 1.1.23
- de.heuboe.tls:tls-cfglib ............................ 1.0.13 -> 1.0.14
- de.heuboe.tls:tls-tel-io-interface .................... 1.0.2 -> 1.0.3
- Minor changes due to changed dependencies

## 1.0.19

- Pinning version auf protobuf to 3.19.0 (3.13 (of parent pom) is buggy)
- Added and improved tests. Esp. fg 4 t 3 and fg 4 t 5 this time.
- Updated dependencies
  - de.heuboe.asfinag:vmis2-configservice-interface 1.16.12 -> 1.16.13
  - de.heuboe.asfinag:vmis2-tls-proto-interface 2.4.3 -> 2.4.4
  - de.heuboe.asfinag:vmis2-tls-scripts 2.4.3 -> 2.4.4
  - de.heuboe.tls:receiver-transformer 1.1.21 -> 1.1.22
  - de.heuboe.tls:tls-cgl 1.1.5 -> 1.1.6

## 1.0.18

- Extended and improved tests.
- Use new libs with corrected receive of FLOAT (NaN) and WZGGrundeinstellung
- TransformerConfig/Config changed in order to use error counter of TransformerImpl
- DataWriterImpl removed comment
- Changed dep vmis2-kafka-protopojo-converter 1.8.0 -> 1.9.0

## 1.0.17

- Dependency receiver-tranformer 1.1.19 -> 1.1.20              (subsequent for GPRS-UZ)

## 1.0.16

- Dependency receiver-tranformer 1.1.18 -> 1.1.19              (subsequent for GPRS-UZ)

## 1.0.15

- Dependency receiver-tranformer 1.1.17 -> 1.1.18              (!-> tls-tele:0.4.2)
- Dependency cfglib 1.0.11 -> 1.0.13
- Dependency vmis2-tls-scripts (test) 2.4.1 -> 2.4.2
- Dependency vmis2-tls-resources (test) 2.4.1 -> 2.4.2
- Dependency vmis2-log4j-extension 1.1.0 -> 1.2.0
- Dependency vmis2-kafka-protopojo-converter 1.6.1 -> 1.8.0
- Dependency vmis2-configservice-interface 1.16.10 -> 1.16.12

## 1.0.14

- Inherit changes for float-invalid value

## 1.0.13

- Added timetolerance (tolerance for future TLS timestamps) to properties and config code to set it approriately.
- the implementation was already there
- Added test for timetolerance
- Updated dep vmis2-configservice-interface from 1.16.7 to 1.16.10

## 1.0.12

- Improved handling of non existing objects. No longer handled as exception. (Datawriter)
- Provided code to turn off messages concerning not existing objects. (Datawriter)
- Added test for appended timestamp (GPRS-UZ)

## 1.0.11

Removed 'overflowing' docker image. ;-)

## 1.0.10

Tag 1.0.9 was spoiled

## 1.0.9

Extracted common parts for e21x and vmis of former tls-receiver-app.
tls-receiver-app became the final executable configured for vmis2.

- Added implementation for MessageManagament

## 1.0.8

Updated dependencies:

- tls-parent-pom 1.0.10 -> 1.0.11
- vmis2-tls-resources.version 2.3.3 -> 2.3.5
- receiver-transformer 1.1.12 -> 1.1.13
- tls-cfglib 1.0.6 -> 1.0.7
- tls-cfgsvc-bridge 0.9.1 -> 0.10.1
- vmis2-configservice-interface 1.16.2 -> 1.16.4
- tls-cgl 1.1.0 -> 1.1.1

## 1.0.7

- vmis2-tls-resources 2.3.2 -> 2.3.3
- tls-cgl (using vmis2-tls-resources 2.3.3) in version 1.1.0
- Supports 'jobnummer' (through submodule)
- Invalid value to float now uses Float.MIN_VALUE (through submodule)
- Updated dependencies especially receiver-transformer to 1.1.11 (Fix of lve timestamp errors)

## 1.0.6

- Updated dependencies especially receiver-transformer to 1.1.11 (Fix of lve timestamp errors)

## 1.0.5

- Updated dependency receiver-transformer to 1.1.10 (Fix of time zone use)

## 1.0.4

- Updated dependencies especially vmis2-tls-resources.version to 2.3.0

## 1.0.3

- Corrected the name of the main class for the docker image

## 1.0.2

- Corrected peculiar error in image names of jib-plugin

## 1.0.1

- changed version of vmis2-log4j2-extension to 1.1.0
- introduced properties for docker images in pom
- update parent pom tls-parent-pom to 1.0.7
- added version for jib-plugin

## 1.0.0

- Core HeuBoe TLS receive Process (wired for vmis2) with V2.2.0 of vmis2-tls-resources

## 0.0.15

- Removed internal non configurable prefix. Pre- and postfix are only configurable from now on.

## 0.0.12

- Dependency to tls-resources 2.1.0 updated => cgl updated
- Some general dependencies updated

## 0.0.11

Added property de.heuboe.asfinag.tls.receiver.timezoneid  
This string value is passed to the TimeGetter (static) and used for intrpretation of TLS timestamps

## 0.0.10

- changed to tests via embedded Kafka
- added a lot of test files
- added metrics
- changed config in order to allow mock of config service/virtual sensors
- added virtual sensors
- updated dependencies
- some test had to be disabled for now

## 0.0.9

New version due to release problems

## 0.0.8

New version due to release problems

## 0.0.7

Added prefix and postfix for topic names  
Changed init from plugin mechanics to init call of newly created class in vmis2-tls-cgl  
uses new generated method convert2 which delivers single tls data objects for use with log compation in kafka  
each converted object (DE-Block) ist directly sent to kafka  
updated dependencies  
updated KafkaConfig.java (2 places)  

## v0.0.6 - 11.11.19

- updated dependencies
- src/test/resources/application.properties
  - added spring.kafka.listener.missing-topics-fatal=false
  - removed duplicate property
