0) buildMST.cmd baut die DB-Versorgung eine LVE-D2V2-MST für den MDM-Export 

a) Die Anwendung mstBuilderSrv_MDM (hbMonitor.xml) erzeugt die MST im DB-Modul datex2MST

b) Die Anwendung d2MSTPubBuilder_MDM (hbMonitor.xml) erzeugt dazu die MST-XML-Datei



1) Beim Aufruf von buildMST.cmd sind ein (oder zwei) Parameter zu übergeben:

   Es sollen Integer-Werte verwendet werden.

   a) <version>: Der erste Parameter gibt die MST-Version an 
                 (Inhalt der Column datex2MST.version im Datenmodell)

   b) <idVersion>: Der zweite Parameter definiert, wie die IDs der MeasurementSites erzeugt werden.
 
	 - Ist der Parameter leer, werden die GeoDyn2-IDs der Messstellen verwendet

	 - Im anderen Fall werden die IDs (abhängig vom Wert des Parameters) ohne Bezug zur GoeDyn2-ID vergeben.

    Insbesondere können so mit einer neuen MST-Version gänzlich neue MeasurementSite-IDs vergeben
    werden. Andererseits werden durch Beibehalten der <idVersion> der Vorversion auch die 
    MeasurementSite-IDs der Vorversion erhalten (Neue Measurement-Sites erhalten natürlich 
    neue IDs). 	      


2) Source-Code der Anwendungen:


a) Aufgerufene Java-Anwendung: 

\tic1\src\datex2\datex2MeasurementSiteTablePublicationEncoder\mstBuilder\mstBuilderClient


b) Angesprochene C++-CORBA-Server

\tic1\src\dat
ex2\datex2MeasurementSiteTablePublicationEncoder\mstBuilder

mstBuilderSrv.sln





3) generatePartialMST.cmd

Der Aufruf erzeugt eine partielle MST zu einer per buildMST.cmd erzeugten kompletten MST. Es wird die
Java-Anwendung d2MSTPubBuilder ausgeführt. Alle konstanten Aufruf-Parameter sind in generateSubMST.arg 
zusammengefasst. Der Aufruf arbeitet mit der MST LVE_MDM (Parameter pubInstance) Es verbleiben die Parameter

-mmtLocFilterFile
-pubFileNameExt
-mstVersion

Die Filterdatei (-mmtLocFilterFile) sollte im Unterverzeichnis 'filter' abgelegt werden. Die MST-Datei 
wird im Verzeichnis der Komplett-MSt abgelegt und ihr Name wird um den Wert von -pubFileNameExt 
erweitert. Der Auszug wird aus der mit -mstVersion angegebenen Version erzeugt.

Beispielaufruf:

D:\VZH\binScript\buildMST_MDM>generatePartialMST.cmd -mmtLocFilterFile ./filter/mmtLocFilterMST_Q_A5part.txt -pubFileNameExt vhPart -mstVersion 10


