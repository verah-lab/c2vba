# Changelog

## v1.1.0
**Added**
- new timestamp 'tls_time' for all environmental data records (pl-check, replaced, smoothing, los determination)
**Changed**
- de.heuboe.asfinag:vmis2-protobuf-parent .......... 3.14.0 -> 3.17.0

## v1.0.1
**Added**
- new field DataStateEd in LosEnvData.DarkBrightDetectionLosData to determine if dark/bright los is PLAUSIBLE or not.
**Changed**
- update parent to 3.14.0

## v1.0.0
**Added**
- add v_KFZ_min and v_KFZ_max to ShortTermMeasurementAggregatedDataDetSite
- undo enum extension of unknown values in RoadSurface.Condition (branch: roadsurface_condition_enums)

## v0.8.1
**Added**
- add v_KFZ_delta to ShortTermMeasurementAggregatedDataDetSite

## v0.8.0
**Changed**
In order to include the speed of the slowest vehicle in the data collection 
and data preparation chain, the following protobuf files and messages have been extended.
- SyncColletedData.proto:
   - message ShortTermCollectedDataLane
- PlCheckData.proto:
  - message ShortTermPlCheckedDataLane
- MeasurementReplacement.proto:
  - message ShortTermMeasurementReplacedDataLane
- MeasurementPreparationData.proto:
  - message AnalyticalDataLane
  - message AnalyticalDataDetSite
- MeasurementAggregationDataProto.proto:
  - message ShortTermMeasurementAggregatedDataLane
  - message ShortTermMeasurementAggregatedDataDetSite 
- Dependency: de.heuboe.asfinag:vmis2-protobuf-parent ........... 3.5.0 -> 3.11.0   
 
## v0.7.2
**Changed**
- typo

## v0.7.1
**Added**
- PShortTermFictitiousReplacedDataLane (as copy from  PShortTermMeasurementReplacedDataLane) to prevent, that different
  topics contains the same data type.

## v0.7.0
**Changed**
- LongTernData.proto: 
  - message LongTermData => message LongTermDataLane
  - message LongTermDataLane.SpeedClassesVersion24:
   - int32 q = 2; is deleted, because is not needed for long term data version 24
   - SpeedClasses speed_classes = 7; has been replaced by int32 numVClasses = 8; and repeated int32 vClasses = 9;
  - message LongTermDataLaneList: repeated LongTermData data = 1; => repeated LongTermDataLane data = 1;
  - message LongTermDataDetSiteList: repeated LongTermData data = 1; => repeated LongTermDataDetSite data = 1;
  
**Added**
- LongTernData.proto: message LongTermDataDetSite

## v0.6.3
**Changed**
- LongTernData.proto: message LongTermDataLane => message LongTermData
**Added**
- LongTernData.proto: message LongTermDataDetSiteList

## v0.6.2
**Changed**
- SyncCollectedData.proto: message ShortTermCollectedTrafficCategoriesLane: field category_boudaries_PKW => category_boundaries_PKW

## v0.6.1
**Added**
- SyncCollectedData.proto: Added list of ShortTermCollectedTrafficCategoriesLane => message ShortTermCollectedTrafficCategoriesLanes

## v0.6.0
These are actually the changes from v0.5.11. Because of the change "enum TlsDataVersion: change field name form VERSION_INCORRECT to SWITCHED_OFF", the middle version number has now been incremented.
- LongTermData.proto: message LongTermDataLane: New field LongTermRecoverRequest.StateLogicalPassivation logical_passive
- SyncCollectedData.proto: enum TlsDataVersion: change field name form VERSION_INCORRECT to SWITCHED_OFF
**Added**
- LongTermData.proto: message LongTermDataLanes (List of LongTermDataLane)

## v0.5.11
**Changed**
- LongTermData.proto: message LongTermDataLane: New field LongTermRecoverRequest.StateLogicalPassivation logical_passive
- SyncCollectedData.proto: enum TlsDataVersion: change field name form VERSION_INCORRECT to SWITCHED_OFF
**Added**
- LongTermData.proto: message LongTermDataLanes (List of LongTermDataLane)

## v0.5.10
**Added**
- new DataStateEd state for: replaced value was not plausible (after second pl-check).

## v0.5.9
**Added**
- add longterm data
- add density qb value, state and quality to the vd-preparation and vd-aggregation

## v0.5.8
- add density qb value, state and quality to the vd-preparation and vd-aggregation. 

## v0.5.7
**Added**
- traffic control technology data (TCT / Verkehersleittechnik VLT,  FG 6)
- long term data

## v0.5.6
**Changed**
- rollback FailedPlCheckData change

## v0.5.5
**Changed**
- fixed typo

## v0.5.4
**Added**
- Missing stereotypes added

## v0.5.3
**Removed**
- LosParameterEnvData.proto
- MeasurementSmoothingParameterEnvData.proto
- PLCheckParameterEnvData.proto
- MeasurementAggregationParameterData.proto

**Added**
- Stereotypes ENTITY, CONTAINER and DATATYPE inserted
- New protos for traffic control technology data (VLT / FG6); fixed typos

**Changed**
- multiline comment with /**/ replaced by //
- vmis2-protobuf-parent version 3.3.0

## v0.5.2
**Changed**
- changed failedPlCheck for environmental data because of new DataStateEd

## v0.5.1
**Changed**
- aggregated all data states of environmental data

## v0.5.0
- renamed ReplacedData to ReplacedRawData   (environmental data)
- renamed PlCheckedReplacedData to ReplacedData     (environmental data)

## v0.4.2
**Added**
- PlCheckCategories for replaced data
- ouput data for pl-checked replaced data

## v0.4.1
**Added**
- added data lists to write to kafka for environmental data (smoothing, los determination)

## v0.4.0
- Update protobuf parent

## v0.3.8
**Added**
- added pl-check parameter for environmental data.

## v0.3.7
**Added**
- added protos for los determination of environmental data (level of service).

## v0.3.6
**Changed**
- changed datatype of quality of smoothed values from int32 to float.

## v0.3.5
**Changed**
- do not use list of MeasurementSmoothingFactors

## v0.3.4
**Added**
- protos for measurement replaced environmental data

## v0.3.3
**Changed**
- MeasurementPreparationData.proto: Message AnalyticalDataLane, change field name b_KFZ to b
- vmis2-protobuf-parent version 2.4.3

## v0.3.2
**Changed**
- vmis2-protobuf-parent version 2.4.1

**Removed**
- MeasurementReplacementParameterData.proto deleted, because Pojo-classes are no longer used in the measurement-replacement-algo

## v0.3.1
This release does not actually contain any changes, was only necessary because the entry of interface version 0.3.0 in the proto files was not pushed for release 0.3.0..

## v0.3.0
**Changed**
- renamed pl-check ed categories

**Removed**
- PlCheckParameterData.proto deleted

## v0.2.11
**Changed**
- vmis2-protobuf-parent version 2.4.0

**Removed**
- delete MeasurementPreparationParameterData.proto, realize parameter over plain old java objects in algo.

## v0.2.10
**Changed**
- vmis2-protobuf-parent version 2.3.0

## v0.2.9
- Rename iid to id for list of FailedPlCheckData for ONE lane id

## v0.2.8

### Changed
- Rename id to iid for all lists of lanes or detection sites written per road in data collection and data preparation

