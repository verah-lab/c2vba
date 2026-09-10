# Changelog

## v0.0.15
- [IN:28720](https://ext.heuboe.de/SpiraTeam/226/Incident/28720.aspx) Exception nach Konfig-Update

## v0.0.14
- [IN:28471](https://ext.heuboe.de/SpiraTeam/226/Incident/28471.aspx) Darstellung Maßnahme LKWUV, nassvPkw, 120disturbed
  die Maßnahme-ID '120disturbed' beginnt als einzige mit einer Ziffer - so kann sie nicht ins xml als ID eingetragen werden.
  865842362te Ausnahme: daraus muss (mal wieder) im xml d120disturbed eingetragen werden - der site-config-service muss das 'd' dann wieder amputieren.

## v0.0.13
- loading config data from cfgService is repeated until the service is available
- bugfix: topics *DataChange trigger revalidation with current cfgService

## v0.0.12
- validation of cluster devices corrected

## v0.0.11
- kafka topics *DataChange trigger revalidation with current cfgService

## v0.0.10
- uses site-base_1.25 - dependency jaxen_1.1.6 added

## v0.0.9
- method getAqSvgs() added

## v0.0.8
- C2VBA no validation of tlsId (never set)

## v0.0.7
- create RoadSegStyles and Symbol-Svgs

## v0.0.6
- png folder restructured, call responseObserver.onError without .onCompleted
- bugfix fill strayQs and their devices with mock values (vmis2 cfgSvc does not provide all props)
- uses site-config-iface-0.0.6 -> site-base-1.23 (classes added to define situation visualization per site)
- removed application parameter, actuator added, log4j2.xml improved

## v0.0.5
- just to save

## v0.0.4
- get(Bab/Loc)Segs by viewId as well

## v0.0.3
- depends on nrw-service-models_1.1.9, image-service-lib_1.1.9, jResUtils_2.2.20

## v0.0.2
- just to save

## v0.0.1
- Initial release
