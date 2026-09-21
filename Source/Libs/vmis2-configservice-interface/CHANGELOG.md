# Changelog

## 1.16.27

- Added VKP infos to message 'VersionDesc'

## 1.16.26

- Added VKP infos to version overview

## 1.16.25

- Added field 'MoreInfo' to message 'TlsEa'
- Added Stuff for VKP data management

## 1.16.24

- Added FG3 types FBG(59), NM(62)
- Added relation type SIT_WCS

## 1.16.23

- Added field 'name' to message 'AqCluster'

## 1.16.22

- Added bit 'all_changed' to 'DataChanges' and 'DataChangeTrigger' messages

## 1.16.21

- Added CfgGroup support
- Added field 'extra' to 'TlsDevice' message

## 1.16.20

- Added fields 'user' and 'touched' to message 'RelInfo'

## 1.16.19

- Added new method 'WriteTunnelData' to ConfigService
- Added optional field 'road_id' to message 'TunnelDirection' 
- Deprecated method 'SetItems'
- Added tunnel version info to VersionDesc and VersionOverview

## 1.16.18

- Added new WzgType 'FASI'

## 1.16.17

- Added support for new type FCD
- Deprecated field 'version_id' in message 'PersistSnapshotsRequest' (ignored in future services)
- Added 'HistLog' messages to DataChanges.proto

## 1.16.16

- Added ConfigItemType FCD
- Refactored test client code to better demonstrate some interface features

## 1.16.15

- Parent .pom updated

## 1.16.14

- New field 'previous_version' in 'ActivateVersionRequest'
- New UdeType FGW = 132;
- New field 'snapshot_count' in 'ManEntry' 

## 1.16.13

