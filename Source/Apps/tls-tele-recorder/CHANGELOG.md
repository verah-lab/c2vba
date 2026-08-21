# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 2.0.1 - 2024-10-14
### Changed
- Renovate -> Update to de.heuboe.tls:tls-kafka-operator v5.0.1 (was 5.0.0)

## 2.0.0 - 2024-09-19
### Changed
- Update to Java 21 (was Java 11)
- Update to spring-boot 3.3.x (was 2.7.x)
- Update to [tls-parent-pom v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48371) (was 2.1.0)
- Update to [tls-kafka-operator v5.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48433) (was 3.3.0)
- Update to [tls-tele v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48372) (was 1.1.3)
- Update to [tls-tel-io-interface v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48373) (was 2.0.2)
- Update to [receiver-reader v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48521) (was 1.2.5)

## 1.0.7 - 2024-04-22
### Added
- Add [vmis2-log4j2-extension v1.3.1](http://pdb.heuboe.hbintern/pdb/#version;id=39048) for logging in JSON files

## 1.0.6 - 2024-04-16
### Changed
- Update to [tls-parent-pom v2.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=43463) (was 2.0.2)
- Update to [tls-kafka-operator v3.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=43227) (was 3.1.0)
- Update to [tls-tele v1.1.3](http://pdb.heuboe.hbintern/pdb/#version;id=45764) (was 1.1.1)
- Update to [tls-tel-io-interface v2.0.2](http://pdb.heuboe.hbintern/pdb/#version;id=46631) (was 2.0.1)
- Update to [receiver-reader v1.2.5](http://pdb.heuboe.hbintern/pdb/#version;id=46869) (was 1.2.3)

## 1.0.5 - 2022-10-14
### Changed
- Update creation of `TimeGetter` object at service start instead of every time a timestamp will be manipulated

## 1.0.4 - 2022-10-14
### Added
- Add new property `manipulateTimestamps.activated` to activate the timestamp manipulation of telegrams for legacy systems
- Add new property `manipulateTimestamps.timezone` for defining a timezone in relation to the timestamp manipulation of telegrams for legacy systems

## 1.0.3 - 2022-09-30
### Added
- Add new property `updateRealAddress` to manipulate the real address of a legacy telegram before sending via TCP

### Changed
- Update to [tls-parent-pom v2.0.2](http://pdb.heuboe.hbintern/pdb/#version;id=41444) (was 2.0.1)
- Update to [tls-kafka-operator v3.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=40856) (was 3.0.0)
- Update to [tls-tele v1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=42046) (was 1.0.1)
- Update to [tls-tel-io-interface v2.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=41565) (was 2.0.0)

## 1.0.2 - 2022-04-04
### Changed
- Update log level from `ERROR` / `WARN` to `DEBUG` for parsing telegrams that seems to be `CommState` telegrams
- Update to [tls-tele v1.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=40495) (was 1.0.0)

## 1.0.1 - 2022-02-02
### Changed
- Update to [tls-parent-pom v2.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=39657) (was 2.0.0)

## 1.0.0 - 2022-01-26
### Added
- Add new mechanism to control log file change based on Quartz timer
- Add property `interval.cron` to enable cron based log file change
- Add property `compressLogs` to enable a GZIP compression of log files
- Add property `maxSaveRetries` to control telegram saving loss due to log file switch

### Changed
- Update to Java 11 (was Java 8)
- Update property `autoinval` to `interval.static`
- Update to [tls-parent-pom v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39224) (was 1.1.0)
- Update to [tls-kafka-operator v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39296) (was 2.1.3)
- Update to [tls-tele v1.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39310) (was 0.5.3)
- Update to [tls-tel-io-interface v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39309) (was 1.0.3)
- Update to [vavr v0.10.2](https://github.com/vavr-io/vavr/releases/tag/v0.10.4) (was 0.10.4)

## 0.0.24 - 2021-12-17
### Changed
- Update to [tls-kafka-operator v2.1.3](http://pdb.heuboe.hbintern/pdb/#version;id=38793) (was 1.9.2)

## 0.0.23 - 2021-11-26
### Changed
- Revert to [tls-kafka-operator v1.9.2](http://pdb.heuboe.hbintern/pdb/#version;id=36345) (was 2.0.0)

## 0.0.22 - 2021-11-15
### Added
- Add second TCP server port configuration `legacyPort` for sending legacy telegrams
- Add new log cleaning strategy based on used disk space by the log files

### Changed
- Switch to method for sending legacy telegrams from tls-tele
- Update to [tls-tele v0.5.3](http://pdb.heuboe.hbintern/pdb/#version;id=37998) (was 0.5.2)

## 0.0.21 - 2021-11-08
### Changed
- Sending legacy telegrams over TCP interface (temporary fix)
- Update client maps to be concurrent

## 0.0.20 - 2021-11-02
### Changed
- Update to [tls-parent-pom v1.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=37868) (was 1.0.11)
- Update to [tls-kafka-operator v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=37894) (was 1.9.2)
- Update to [tls-tele v0.5.2](http://pdb.heuboe.hbintern/pdb/#version;id=37912) (was 0.5.1)
- Update to [tls-tel-io-interface v1.0.3](http://pdb.heuboe.hbintern/pdb/#version;id=37906) (was 1.0.2)

## 0.0.19 - 2021-09-23
### Added
- Add TCP server for telegram streaming

### Changed
- Update directory scan of log file to ignore files in subdirectories

## 0.0.18 - 2021-09-14
### Changed
- Update message handling response to KOS for CommStat telegrams from false to true

## 0.0.17 - 2021-08-20
### Changed
- Update to [tls-kafka-operator v1.9.2](http://pdb.heuboe.hbintern/pdb/#version;id=36345) (was 1.9.0)
- Update to [tls-tele v0.5.1](http://pdb.heuboe.hbintern/pdb/#version;id=36494) (was 0.4.2)

## 0.0.16 - 2021-06-14
### Changed
- Update to [tls-kafka-operator v1.9.0](http://pdb.heuboe.hbintern/pdb/#version;id=35445) (was 1.7.3)

## 0.0.15 - 2021-06-04
### Added
- Add spring actuator endpoint `loggers`

### Changed
- Update to [tls-kafka-operator v1.7.3](http://pdb.heuboe.hbintern/pdb/#version;id=35336) (was 1.6.3)

## 0.0.14 - 2021-03-23
### Added
- Add properties `responseRetries` and `responseTimeout` to configure timeout for KafkaOperatorService
- Add handling of telegrams that are too short

### Changed
- Reduce log level
- Correct month in log file
- Update to [tls-kafka-operator v1.6.3](http://pdb.heuboe.hbintern/pdb/#version;id=34375) (was 1.6.2)

## 0.0.13 - 2021-03-08
### Changed
- Fix saving telegrams from SEND direction

## 0.0.12 - 2021-02-25
### Added
- Add functionality that checks the telegram directory for existence and create it if necessary
- Add new property `autoinval` to control the interval new files should be created

### Changed
- Update log level for file writing errors
- Rename property `absolutTelegramPath` to `absolutLogPath`
- Rename property `cleanTelegrams` to `cleanLogs`
- Update to [tls-kafka-operator v1.6.2](http://pdb.heuboe.hbintern/pdb/#version;id=33948) (was 1.6.1)

## 0.0.11 - 2021-02-03
### Changed
- Update to [tls-kafka-operator v1.6.1](http://pdb.heuboe.hbintern/pdb/#version;id=33816) (was 1.5.6)

## 0.0.10 - 2021-01-25
### Changed
- Use `tls-tel-io-interface` instead of `vmis2-tls-tel-io-interface` dependency
- Update to [tls-parent-pom v1.0.11](http://pdb.heuboe.hbintern/pdb/#version;id=33040) (was 1.0.5)
- Update to [tls-kafka-operator v1.5.6](http://pdb.heuboe.hbintern/pdb/#version;id=33628) (was 1.4.3)

## 0.0.9 - 2020-10-13
### Added
- Add support for telegrams with send direction
- Add new property `tls.tele.recorder.sendTopic` to `application.properties`

### Changed
- Renamed property `tls.tele.recorder.receiverTopic` to `tls.tele.recorder.receiveTopic`

## 0.0.8 - 2020-10-13
### Changed
- Use `vmis2-tls-tel-io-interface` instead of `tls-tel-io-interface` dependency

### Removed
- Remove configurable telegram separator via `application.properties`

## 0.0.7 - 2020-10-06
### Changed
- Update to [tls-parent-pom v1.0.5](http://pdb.heuboe.hbintern/pdb/#version;id=31909) (was 1.0.4)
- Update to [tls-kafka-operator v1.4.3](http://pdb.heuboe.hbintern/pdb/#version;id=31936) (was 1.4.2)

## 0.0.6 - 2020-09-30
### Changed
- Fix path of docker image

## 0.0.5 - 2020-09-30
### Added
- Add docker image build configuration

## 0.0.4 - 2020-09-30
### Added
- Add new property `receiverTopic` to configure telegram topic

### Changed
- Update project to be a standalone project

## 0.0.3 - 2020-09-28
### Changed
- Update `TeleRecorderUtils` to an abstract class, was interface

### Removed
- Remove property `tls.tele.recorder.fileFormat`. All files will be saved in a binary format.

## 0.0.2 - 2020-09-25
### Added
- Add milliseconds to telegram file names
- Add configurable endpoints via `application.properties`
- Add configurable telegram separator via `application.properties`

### Changed
- Update handling of `absolutTelegramPath` property path to be independent of tailing slash

## 0.0.1 - 2020-09-24
### Added
- Initial release
