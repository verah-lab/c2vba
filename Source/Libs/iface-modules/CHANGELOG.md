# Changelog iface parent pom

## 1.0.0

Modifications:
- Update/change parent pom: from jhbpom to tls-parent-pom [3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=48364)
- Set version to 1.0.0-SNAPSHOT (from 0.3.2-SNAPSHOT)
- got rid of jhb{Base}Logging, especially in IfaceProtTlsOverIp and IfaceLib
- Now @Slf4j (lombok) together with slf4j-api is used
- Modifications concerning migration from junit 4 to 5
- Added ci pipeline
- Update dependency: tls-tele 1.1.2 -> [2.0.0]()
- Update dependencies concerning spring and logging

## 0.3.1

- Fixed two places where access to routing by an unknown node leaded to null ptr access

## 0.3.0

- IfaceProtTlsOverIp
  - ConfigReader added definition for tlsoip parameters
  - added handling of tlsoip parameters from config-service
  - added field active in ConnectionConfig - indicates active or inactive connection
  - made timeSyncGenerator private
  - cleanup ConnectionConfigParamList
  - made ConnectionConfig (largely) a lombok Data class
  - cleanup TlsOverIp
  - pom: exchange jhbBase by jhbBase-logging, added lombok
- IfaceInterfaces
  - IfaceApplication: + Methods stopComm and startComm by osi7 number
  - IfaceException: Commented
  - IfaceSystemConnector: Commented class
  - RoutingEntry: commented
  - TelegramSentRcvdTracker: made sonar happy
- IfaceLib
  - IfaceApp
    - Changed logger (get rid of jbhlogging?)
    - made sonar happy
    - code cleanup
    - added implementation of {start|stop}Comm by osi7 numbers

## 0.2.2

- Updated dependencies
- root
  - Update to [tls-tele v1.1.2](http://pdb.heuboe.hbintern/pdb/#version;id=42656) (was 1.0.0 rn)
  - Update to [jhbpom 7.3.5](http://pdb.heuboe.hbintern/pdb/#version;id=42670) (7.3.3 to 7.3.5)

- ConnectionConfig: Added members tcpPortB (alternative port for second ip), id (to identify a connection)
- ConfigReader:
  - Added default for client
  - Added code for id
  - Enhanced code with warnings
  - spliced constructor, added methods setGraceTime, getGraceTime, checkAndSpreadDefaults. Handle receipt grace time
- Config: Code for receipt grace time
- Connection:
  - Handle exceptions due to bad tls telegrams
  - Handle receipt grace time
- ConnectionConfig
  - added Integer receiptGraceTime and code therefor
- IfaceApp
  - Handle exceptions concerning bad tls telegrams
- IfaceExeption
  - Added member potentialStreamProblem for bad tls telegrams

## v0.2.1

- Changed
  - Update to [spring-boot v2.5.9](https://github.com/spring-projects/spring-boot/releases/tag/v2.5.9) (was 2.5.8)

## v0.2.0

- Added
  - Add [log4j v2.17.1](https://logging.apache.org/log4j/2.x/changes-report.html#a2.17.1) (was 2.17.0 from `jhbBase-logging` dependency)

- Changed
  - Update to Java 11 (was Java 8)
  - Update to [jhbBase v3.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=39215) (was 3.0.7)
  - Update to [jddp v3.1.0](http://pdb.heuboe.hbintern/pdb/#version;id=39217) (was 3.0.1)
  - Update to jhbCsv v1.2.4 (was 1.2.0)
  - Update to [tls-tele v1.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=39310) (was 0.5.3)
  - Update to [spring-boot v2.5.8](https://github.com/spring-projects/spring-boot/releases/tag/v2.5.8) (was 2.4.7)
  - Update to [junit v4.13.2](https://github.com/junit-team/junit4/blob/HEAD/doc/ReleaseNotes4.13.2.md) (was 4.13)
  - Update to [cxf v3.5.0](https://github.com/apache/cxf/releases/tag/cxf-3.5.0) (was 3.1.0)
  - Update to [slf4j v1.7.35](https://github.com/qos-ch/slf4j/releases/tag/v_1.7.35) (was 1.7.10)

## 0.1.10

- ifaceProtTlsOverIp: Removed/Moved_to_debug(trace) some logs that where used in bug hunting.
- iface-prot-tls-over-ip: Update tls-tele (test) from 0.5.2 to 0.5.3
- Update perent pom jhbpom from 7.1.3 to 7.3.3

## 0.1.9

- ifaceLib: Removed creation of system messages in case of routing errors
- ifaceProtTlsOverIp: Added code before telegram gets sended to parent that indicates alive state of a connection (in case of being a server)
