# Changelog

## v3.5.1
- added PanelContentCorrection to InfoMeasureWithCause

## v3.5.0
- adapted model for automatisierte Infoschaltungen part 2 
- vmis2-protobuf-parent ... 3.13.0 -> 3.19.0

## v3.4.6
- added TwoPartID to InfoMeasureControlSigns message

## v3.4.5
- add proto for infrastructure object of harmonic sections.

## v3.4.4
- fixed typo of catalogue wording

## v3.4.3
- added Message InfoMeasureCatalogue

## v3.4.2
- added Proto InfoControlSign and InfoMeasure for the first part of "automatisierte Infoschaltungen"

## v3.4.1
- add proto for infrastructureobject of harmonic sections.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.19 -> 1.16.23
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 4.1.2 -> 4.1.9
- de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.6.1 -> 4.6.2


## v3.4.0
- US 29659: add string list message

## v3.3.0
- US28760: add VersionData and VersionMsg in CommonControlData
- US29069: added CommonSituationMeasure.proto for "Querdifferenzierung"
- added usage of CommonSituationMeasure.proto to Situation.proto and Measure.proto

## v3.2.1
- added MeasureAndControlSign and MeasuresAndControlSigns to Measure.proto  
  Used to keep Measures and ControlSignMeasures together in one data structure, so they can be written to one topic (see https://vmis-ehe.visualstudio.com/vmis2/_workitems/edit/28697)

## v3.2.0
- particular matter: add UNDEFINED state to enum values

## v3.1.9
- update VMIS2.Transfer.Interface

## v3.1.8
- added Modifier to the ControlSignMeasure and Reasons 

## v3.1.7
- add event_category to event_category.
- 
## v3.1.6
- release again with pdb release plugin

## v3.1.5
- remove incorrect Any.proto import
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 4.1.1 -> 4.1.2
- de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.6.0 -> 4.6.1
- de.heuboe.asfinag:vmis2-protobuf-parent .......... 3.12.0 -> 3.13.0



## v3.1.4
- Add CfgAqMsg and GeoFeatureMsg, to publish CfgAqs and GeoFeatures to broker.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.18 -> 1.16.19


## v3.1.3
- Add InfrastructureObjectMsg, InfrastructureObject and WzgSymbolsMsg to publish infrastructure objects to broker.
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.15 -> 1.16.18
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 4.0.5 -> 4.1.1
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ...0.7.2 -> 0.8.0
- de.heuboe.asfinag:vmis2-protobuf-parent .......... 3.11.0 -> 3.12.0



## v3.1.2
- add azure-pipeline-csharp.yaml

## v3.1.1
- merge of data reduction and event branch

## v3.1.0
- add protos for event class proxy
- measure, measureClass: add validity_timespec_event_definition_id, validity_timespec_event_negation

## v3.0.9
- added autarky flag to Situation/MergedSituation/Measure/ControlSignMeasure
- added Type of Object for MergedSituation/Measure
- added linkage of Measure Type to ControlSignMeasure

## v3.0.8
- add deleted flag to control sign measures.

## v3.0.7
- add operator_adjusted flag to control sign measures.

## v3.0.6
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 4.0.4 -> 4.0.5

## v3.0.5
- add azure-pipeline-csharp.yaml

## v3.0.4
- release again with pdb plugin, to run pipeline

## v3.0.3
* add new data model for progression * add validity section of measure to control sign measure
* add validity section and class id of measure to MeasureSequenceState
- de.heuboe.asfinag:vmis2-configservice-interface ... 1.16.14 -> 1.16.15
-  de.heuboe.asfinag:vmis2-protobuf-parent ........... 3.9.0 -> 3.11.0

## v3.0.2
* Add agg interval to DataSeries

## v3.0.1
* add user, changed_at, comment in SituationRecognitionValiditySectionsCatalogue

## v3.0.0
* add SituationRecognitionValiditySectionsCatalogue message (Situation.proto)

## v2.5.0
* manual measures with recurrent validity timespecs

## v2.4.0
* changes for data reduction:
* add sequential_number to situation
* add sequential_number to mergedSituations
* add sequential_number to measures

## v2.3.0
- clean up ParticularMatter.proto

## v2.2.3 
- release again because of network problems with owasp db.

## v2.2.2
- add extra origin for tunnel content.

## v2.2.1
- added measure selection active flag for measure class

## v2.2.0
- Tunnel content to VersionedDevicePanelContent and ConcreteControlSign.
- de.heuboe.asfinag:vmis2-protobuf-parent ............ 3.5.0 -> 3.6.0
- de.heuboe.asfinag:vmis2-configservice-interface .... 1.16.8 -> 1.16.12
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 3.9.2 -> 4.0.3
- de.heuboe.asfinag:vmis2-receiving-processing-datamodel ... 0.6.3 -> 0.7.2


## v2.1.2
- add time-based cross section event pattern

## v2.1.1
- deleted simple timeSpec definition for measure class and added a complex one based on iCal RFC 2445 as String

## v2.1.0
- delete MainFunnel and NoMainFunnel.
- change structure in control sign.
- add FunnelDefinition, SBAFunnel and WTVFunnel.
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.7 -> 1.16.8


## v2.0.12
- added deleted flag for mergedSituation and measures

## v2.0.11
- add lane_reference for MergedSituation


## v2.0.10
- add list message for TrafficTimeSeries
- add iid to immission messages

## v2.0.9
- create new enum for measure state in wta wtv priority tables. 
- de.heuboe.asfinag:vmis2-protobuf-parent ............ 3.3.7 -> 3.4.2


## v2.0.8
- remove java 9 from pom.

## v2.0.7
- added fix attributes to track external situation in situation, measures and control sign measures.
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.16.2 -> 1.16.6
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 3.7.7 -> 3.8.7


## v2.0.6
- added attributes for tracking to situation and shifting values & shifting areas to measureclass

## v2.0.5
- add DisplayPanelLogicalOperationMode enum, entity and container to control sign proto.

## v2.0.4
- insert missing comment fields to catalogues.

## v2.0.3
- de.heuboe.asfinag:vmis2-protobuf-parent ............ 3.3.6 -> 3.3.7

## v2.0.2
- move user and change_at field to catalogues.
- add comment field in all catalogues.
- set number of old  user and change_at field to reserved.

## v2.0.1
- set number of old LogicalOperatingMode to reserved.
- give MeasurePriorityMode a new number.

## v2.0.0
- de.heuboe.asfinag:vmis2-protobuf-parent ............ 3.3.2 -> 3.3.6
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.15.8 -> 1.16.2
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 3.7.3 -> 3.7.7
- de.heuboe.asfinag:vmis2-jprotoc-transferinterface ..... 4.2.0 -> 4.2.2
- add new fields to catalouges for logging, user and changed at.
- changed ManApprovalMeasure to ManChangeableMeasure
- add MeasuresAdjusted
- move LogicalOperatingMode to CommonControlData to resolve circular dependency
- add VersionedDevicePanelContent
- rename LogicalOperatingMode to MeasurePriorityMode
- remove MeasureStateWrapper from MeasureState

## v1.19.14
**Added**
- field processing_time to message ControlSignReason in ControlSign.proto
- de.heuboe.asfinag:vmis2-protobuf-parent ............ 3.3.0 -> 3.3.2

## v1.19.13
**Added**
- new list of contained manual situations in merged situations

## v1.19.12
**Added**
- Missing stereotypes added

## v1.19.11
**Added**
- add new proto VariableTrafficPanelMonitoringState

## v1.19.10
- add new attribute OriginControlSignMeasure to DevicePanelContent and  OneControlSignReasonMeasure (VMIS2-S1ANF-592 and 593)

## v1.19.9
**Added**
- protoNuget: geomanager dependency

## v1.19.8
** Added**
- add lane_reference to Situation ((VMIS2-S1ANF-785)
- add enable_lane_reference to SituationClass (VMIS2-S1ANF-789)
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.15.6 -> 1.15.8
- de.heuboe.asfinag:vmis2-grpc-interface-geomanager ..... 3.7.1 -> 3.7.3

## v1.19.7
** Added**
- add section with op-metering and coordinates to validitySection

**Changed**
- update vmis2-protobuf-parent to 3.2.3
- protoNuget: update transfer-interface to 4.1.0

## v1.19.6
**Changed**
- protoNuget: update transfer-interface to 4.0.4

## v1.19.5
**Changed**
- move attribute manual_handling from SituationClass to SituationSubClass

## v1.19.4
- de.heuboe.asfinag:vmis2-configservice-interface ..... 1.15.1 -> 1.15.6

## v1.19.3

**Added**
- add attribute source_sensors to Situation and MergedSituation
- add message Sensor
- [Issue #2](https://gitlab.heuboe.hbintern/VMIS2/control/base/vmis2-control-datamodel/-/issues/2) - proto Definitionen um VMIS2 spezifische Dokumentationsinformationen erweitern (add "required")
- add attribute manual_handling to SituationClass and MeasureClass

## v1.19.2

**Added**

- [Issue #2](https://gitlab.heuboe.hbintern/VMIS2/control/base/vmis2-control-datamodel/-/issues/2) - proto Definitionen um VMIS2 spezifische Dokumentationsinformationen erweitern

## v1.19.1
- ControlSign.proto: add Message DisplayPanelLockMatrices, DisplayPanelLockMatrix, DisplayPanelLockRow and DisplayPanelLockEntry.


## v1.19.0

**Changed**

- vmis2-protobuf-parent v3.0.0

## v1.18.5
**Changed**
- vmis2-protobuf-parent v2.5.3
- CommonControlData.proto: message TwoPartIdentification change comment
- Situation.proto: message SourceSituation external reference to Situation is marked

**Removed**
- Situation.proto: message Situation: field "repeated string situation_ref = 14" 
  is deleted and reserved


## v1.18.4
- update to vmis2-protobuf-parent v2.5.2 

## v1.18.3
**Changed**
- ControlSign.proto: add Message PriorityTables, PriorityTable an PriorityEntry.

## v1.18.1
**Changed**
- Situation.proto: Message Situation:
    the fields float result_quality = 9 and float algorithm_quality = 10 
    now contain floating point percentages => only comment changed
- ControlSign.proto: add Message ConreteControlSign and ConcreteControlSigns
    
## v1.18.0
**Added**
- Search for external references and marked them for the documentation in the messages, 
  e.g one reference from Measure to Situation:
    repeated TwoPartIdentification situation_two_part_ids = 9 [(.heuboe.protobuf.extRef) = 
    { type: "eu.vmis_ehe.vmis2.control.data.Situation", field: "two_part_id" }];
- ControlSign.proto: added field validity_timespec to message ControlSignMeasure

**Changed**
- Multi-line comments (/*....*/) for fields positioned above the fields and changed to // comments
- vmis2-protobuf-parent v2.4.3

**Removed**
- Situation.proto: Field check_time and source_situations are removed in message Situation => 
        for compatibility reasons, the field values were set to reserved
- Infrastructure.proto        
    
## v1.17.0
**Changed**
* repeated TwoPartIdentification attribute_class to repeated string attribute_class

**Added**
* MethodOfConflictAdjustment (STATEPRIORITYBASED, QUALITYBASED, NONE) 
  * if conflictAdjustment was false use NONE now
  * if conflictAdjustment was true use STATEPRIORITYBASED now
* situationQualityMinimum, algorithmQualityMinimum

**Removed**
* bool conflictAdjustment

## v1.16.1
**Added** 
- IID for SuppressedMeasure

## v1.16.0
**Added**
- iid for MeasureCatalogue, SituationCatalogue and AttributeCatalogue
- ControlSignReason 

**Changed**
- update vmis2-protobuf-parent to 2.4.0

## v1.15.3
**Changed**
- SuppressedMeasures adapted

## v1.15.2
**Added**
- csharp-namespace

## v1.15.1
**Added**
- SuppressedMeasure

**Changed**
- ControlSign: ValiditySection as List

**Removed**
- ResultingPanelContents

## v1.15.0
**Added**
- message ControlSignMeasures 
- MeasureClass: new attributes measure_category and funnel_behaviour

**Changed**
- rename id to iid for Measures, MergedSituations, Situations

## v1.14.6
* add ControlSignMeasure
* add ResultingPanelContents
* update AlgoState with timestamp

## v1.14.5
Update to new parent pom 2.0.0
Update azure-pipeline.yaml
AlgoStates added to proto

## v1.14.3

Update parent pom to 1.5.0
remove protos which go to vmis2-receiving-processing-datamodel

## v1.14.1

Updated parent pom to 1.3.2

## v1.14.0

**Changed**

* Added EDBInterface.proto
* Added Message EDBAttributes to support 'evons' EDB process. EDBAttributes has been 
added to: 
  * Situations
  * MergedSituations
  * Measures


