# Changelog of C2VBA data project

All notable changes to this project will be documented in this file.
HeuBoe changelog guideline (based on renovate tool) can be found here:
[Changelog Aktualisierung](https://druide.heuboe.de/node/6113#toc:445-Changelog-Aktualisierung).
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Further information about by-config-cleaner, by-config-reader and the whole configuration processing, see:
[C2VBA Konfiguration](https://druide.heuboe.de/node/7188)

## [0.1.4] - 2025-12-16

### Changed
- changed names of objects in tunnel Kohlberg file (A. Haug, D. Hermanns)


## [0.1.3] - 2025-12-03

### Added
- new configuration cleaner process ('aufbereitet'): 20251203_111148

### Changed
- changed excel file of Tunnel Kohlberg: removed "_C" name parts from barriers ("Schranke") and VLSAs


## [0.1.2] - 2025-10-31

### Changed
- appended wzg-type indicators to name (column 'Bezeichner') for Tunnel Kohlberg file


## [0.1.1] - 2025-10-27

### Added
- new configuration cleaner process ('aufbereitet'): 20251027_131413

### Changed
- changed excel file LVE_Zusammenfassung_C2VBA-GE_ABDS_Projektierung_V36_Ergaenzung-SSP_V6:
  (see changelog in excel sheet)


## [0.1.0] - 2025-10-21

### Added
- added new excel files:
  - Konfiguration_Tunnel_Kohlberg(Version 1 03) an VRZ_edit_SSP_V2.xlsx
  - LVE_Zusammenfassung_C2VBA-GE_ABDS_Projektierung_V36_Ergaenzung-SSP_V6.xlsx
  - Konfig_A92_edit_SSP_V5.xls
  - cp_tutting_DeKonfig_edit_SSP_V2.xls
- added new subfolder structure in folder 'aufbereitet' in order to collect data by import date
- added HB_Excel_Änderungen.txt in order to document, which excel changes were done by HB to be able to successfully run the import

### Changed
- renamed project and its path from 'C2VBA' to 'c2vba-config-data_C'
- moved excel file C2VBA-GE_UZ06_cp_a92_DeKonfig.xls to new folder 'archiv', because its contents where replaced by new file Konfig_A92_edit_SSP_V5.xls
- optimized changelog and updated to newest HB guideline
- further information about changes of excel files, see HB_Excel_Änderungen.txt


## [0.0.7] - 2024-02-26

### Changed
- updated config LVE Zusammenfassung


## [0.0.6] - 2024-02-23

### Removed
- removed duplicate KonfTab


## [0.0.5] - 2024-02-22

#### Changed
- fix loc/dist


## [0.0.4] - 2024-02-05

### Changed
- new import


## [0.0.3] - 2023-12-20

### Changed
- updated config


## [0.0.2] - 2023-10-31

### Changed
- replace Konfig Tunnel Kohlberg (with missing AQs)
- replace fixed Konfig A93
- fix Fg4 MQs
- add LVE Excel (with missing EQs)
- add SWIS Konfig
- add WZG Types


## [0.0.1] - 2023-10-21