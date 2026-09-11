1) Batch-Dateien

createMST-LVE.cmd erzeugt einen LVE-MeasurementSiteTable, sowohl die Datenbankausprägung in der MongoDB 'datex2MST' als auch eine XML-Datei in ../MST/LVE. createMST-UFD.cmd erzeugt analog einen UFD-MeasurementSiteTable.

Der Pfad der Java-VM ist anzupassen 


2) Aufruf-Parameter

Die Aufruf-Parameter sind in application-lve.properties bzw. application-ufd.properties anzupassen

a) Umgebungsparameter: Datenbank-Verbindung, lokale Pfade

b) MST-Parameter: 

de.heuboe.datex2.mst.mstVersion 

ist jeweils zu inkrementieren. Die bereits verwendeten Versionen können der Collection datex2MST der MongoDB entnommen werden. (Es ist möglich, eine vorhandene Version zu überschreiben.)


