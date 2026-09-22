# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## 2.0.1
-Bugfix 33576: cannot cast of point reference to line reference https://vmis-ehe.visualstudio.com/vmis2/_boards/board/t/Kernsystem/Stories/?workitem=33576
remove casting, add getMapVersion to Interface GeoReference, use getMapVersion without casting.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.24 -> 1.16.27
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.4.6 -> 3.5.1
- de.heuboe.asfinag:vmis2-base-parent ................ 4.1.0 -> 5.2.0



## 2.0.0
- increase version because of java update
- https://dev.azure.com/asfinag/VM.VMIS-ZT.VMIS2/_workitems/edit/86949
- add junction as new infrastructure object 
- add infrastructure object comparator and test
- de.heuboe.asfinag:vmis2-base-parent ............... 2.19.0 -> 4.1.0
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.19 -> 1.16.24
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.1.6 -> 3.4.6



## 1.6.0
- InfrastructureObjectBase: add property 'usePrettyPrinter' for attached data using PrettyPrinter or toString()
- InfrastructureObjectBase: generate references of infrastructureObjects only if iterationDepth < maxIterationDepthWithReferences to avoid endless loops

## 1.5.1
- change hashmap iteration in abstract infrastructure object publisher, to not run in concurrent modification exception.

## 1.5.0
- add LaneDetails

## 1.4.9
- add reference type for tunnel, cfg srv and geo mgr object.
- de.heuboe.asfinag:vmis2-control-datamodel ............. 3.1.3 -> 3.1.4

## 1.4.8
- add reference type for SBA, WTA and Other AQs.

## 1.4.7
- add protobuf serializer to serializer infrastructure Object to PInfrastructureObject.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.17 -> 1.16.18
- de.heuboe.asfinag:vmis2-control-datamodel .... 2.1.2 -> 3.1.3
- de.heuboe.asfinag:vmis2-base-parent .............. 2.17.0 -> 2.18.0

## 1.4.6
- bugfix removeForbiddenAreas: areasOfDontUse could contain a point location now without leading to an StackOverflowError (will be interpreted as 1m validity section)

## 1.4.5
- bugfix filterAllowedSections: allowedSections could contain a point location now without leading to an StackOverflowError

## 1.4.4
- updated all dependencies and made code fine with them

## 1.4.3
- add TunnelDirection as new infrastruture
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.7 -> 2.12.0
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.6 -> 1.16.10
- de.heuboe.asfinag:vmis2-control-datamodel ............. 2.0.6 -> 2.1.2

## 1.4.2
- added new function to reset a complete new attached data to an infra object

## 1.4.1
- LineReference: set roadId in constructors
- InfrastructureObjectBase: bugfix when adding a reference but list was null

## 1.4.0
- add new method updateReferences to InfrastructureObject
- add new method to get complete attachedData
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.3 -> 1.16.4
- de.heuboe.asfinag:vmis2-control-datamodel ............. 2.0.3 -> 2.0.5


## 1.3.3
- new type and reference type communication controller added

## 1.3.2
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.3 -> 2.11.7
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.0 -> 1.16.3
- de.heuboe.asfinag:vmis2-control-datamodel ........... 1.19.12 -> 2.0.3

## 1.3.1
**Added**
- new reference type and sensor for traffic control technology (tct) [FG6, VLT]

## 1.3.0
- de.heuboe.asfinag:vmis2-control-datamodel ........... 1.19.4 -> 1.19.9
- de.heuboe.asfinag:vmis2-base-parent .............. 2.11.1 -> 2.11.3
- add config service

## 1.2.8
- update control datamodel to 1.19.4

## 1.2.7
- add methods to update and remove attachedData of an infrstructureObject. 

## 1.2.6
- increase logging in getAttachedData method, to see faster differences.
- de.heuboe.asfinag:vmis2-base-parent .............. 2.10.4 -> 2.11.1

## 1.2.5
- correct sonar hints.

## 1.2.4
- update base parent pom to 2.10.4
- update control datamodel to 1.19.2

## 1.2.3
- update base parent pom to 2.10.4
- update control datamodel to 1.19.2

## 1.2.2
update base parent pom to 2.10.3
update control datamodel to 1.18.5 

## 1.2.1
update base parent pom to 2.10.2
update control datamodel to 1.18.4 

## 1.2.0
**Removed**
- Delete interface Infrastructure, because is not required

**Changed**
- vmis2-base-parent v2.9.2
- vmis2-control-datamodel v1.17.0

## 1.1.0
**Changed**
- Rename DevicePanel to VariableTrafficPanel
- update base parent pom version to 2.8.1

**Removed**
- Delete type in constructor of DisplayPanel and VariableTrafficPanel, update constructors in tests

## 1.0.0

**Added**
- Added azure-pipeline.yaml
- New InfrastructureObjects DevicePanel and DisplayPanel

**Changed**
- Major changes due to infrastructure harmonisation
- Defines for ReferenceTypes again entered as public

**Removed**
- As discussed, type parameter deleted in the constructors for Road, DetectionSite, Lane, RouteStation

**Fixed**
- Prevent NullPointerException in InfrastructureObjectBase

