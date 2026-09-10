buildMST.cmd erzeugt die DB-Versorgung eines MeasurementSiteTables


1) Beim Aufruf von buildMST.cmd sind drei (oder vier) Parameter zu übergeben:


   a) MST-ID: 		  Der Datenart datex2MST können die aktuell verwendeten MST-IDs entnommen werden.	

   b) <version>:      Der zweite Parameter gibt die MST-Version an. Man wird den um 1 erhöhten Wert der letzten Version verwenden. 
					  (Der Datenart datex2MST kann man die aktuell verwendete Version entnehmen.)    
 				 
   c) <defVersion>:   Der dritte Parameter gibt die Version der MST-Definition an (s. Datenart datex2MSTDefinition). I.d.R. wird man
                      hier den Wert aktuellen Version (s. datex2MST::defVersion) verwenden.  

   d) <idVersion>: 	  Der vierte Parameter definiert, wie die IDs der MeasurementSites erzeugt werden.
 
	 - Ist der Parameter leer, werden die GeoDyn2-IDs der Messstellen verwendet

	 - Im anderen Fall werden die IDs (abhängig vom Wert des Parameters) ohne Bezug zur GoeDyn2-ID vergeben.

    Insbesondere können so mit einer neuen MST-Version gänzlich neue MeasurementSite-IDs vergeben
    werden. Andererseits werden durch Beibehalten der <idVersion> der Vorversion auch die 
    MeasurementSite-IDs der Vorversion erhalten (Neue Measurement-Sites erhalten natürlich 
    neue IDs). 	      

2) Nur XML-Datei erzeugen:

Ist die MST bereits erzeugt und soll nur die XML-Datei erstellt werden, ruft man

buildMST_XML.cmd <MST-ID> <version> 

auf. Die Version muss also in der Datenart datex2MST bereits vorliegen !



3) Aufgerufene Anwendungen:

a) Die Anwendung mstBuilderSrv (hbMonitor.xml) erzeugt die MST im DB-Modul datex2MST. Die Anwendung d2MSTPubBuilder (hbMonitor.xml) erstellt die XML-Datei. 


4) Detailliertere Informationen unter:

https://appl-srv2.heuboe.hbintern/wiki-pg/index.php/MST-Erzeugung




