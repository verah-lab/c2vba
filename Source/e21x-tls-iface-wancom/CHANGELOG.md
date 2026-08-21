# Changelog

## 1.1.0

- Added property specialDevRoot (de.heuboe.tls.ifacewancom.config.specialDevRoot)
- Added test for specialDevRoot (SpecialDevRootTest)
- Split configuration to smaller pieces due to testability
- integrated change handling as far it is working upto now (no actions)
- Updated dependecies
  - de.heuboe.tls:tls-ifacetlsoip-kafka 1.1.3 -> [1.1.4](http://pdb.heuboe.hbintern/pdb/#version;id=44458) /Parent/
  - de.heuboe.asfinag:vmis2-configservice-interface 1.16.21 -> [1.16.22](http://pdb.heuboe.hbintern/pdb/#version;id=44467)
  - de.heuboe.asfinag:vmis2-tls-cfggetter 1.1.1 -> [1.4.0](http://pdb.heuboe.hbintern/pdb/#version;id=44591)
  - de.heuboe.tls:tls-cfgsvc-bridge 1.0.0 -> [1.0.2](http://pdb.heuboe.hbintern/pdb/#version;id=44569)
  - de.heuboe.tls:tls-ifaceroutingcfg 1.1.3 -> [1.1.4](http://pdb.heuboe.hbintern/pdb/#version;id=44459)
  - de.heuboe.tls:tls-ifacesysconkafka  1.1.3 -> [1.1.4](http://pdb.heuboe.hbintern/pdb/#version;id=44460)
- Made sonar happy


## 1.0.0

- improved logging
- Improved exception handling in order to detect bad telegram structure. Most of the work was already done in libs used.
- removed bug, where the langth of a received telegram was checked wrong
- changes due to updated de.heuboe.tls:tls-ifacetlsoip-kafka
- removed unused class Util in tests
- handled comments in MR in tests
- Updated dependencies
  - de.heuboe.asfinag:vmis2-configservice-interface 1.16.15 -> [1.16.21](http://pdb.heuboe.hbintern/pdb/#version;id=43379)
  - de.heuboe.asfinag:vmis2-tls-cfggetter 1.0.10 -> [1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=40658)
  - de.heuboe.tls:tls-ifacetlsoip-kafka 1.1.1 -> [1.1.3](http://pdb.heuboe.hbintern/pdb/#version;id=42869)

## v0.1.0

Changed

- Update to Java 11 (was Java 8)
- Update to [tls-ifacetlsoip-kafka v1.1.1](http://pdb.heuboe.hbintern/pdb/#version;id=39669) (was 1.0.7)
- Update to [vmis2-configservice-interface v1.16.15](http://pdb.heuboe.hbintern/pdb/#version;id=38943) (was 1.16.3)
- Update to [vmis2-log4j2-extension v1.3.1](http://pdb.heuboe.hbintern/pdb/#version;id=39048) (was 1.1.0)
- Update to [vmis2-tls-cfggetter v1.0.10](http://pdb.heuboe.hbintern/pdb/#version;id=39318) (was 1.0.4)
- Update to [jib-maven-plugin v3.1.4](https://github.com/GoogleContainerTools/jib/blob/master/jib-maven-plugin/CHANGELOG.md#314) (was 2.5.0)

## 0.0.6

- Fixed main class in docker config in pom.xml

## 0.0.5

- First release
