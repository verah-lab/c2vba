# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 3.0.0 - 2025-12-11
### Added
- [TK:3959](https://ext.heuboe.de/SpiraTeam/226/Task/3959.aspx): Add new FG6 datatype `VLTBatterieLadezustand`
- [TK:3959](https://ext.heuboe.de/SpiraTeam/226/Task/3959.aspx): Add new FG6 datatype `VLTBatterieDefekt`
- [TK:3959](https://ext.heuboe.de/SpiraTeam/226/Task/3959.aspx): Add new FG6 datatype `VLTSolarzellenDefekt`

### Changed
- Update to heuboe-base-parent v1.6.0 (was 1.0.1)
- Update to tls-resources 5.1.0 (was 4.0.0)
- Renovate -> Update to de.heuboe.asfinag:vmis2-jprotoc-plugin 4.7.0 (was 4.6.2)
- Renovate -> Update to de.heuboe.asfinag:vmis2-jprotoc-transferinterface 4.7.0 (was 4.6.2)

## 2.0.0 - 2024-09-20
### Changed
- Update to Java 21 (was Java 11)
- Update to spring-boot 3.3.x (was 2.7.x)
- Update to [heuboe-base-parent 1.0.1](http://pdb.heuboe.hbintern/pdb/#version;id=48853) (was vmis2-base-parent 2.26.0)
- Update to [tls-resources 4.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48406) (was 3.5.0)

### Removed
- Remove dependency `classindex` because it seems not to be

## 1.0.4 - 2024-06-06
### Changed
- Update to [tls-resources v3.5.0](http://pdb.heuboe.hbintern/pdb/#version;id=47732) (was 3.4.3)

## 1.0.3 - 2024-05-06
### Changed
- Fix fg4 proto generation to use correct `now-rcv-fg4.txt` instead of `rcv-fg4.txt`

## 1.0.2 - 2024-05-06
### Changed
- Update override of `send-fg-all.txt` script to ensure that resource stream loader always reads the correct file
- Update override of `rcv-fg4.txt` script to ensure that resource stream loader always reads the correct file
- Rename `send-fg-all.txt` script to `now-send-fg-all.txt`
- Rename `rcv-fg4.txt` script to `now-rcv-fg4.txt`

## 1.0.1 - 2024-03-27
### Changed
- Fix target topics, macro definitions and reactions for fg4 type 32, 33, 48 and 55

## 1.0.0 - 2024-03-25
### Added
- Initial commit
