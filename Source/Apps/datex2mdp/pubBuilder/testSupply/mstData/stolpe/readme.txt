Das ist die MST mit ID 'LVE_MDM' und Version '4'.

Es ist eine MST zu Messquerschnitten/Schleifen/Verkehrswerten 
in einer 'Stolpe'-Datenbank. 

Die Datei mstConfigMDM.xml ist die MST-Konfiguration.
Die Datei mstDefinitionLVE_MDM.xml ist die MST-Definition.

Ist die MST in der Datenbank nicht mehr vorhanden (oder wurde sie verändert),
ist in der Datenarte datex2MST der Datensatz mit 

mst='LVE_MDM' und verison = '4'

(falls vorhanden) zu löschen und die .dtx-Dateien in der Reihenfolge

datex2MSTConfig.dtx
datex2MSTDefinition.dtx
datex2MSTDefinitionDetail.dtx
datex2MST.dtx
datex2MSTDataSource.dtx
datex2MSTLocation.dtx
datex2MSTItem.dtx

per DDP-Tool zu importieren.
