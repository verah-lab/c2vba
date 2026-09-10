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


2) Aufgerufene Anwendungen:

a) Die Anwendung mstBuilderSrv erzeugt die MST im DB-Modul datex2MST. Der Status der Erzeugung wird als Datensatz der
   Datenart datex2MSTBuildState über die GeoDyn-Datenverteilung publiziert.   

b) Der d2MSTPubBuilder erzeugt dazu die MST-XML-Datei.  





