0) buildMST.cmd baut die DB-Versorgung eine LVE-D2V2-MST für den MDM-Export 

a) Die Anwedung mstBuilderSrv_MDM (hbMonitor.xml) erzeugt die MST im DB-Modul datex2MST

b) Die Anwedung d2MSTPubBuilder_MDM (hbMonitor.xml) erzeugt dazu die MST-XML-Datei



1) Beim Aufruf von buildMST.cmd ist die Version (Column datex2MST.version im Datenmodell) als Aufruf-Parameter zu übergeben:


2) Source-Code der Anwendungen:


a) Aufgerufene Java-Anwendung: 

\tic1\src\datex2\datex2MeasurementSiteTablePublicationEncoder\mstBuilder\mstBuilderClient


b) Angesprochene C++-CORBA-Server

\tic1\src\datex2\datex2MeasurementSiteTablePublicationEncoder\mstBuilder

mstBuilderSrv.sln