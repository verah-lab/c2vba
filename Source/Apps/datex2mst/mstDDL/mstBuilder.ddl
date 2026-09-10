// 
// (C) Copyright 1998-1999 Heusch/Boesefeldt GmbH.
// ALL RIGHTS RESERVED
//
// FILE:        mstBuilder.ddl
// AUTHOR:      Peter Schmitz
// COMMENT:     Data model for MeasurementSiteTable (Datex 2)
// DATE:	    07.05.2010
// HISTORY ////////////////////////////////////////////////////////////////////
// DATE			WHO		WHAT
///////////////////////////////////////////////////////////////////////////////
#ifndef DATEX2MST_DDL		// DDL-Guard
#define DATEX2MST_DDL

////////////////////////////////////////////////////////////////////////////////
// Include files
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// MODULE:      datex2MST
// USING:     
// COMMENT:     Data model for MeasurementSiteTable (Datex 2)
///////////////////////////////////////////////////////////////////////////////

#include "keep-defines.ddl"


module datex2MST "MeasurementSiteTable (Datex 2) data model" 
{


// ps 05.07.2011
// Ursprünglicher Name ('buildState') kollidierte mit Domain-Namen
// in anderem Modul: deshalb umbenannt. Die vom DDL-Compiler
// auszuführenden Änderungen sollten kein Datenart-Recreate 
// umfassen !
domain build_State "Status der MST-Erstellung"
range {
0 "build queued",
1 "build started",
2 "configuration parsed",
3 "saving records",
4 "record building finished",
5 "build failed",
6 "build aborted",
7 "finished"
} as byte;



history class datex2MST "MST-Kennungen"
{
	primary key (char 		mst[64]	    		"Eindeutige MST-Kennung"); 
	primary key (char 		version[20]	    	"Version");
	column		(char 		description[512]	"MST-Beschreibung");

	column		(char 		d2Version[20]		"Datex-II-Version");
	column		(char 		defVersion[20]		"Version der MST-Definition");  // references datex2MSTDefinition(defVersion)  

	column		(bool       active				"Aktive Version: Höchstens eine Version sollte aktiv sein");
} keep OPERATING_DATA;


history class datex2MSTDefinition "Definition einer MST auf der Grundlage eines MST-Mappings, gemäß http://www.heuboe.de/mst/definition"
{
	primary key  (char 	mst[64]	    		"Eindeutige MST-Kennung",	
	              char 	defVersion[20]		"Version der Definition, nicht der MST-Instanz");					
    column		 (char 	d2Version[20]  		"Datex II Version");			
    column		 (char 	schemaFile[512]  	"Datex II XML-Schema Datei");			
    column		 (char 	schemaCategory[32]  "Schema-Kategorie: etwa <mdm>");
    column       (char 	description[512]  	"Beschreibung");	
    
    column       (bool 	autoUpdate  		"true: MST wird automatisch aktualisiert");	
    column       (char 	mdpPublisher[64]  	"(hbMonitor-) Name des Prozesses, der die DATEX-II-Daten-Publikation (MDP-Publisher) erzeugt");	
    column       (long 	mdpRestartAfter     "Zeitraum (Minuten) von der Erstellung der MST bis zum Neustart des MDP-Publishers");	
    column       (char 	connPropFile[256]  	"Verbindungsparameter des MST-Empfängers (i.d.R. MDM)");	   	
} keep OPERATING_DATA;


history attribute datex2MSTDefinitionDetail "XML-Inhalt der MST-Definition"
{
	primary key  ( char 	mst[64]	    		"Eindeutige MST-Kennung",	
	               char 	defVersion[20]		"Version der Definition, nicht der MST-Instanz") references datex2MSTDefinition(mst, defVersion) on delete cascade;					
	primary key  (long  count				"Nummerierung der Teile der XML-Beschreibung der Definition" );
	column		 (char  defPart[2048]	    "Teil der XML-Beschreibung der Definition");		
	
	//foreign key ( mst, defVersion ) references datex2MSTDefinition(mst, defVersion) on delete cascade;
} keep OPERATING_DATA;


history attribute datex2MSTLocation  "MeasurementSite"
{
    primary key  ( char 	mst[64]	    		"eindeutige MST-Kennung",
	               char 	version[20]			"Version") references datex2MST(mst, version) on delete cascade;
	primary key  ( long  id );

	column		 (char  d2Id[64]	   		"MeasurementSite-ID");		
	column		 (char  locName[256]	   	"Location-Name");		
	column       (char  equipment[64]   	"Typ der Erfassungshardware" );

   	column       (char  rdsLocTblRef[3] 	"RDS Location Table Reference" );
   	column       (char  rdsLocTblVer[6] 	"RDS Location Table Version" );

   	column       (long  rdsPrimLocCode     	"RDS Primary Location" );
   	column       (long  rdsSecLocCode       "RDS Secondary Location" );

   	column       (char  rdsDirection[1]   	"RDS Direction" );
	column       (long  rdsPrimLocDist    	"RDS Primary Location Distance (meter)" );
	column       (long  rdsSecLocDist   	"RDS Secondary Location Distance (meter)" );
	
   	column       (double  startCoordX       "x-Koordinate des Anfangspunkts" );   
   	column       (double  startCoordY       "y-Koordinate des Anfangspunkts" );   

   	column       (double  endCoordX         "x-Koordinate des Endpunkts" );   
   	column       (double  endCoordY         "y-Koordinate des Endpunkts" );   
	column		 (long  geoDynId			"GeoDyn2-ID");
	
	column       (char  d2ExtRefSystem[64]  "Datex2 External Referencing: Referencing System" );
	column		 (char  d2ExtLocCode[64]	"Datex2 External Referencing: Location ID");		

   	column       (long  bearing             "Road direction (unit degree, integer value 0 - 359)" );   
	// foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;


history attribute datex2MSTCoordinate "Koordinaten einer linearen MeasurementSite-Location (Polyline)"
{
    primary key  ( char 	mst[64]	    		"Eindeutige MST-Kennung",
	               char 	version[20]	        "Version",
	               long  id                  "Location-ID") references datex2MSTLocation(mst, version, id) on delete cascade;                      

	primary key  ( long index				"Index des Punktes in der Polyline");
	column       ( double x					"x-Koordinate");
	column       ( double y                 "y-Koordinate");

	// foreign key ( mst, version, id ) references datex2MSTLocation(mst, version, id) on delete cascade;
} keep OPERATING_DATA;


history class datex2MSTD2Location "Datex2-Location-Beschreibung"
{
    primary key  (char 	mst[64]	    		"Eindeutige MST-Kennung");
	primary key  (char 	version[20]	        "Version");
	primary key  (long  id                  "Location-ID");                      

	primary key  (long  count				"Nummerierung der Teile der XML-Beschreibung der Location" );
	column		 (char  defPart[2048]	    "Teil der XML-Beschreibung der Location");		
} keep OPERATING_DATA;


history attribute  datex2MSTDataSource "Datenarten"
{
    primary key  ( char 			mst[64]	    				"Eindeutige MST-Kennung" ,
				   char 			version[20]					"Version" ) references datex2MST(mst, version ) on delete cascade;
	primary key  ( long				id                          "Source-ID" );	

   	column       ( char  			datakind[64]      			"Name der Datenart" );   
   	column       ( char  			valueCol[64]     			"Name der Werte-Spalte" );   
   	column       ( char  			valueColCharacteristic[64]  "Eigenschaft der Werte-Spalte" );   

	column       ( char  			idColumn[64]      			"Name ID-Spalte in datakind" );
	column       ( char  			validColumn[64]      		"Name einer dataKind-Spalte, deren Werte die Gültigkeit der Daten anzeigen" );
	column       ( char  			invalidValues[256]     		",-getrennte Werte der Spalte validColumn, die die Ungültigkeit anzeigen" );

   	column       ( char 			d2VehType[64]				"Datex-II: Fahrzeugart" ); 
   	column       ( char 			d2DataType[64]				"Datex-II: Art des Messwerts: MeasuredOrDerivedDataTypeEnum" ); 
	column       ( char  			d2BasicDataType[128]     	"Datex-II-Ableitung von basicData, deren Instanz einen Wert aufnimmt" );
	column       ( char  			d2ValueElement[128]      	"Name des Elements in basicDataType, das einen Wert aufnimmt" );
	column       ( char  			d2Value[64]      			"Name des Subelements von valueElement, das den Wert als Element-Text aufnimmt" );
	column       ( char  			d2ValueInner[64]      		"Name des Subelements von value, das den Wert als Element-Text aufnimmt" );
	column       ( char  			d2ValuePath[256]      		"Alternative zu d2ValueElement/d2Value/d2ValueInner: vollständiger Pfad, Elemente sind /-getrennt" );

	column		 ( char  			mappingType[64]             "Typ der Berechnung des Datex-II-Wertes");
	column		 ( double			factor						"Multiplikationsfaktor");
	column		 ( char				valueDataType[64]			"Datentyp der Werte");

   	column       ( long  			validInterval     			"Zeitschranke für max. Alter der Werte(Sekunden): ");   

	// foreign key ( mst, version ) references datex2MST(mst, version ) on delete cascade;
} keep OPERATING_DATA;


history attribute  datex2MSTDataSourceFilter "Daten-Filter"
{
    primary key  ( char 			mst[64]	    				"Eindeutige MST-Kennung",
				   char 			version[20]					"Version",
				   long				id							"Source-ID") references datex2MSTDataSource(mst, version, id ) on delete cascade;	

   	primary key  ( long  			period      				"Erfassungszyklus (Sekunden)" );   
   	column       ( char  			filterCol[64]     			"Name der Filter-Spalte" );  
   	column       ( char  			filterValues[256]     		",-getrennte Filter-Werte" );  
   	 
	// foreign key ( mst, version, id ) references datex2MSTDataSource(mst, version, id ) on delete cascade;
} keep OPERATING_DATA;


history attribute  datex2MSTDataSourceEnumMapping "Abbildung auf Enum-Literale"
{
    primary key  ( char 			mst[64]	    				"Eindeutige MST-Kennung",
	               char 			version[20]					"Version",
	               long				id							"Source-ID") references datex2MSTDataSource(mst, version, id ) on delete cascade;	

   	primary key  ( long  			value      					"Enum-Wert" );   
   	column       ( char  			name[64]     				"Enum-Literal" );  
   	 
	// foreign key ( mst, version, id ) references datex2MSTDataSource(mst, version, id ) on delete cascade;
} keep OPERATING_DATA;

history attribute datex2MSTItem "MeasurementSite-Item: entspricht einer <MeasurementSpecificCharacteristics> im Datex2-Datenmodell"
{
   primary key  ( char 	mst[64]	    		"Eindeutige MST-Kennung",
	              char	version[20]			"Version") references datex2MST(mst, version) on delete cascade;
   primary key  ( long  id					"id" );

   column		( long  index				"Index innerhalb des datex2MSTLocation-Parent-Objekts" );

   column       (char   name[64]        	"Name" );
   column       (char   carriageway[64]     "Fahrbahn: gemäß Datex2" );
   column       (char   lane[64]       	    "betroffene Spur" );
   column       (long   period          	"Erfassungszyklus (Sekunden)" ); 
   	
   column       (long   dkRef             	"Id aus datex2MSTDataSource" );		// 'references datex2MSTDataSource(id) on delete restrict;' mag der DDL-Compiler nicht 
   column       (long   dkId             	"Id des Objekts in der durch dkRef gegebenen Datenart" );
   column       (char   dkKey[40]           "String-Id des Objekts in der durch dkRef gegebenen Datenart (aus Kompatibilitätsgründen wird auch die ganzzahlige Variante (dkId) noch vorgehalten)" );
   column       (long   locId             	"Id aus datex2MSTlocation" );		// 'on delete cascade;'references datex2MSTLocation(id) mag der DDL-Compiler nicht 

   // foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;



history attribute datex2MSTRoadFilter    "Location-Filter: wird z.Zt. nicht verwendet (31.07.2014)"
{
    primary key  ( char 	mst[64]	        "Eindeutige MST-Kennung", 
				   char	version[20]			"Version") references datex2MST(mst, version) on delete cascade;
	primary key  (char  roadName[128]		"GeoDyn2 rod number (directed)" );
	primary key	 (long  fromKm	   			"Beginn Kilometrierung");		// -1: nicht definiert
	column		 (long  toKm	   			"Ende Kilometrierung");			// -1: nicht definiert

    // foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;

history attribute datex2MSTObjectFilter "Objekt-Filter"
{
    primary key  ( char  mst[64]	        "Eindeutige MST-Kennung", 
				   char	 version[20]		"Version") references datex2MST(mst, version) on delete cascade;
	primary key  ( char  referenceType[20]	"TLS, GEODYN, ..." );
	primary key  ( char  type[64]			"fuer TLS etwa Q, fuer GEODYN tr_detection_site" );
	primary key  ( char  id[32]				"Object-ID" );
	column		 ( char  name[128]	   		"Objekt-Name");			
	column		 ( bool  excluded			"true: Objekt wird nicht in die MST aufgenommen.");

    // foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;


history attribute datex2MSTObjectFilterSubObj "Sub-Objekte eines Filter-Objekts"
{
    primary key  ( char  mst[64]	        "Eindeutige MST-Kennung", 
				   char	 version[20]		"Version",
				   char  referenceType[20]	"TLS, GEODYN, ...",
				   char  type[64]			"TLS: etwa Q, GEODYN: etwa tr_detection_site",
				   char  id[32]			    "Object-ID" )references datex2MSTObjectFilter(mst, version, referenceType, type, id) on delete cascade;
	column		 ( char  subObjName[16]	   	"Objekt-Name");			
} keep OPERATING_DATA;


history class datex2MSTBuildState "Status der MST-Erstellung"
{
	primary key ( char 			mst[64]         "MST-Id"); // references datex2MST(mst) on delete restrict;
    primary key ( char			version[20]		"Version");
	column		( timestamp		buildStarted    "Startzeitpunkt der Erstellung");
	column		( build_State	state           "Status");
	column		( char		    result[255]     "Ergebnis");

    // foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;



// Geplante Erweiterung:
// 
//		 Einführung einer 'Kopf-Datenart' mit Beschreibung.
//       Bestehende Datenart in datex2MSTConfigDetail umbenennen
//       Kopf-Datenart heißt dann datex2MSTConfig

history class datex2MSTConfig  "Abbildung Verkehrsdatenmodell --> Datex2, gemäß http://www.heuboe.de/mst/config"
{
    primary key  (char 	d2Version[20]  		"Datex II Version");
	primary key  (long  count				"Nummerierung der Teile der XML-Definition der Abbildung" );
	column		 (char  defPart[2048]		"Teil der XML-Definition der Abbildung");		
} keep OPERATING_DATA;

}

module datex2MDP "MeasuredDataPublication (Datex 2) related  data model" 
{

domain objectState "Objekt-Status"
range {
1 "aktiviert",
2 "deaktiviert"
} as byte;


// Der MDP-Builder berücksichtigt bei der Erzeugung der Publikationen den
// Status der MeasurementSites: Daten von MeasurementSites mit Status 'ignoriert' 
// werden nicht publiziert. 
history class datex2MDPLocationState		 "Status der MeasurementSites aus datex2MSTLocation"
{
	primary key  ( char  geodynType[64]		"Geodyn-Datenart, etwa: tr_detection_site");
	primary key  ( long  id			"GeoDyn2-ID");  // references datex2MSTLocation::geoDynId
	column( objectState state					"Status"); 
} keep OPERATING_DATA;


// Der MDP-Builder berücksichtigt bei der Erzeugung der Publikationen den
// Status des Items (oft Spur/Schleife): Daten von Items mit Status 'ignoriert' 
// werden nicht publiziert. 
history class datex2MDPItemState		 "Status eines datex2MSTItem"
{
	primary key  ( long  id				"Item-ID");  // references datex2MSTItem::dkId
	column( objectState state				"Status|Versand an MDM"); 
} keep OPERATING_DATA;

}

#endif
