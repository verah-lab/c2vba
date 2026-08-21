# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 2.0.0 - 2024-10-16
### Changed
- Update to Java 21 (was Java 11)
- Update to spring-boot 3.3.x (was 2.7.x)
- Update to [heuboe-base-parent v1.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=48972) (was tls-parent-pom 2.2.0)
- Update to [tls-cfglib v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48425) (was 2.3.0)
- Update to [tls-kafka-operator v5.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=49143) (was 4.0.0)
- Update to [now-tls-proto-interface v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48900) (was 1.0.4)
- Update to [vmis2-kafka-protopojo-converter v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=44713) (was 1.9.0)
- Update to [vmis2-configservice-interface v1.17.1](http://pdb.heuboe.hbintern/pdb/#version;id=48875) (was 1.16.27)
- Update to [vmis2-tls-cfggetter v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48432) (was 1.6.1)
- Update to [heuboe-kafka-test-utils v2.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=44152) (was 1.3.0)

### Removed
- Remove dependency `tls-cfgsvc-bridge` because it will be delivered by other dependencies

## 1.0.3 - 2024-10-15
### Changed
- Update internal handling of legacy devices to respect config changes in the correct way

## 1.0.2 - 2024-06-06
### Changed
- Update to [tls-parent-pom v2.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=47725) (was 2.1.0)
- Update to [tls-cfglib v2.3.0](http://pdb.heuboe.hbintern/pdb/#version;id=47731) (was 2.2.0)
- Update to [tls-cfgsvc-bridge v1.2.0](http://pdb.heuboe.hbintern/pdb/#version;id=47727) (was 1.1.0)
- Update to [tls-kafka-operator v4.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=47800) (was 3.3.0)
- Update to [now-tls-proto-interface v1.0.4](http://pdb.heuboe.hbintern/pdb/#version;id=47854) (was 1.0.0)
- Update to [vmis2-configservice-interface v1.16.27](http://pdb.heuboe.hbintern/pdb/#version;id=46934) (was 1.16.26)

## 1.0.1 - 2024-04-17
### Added
- Add present check for device in config service for messages that should be converted in send direction

### Changed
- Update handling of messages with more than one containing element object to send them as transactional object instead
  of splitting them
- Ignore devices that are not contained in the config service

## 1.0.0 - 2024-04-12
### Added
- Initial commit
