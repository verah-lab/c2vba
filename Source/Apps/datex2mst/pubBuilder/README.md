# datex2MSTBuilder

Die App datex2MSTBuilder (Verzeichnis pubBuilder) DATEX-II-MeasurementSite-Publikationen. Sie implementiert das WebService-Interface
D2MSTPubService. Bei Aufruf von createMSTPublicationFileDef() wird ein DATEX-II-XML-Dokument zur mstId und mstVersion erzeugt und als Datei abgelegt. Die MeasurementSite-Publikation beschreibt die Verkehrsinfrastruktur zu den Verkehrswerten in einer MeasuredData-Publikation. Das sind etwa Mess-Querschnitte (Ort im Verkehrsnetz, Detektoren, Datentypen: Verkehrsstärke, Geschwindigkeit)

**Entwicklungsziel**

Der datex2MSTBuilder wird auf die Implementierung des  WebService-Interface D2MSTPubService reduziert. Er wird nur als Bibliothek vom datex2MSTLocServiceBuilder genutzt. Er wird nicht mehr als App ausgeführt. Die Anbindung an GeoDyn-Persistenz und -Datenverteilung wird entfernt. Das wird als ein neues Artefakt angelegt. 