- Added relation SIT_NOI
- Update to [vmis2-protobuf-parent v3.6.0](http://pdb.heuboe.hbintern/pdb/#version;id=36752) (was 3.4.1)
- Switch to new build pipeline

## 1.16.12

- Added relation SIT_PMX

## 1.16.11

- Added fields 'cmp_components' and 'cmp_programs' to message 'CfgWzg'
- Added new Value SUB to enum ConfigItemType
- Added field 'geo_changes' to 'AreaChange'

## 1.16.10

- Added WZG type 'WTA_B'
- Added field 'user' to message 'Relation'
- Added return values for 'importTunnelData' methods  
- Changed field 'mapped_program_response' in 'TrafficProgram' to string (from int32)

## 1.16.9

- Added new item type 'Tunnel' to GetItems/GetAllItems request
- Added new method 'SetItems' to ConfigService interface
- Added methods ImportTunnelData/ImportTunnelDataStream to VersionService interface 
- Added field 'origin' to message 'SnapshotUpdate'

## 1.16.8

- Added field 'cluster_id' to message 'CfgMq'
- Added relation type SIT_AID

## 1.16.7

- Added relation types SIT_IMM and SIT_ATC
- Added methods ImportManualData/ImportManualDataStream to VersionService interface
- Added field 'user' to ConfigService's request messages that change data

## 1.16.6

- Added field 'ip_address_alt' to message 'TlsDevice'
- Added field 'activation_token' to activation and data change messages
- Added new message 'DataChangeTrigger' to DataChanges.proto

## 1.16.5
**(This version's version service interface is incompatible to previous versions)**

- Added message 'DataChanges' 
- Fixed a typo in SIT_AWD ENUM constant (from 1004 to 10004) **(inkompatible)**


## 1.16.4
**(This version's version service interface is incompatible to previous versions)**

- Added new message 'VersionDbKey', and used it in message 'VersionDescriptor'
- Added new message 'VersionOverview', used to report version data to KAFKA
- Changed method's 'GetAvailableVersions' return value to 'VersionOverview' **(inkompatible)**
- Added new method CheckVersion to interface 'VersionService'
- Added field 'eclipsing' to message 'Relation'
- Added new methods 'RestoreDefaultRelations' and 'RestoreVirtualDefaultItems' to interface 'ConfigService'
- Added field 'delete_snapshots' to messages 'ActivateVersion' and 'ActivateVersionRequest' 

## 1.16.3
**(This version is incompatible to previous versions)**

- Added new LaneType.ALL
- Added field 'source_ids' to message 'VUDef' in 'UpdateVirtualItemsRequest'
- removed return value from method 'DeleteVirtualItems' **(inkompatible)**
- Adjusted CreateExtDataRequest to use PVersionKey (instead of PBaseVersionKey) **(inkompatible)** 

## 1.16.2
**(This version's version service interface is incompatible to previous versions)**

- Added new method 'PushConfigData' to VersionService interface
- Refactored method 'ActivateVersion' to match above method
- Added new message 'ActivateVersion' to ConfigDbStuff

## 1.16.1

- Added new relation types SIT_CCW, SIT_AWD
- Added new VltType LEV(57)
- Added new AqType WWW_FG5(12)
- Added fields 'config_activated' and 'mode' to message 'ServiceVersion'

## 1.16.0
**(This version is incompatible to previous versions)**

- Added data model dokumentation options
- Added field 'global_h_index' to message 'LanePos'
- Added field 'ip_address' to message 'TlsDevice'
- Added new relation types REL_NEIGHBOUR_FS_QKFZ and REL_NEIGHBOUR_FS_VPKW
- Refactored message 'ItemUpdates' to avoid breaking KAFKA for large updates **(incompatible)**

  #####Version handling stuff only
- Added new method 'CreateConfigVersion' to VersionService
- Refactored version activation, deletion, and info methods **(incompatible)**
- Removed 'master' from all relevant version id fields' names **(incompatible)**
- Added fields 'chunk_num' and 'chunk_count' to message 'BaseData'
- Replaced DB VersionDescriptor's 'timestamp' with 'activated' and 'created' **(incompatible)**
 
## 1.15.8

- Added message AqLinkData to Relation 

## 1.15.7

- Added new method 'GetItemInfos' to ConfigService
- Added new relation type REL_AQ_HK
- Removed field 'predecessors' from message 'Relation'
- Removed ATTR type WZG_TYPE
- ADDED ATTR type EFH
- Added MULTIPLE to enum 'LaneType'
- Added VHT to enum 'WzgType'

## 1.15.6

- Added field 'scope' to most config items

## 1.15.5

- Added field 'blink_frequency' to message 'WzgSymbol'
- Moved messages used for JSon creation only to DbStuff.proto

## 1.15.4

- Added new internal message 'Versions'

## 1.15.3

- Fixed typo in UdeType: Changed LAEQ's value to 87 (from 387)
- Added field 'name' to message VirtualSensor

## 1.15.2

**(This version is incompatible to previous versions)**

- Replaced methods setVirtualSensors,removeVirtualSensors with read-/create-/update-/deleteVirtualItems **(incompatible)** 
- Added new relation type REL_SUPPLEMENTARY_WZG (Zusatz-WZG)
- Added new relation type REL_WRONG_WAY_DRIVER (Geisterfahrer)
- Added field 'predecessors' to message 'Relation' (for REL_DLA_* relation types)

## 1.15.1

**(This version is incompatible to previous versions)**

- Refactured AqType enum constants according to new input from AG **(incompatible)**
- Refactured WzgType enum constants according to new input from AG **(incompatible)**
- Fixed TrafficSignificance enum values YELLOW_ARROW_LEFT and YELLOW_ARROW_RIGHT (from GREEN_ARROW_..) **(incompatible)**
- Fixed relation type name REL_LPL9_SW_NS_RLF (from REL_LPL9_SW_RLF) **(incompatible)**
- Extended CfgWzg and WzgSymbol messages to accommodate for component WZGs
- Added field 'clusters' to message CfgAq

## v1.14.2

- Added field 'type' to CfgVdeSensor and CfgKri
- Added field 'uz_id' to GetItemRelationsRequest
- Added new relation type SIT_CWI

## v1.14.1

- Fixed previous release

## v1.14.0

- Update to [vmis2-protobuf-parent v3.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=30221) (was 2.5.3)
- Update to [vmis2-version-notifier-datamodel v1.0.0](http://pdb.heuboe.hbintern/pdb/#version;id=30223) (was 0.0.2)

## v1.13.4

- Refactored virtual sensor handling: New methods SetVirtualSensors, RemoveVirtualSensors
- Refactored method ClearManualRelations to RemoveManualRelations
- Removed field 'mode' from message SetItemRelationsRequest
- Added field 'manual' to message Relation
- Added more relation types
- Changed messages ItemUpdates and RelationUpdates to include all relevant changes into one update
- Parent .pom update
- Cosmetic changes/cleanup

## v1.13.3

- Added field 'q_id' to CfgVdeSensor and CfgWzg
- Added field 'encoding' to import request

## v1.13.2

- Using external 'Notification' (from vmis2-version-notifier-datamodel)
- Added enum RelationType for relation types
- Added field virtual_sensor to Relation

## v1.13.1

- Added field 'cluster_ids' to message CfgAq
- Removed LaneType 'between': That's now a flag in LanePos
- Changed message 'Descriptor' in VersionService's CreateManualDataRequest to 'ManVersionDesc'

## v1.13.0

- Moved version management methods to new VersionService Interface
- Added field 'versions' to GetItemsRequest and GetAllItemsRequest 
- Fixed UdeSensor type SEW to SWE

## v1.0.12

- Added field 'display_type' to CfgWzg
- Changed interface version type to .heuboe.protobuf.interface_version

## v1.0.11

- Refactured .proto files so UdeSensors can have a Relation
- Changed lots of ENUMS & TYPES according to 'PLaPB 800.566.2602 TSt [V3.00]-Verortung_von_Betriebsmitteln_[V1.00]_v12'
- Added a 'Location' field to most types
- Renamed method SetItemRelations to SetManualRelations
- Added method ClearManualRelations
- Changed Relation's type to 'string'
- Simplified Relation's targets
- Renamed relation type NEIGHBOUR_MQ to REL_NEIGHBOUR_MQ
- Added field 'virtual_source' to CfgUdeSensor, for virtual sensors
- Added field 'virtual_target' to TlsEa, for virtual EAs
