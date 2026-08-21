# Changelog of by-config-reader

All notable changes to this project will be documented in this file.
HeuBoe changelog guideline (based on renovate tool) can be found here:
[Changelog Aktualisierung](https://druide.heuboe.de/node/6113#toc:445-Changelog-Aktualisierung).
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Further information about by-config-cleaner, by-config-reader and the whole configuration processing, see:
[C2VBA Konfiguration](https://druide.heuboe.de/node/7188)

## [0.1.2] - 2026-01-09

### Added
- added more wzg-type mappings

### Changed
- code optimization


## [0.1.1] - 2025-12-03

### Added
- added handling of wzg type for barriers ('Schranke', 'Halbschranke') as VHT

### Changed
- updated dependency of hb-config-reader to version 0.0.3 
  - contains: empty row bug fix


## [0.1.0] - 2025-10-21

### Added
- new property to set the directory of tsv files to be imported

### Changed
- improved logging and exception handling in order to enable a better debugging process
- improved testing


## [0.0.3] - unreleased

### Added
- added changelog file

### Changed
- improved logging and exception handling
- optimized documentation in application.properties template
- update dependencies:
  - de.heuboe.base:jhbpom ............................... 7.3.5 -> 9.0.0
  - de.heuboe.config:hb-config-base ..................... 1.0.3 -> 1.0.5
  - de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.17 -> 1.16.22
