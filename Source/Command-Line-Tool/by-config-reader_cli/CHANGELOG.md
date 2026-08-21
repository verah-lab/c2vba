# Changelog of CLI Deployment

All notable changes to this project will be documented in this file.
HeuBoe changelog guideline (based on renovate tool) can be found here:
[Changelog Aktualisierung](https://druide.heuboe.de/node/6113#toc:445-Changelog-Aktualisierung).
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Further information about by-config-cleaner, by-config-reader and the whole configuration processing, see:
[C2VBA Konfiguration](https://druide.heuboe.de/node/7188)

## [0.1.2.0] - 2025-01-06

### Changed
- updated by-config-reader to version 0.1.2:
  - added more wzg-type mappings


## [0.1.1.1] - 2025-12-16

### Changed
- changed path to new config: 20251215_164621


## [0.1.1.0] - 2025-12-03

### Changed
- updated by-config-reader to version 0.1.1:
  - added functionality to determine barriers ('HS' / 'SCH') as wzg type 'VHT'
- fixed wzg-type indicators in name (column 'Bezeichner') for Tunnel Kohlberg file
- changed path to new config: 20251203_111148


## [0.1.0.2] - 2025-10-31

### Changed
- appended wzg-type indicators to name (column 'Bezeichner') for Tunnel Kohlberg file
- changed path to new config: 20251031_124415


## [0.1.0.1] - 2025-10-27

### Changed
- changed path to c2vba-config-data_C project (it was renamed)
- changed path to new config: 20251027_131413


## [0.1.0.0] - 2025-10-24

### Added
- added .sh Shell script to start by-config-reader
- added property "aufbereitetDir" to set path as property (not as call parameter)
- added CHANGELOG.md

### Changed
- updated jar of by-config-reader to version 0.1.0

### Removed
- removed .BAT batch file (replaced through .sh shell script)
- removed JRE 11 files

