# datex2MST

[Erzeugung von DATEX-II-MeasurementSite-Publikationen im Confluence](https://con.heuboe.de/pages/viewpage.action?pageId=167215908)


**datex2MSTBuilder**

Die App _datex2MSTBuilder_ (Verzeichnis pubBuilder) DATEX-II-MeasurementSite-Publikationen. Sie implementiert das WebService-Interface
*D2MSTPubService*. Bei Aufruf von createMSTPublicationFileDef() wird ein DATEX-II-XML-Dokument zur mstId und mstVersion erzeugt und als Datei abgelegt. Die MeasurementSite-Publikation beschreibt die Verkehrsinfrastruktur zu den Verkehrswerten in einer MeasuredData-Publikation. Das sind etwa Mess-Querschnitte (Ort im Verkehrsnetz, Detektoren, Datentypen: Verkehrsstärke, Geschwindigkeit) 

**datex2MSTLocServiceBuilder**

Der _datex2MSTLocServiceBuilder_ erzeugt die detaillierte, persistente MST-Definition. Er bezieht Location-Daten über einen MST-Location-Service. Zur  Erzeugung der XML-Repräsentation (DATEX-II-MeasurementSite-Publikation) wird der datex2MSTBuilder aufgerufen.


Artefakte 

| Name | Beschreibung |
| ------ | ------ |
| datex2MSTConfig | Java-Binding zu den XML-Schemas der MST-Konfiguration und MST-Definition |
| datex2MSTBuilder | Erzeugt DATEX-II-MeasurementSite-Publikationen |
| mstLocServiceInterface | MST-Location-Service-interface |
| datex2MSTLocServiceBuilder | Erzeugt die detaillierte, persistente MST-Definition. Bezieht Location-Daten über einen MST-Location-Service |



