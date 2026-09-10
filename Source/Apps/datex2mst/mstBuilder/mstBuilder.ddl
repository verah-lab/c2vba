///////////////////////////////////////////////////////////////////////
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
#include "properties.ddl"


module datex2MST "MeasurementSiteTable (Datex 2) data model" 
{


// ps 05.07.2011
// Urspr¸nglicher Name ('buildState') kollidierte mit Domain-Namen
// in anderem Modul: deshalb umbenannt. Die vom DDL-Compiler
// auszuf¸hrenden ƒnderungen sollten kein Datenart-Recreate 
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

	column		(bool       active				"Aktive Version: Hˆchstens eine Version sollte aktiv sein");
} keep OPERATING_DATA;


history class datex2MSTDefinition "Definition einer MST auf der Grundlage eines MST-Mappings, gem‰ﬂ http://www.heuboe.de/mstDefinition"
{
    primary key  (char 	d2Version[20]  		"Datex II Version");		// references datex2Mapping		
	primary key  (char 	mst[64]	    		"Eindeutige MST-Kennung");	// references datex2MST
	primary key  (char 	version[20]			"Version");					// references datex2MST
	primary key  (long  count				"Nummerierung der Teile der XML-Beschreibung der Definition" );
	column		 (char  defPart[2048]	    "Teil der XML-Beschreibung der Definition");		
} keep OPERATING_DATA;


history attribute datex2MSTLocation
{
    primary key  (char 	mst[64]	    		"eindeutige MST-Kennung");
	primary key  (char 	version[20]			"Version");
	primary key  (long  id );

	column		 (char  d2Id[64]	   		"MeasurementSite-ID");		
	column		 (char  locName[256]	   	"Location-Name");		
	column       (char  equipment[64]   	"Typ der Erfassungshardware" );

   	column       (char  rdsLocTblRef[3] 	"RDS Location Table Reference" );
   	column       (char  rdsLocTblVer[6] 	"RDS Location Table Version" );

   	column       (long  rdsPrimLocCode     	"RDS Primary Location" );
   	column       (long  rdsSecLocCode       "RDS Secondary Location" );

   	column       (char  rdsDirection[1]   	"RDS Direction" );
	column       (long  rdsPrimLocDist    	"RDS Primary-Location-Distance (meter)" );
	column       (long  rdsSecLocDist   	"RDS Secondary-Location-Distance (meter)" );

   	column       (long  startCoordX         "Anfangspunkt: Ortsreferenzierung nach Gauss-Kr¸ger, x-Koordinate" );   
   	column       (long  startCoordY         "Anfangspunkt: Ortsreferenzierung nach Gauss-Kr¸ger, y-Koordinate" );   

   	column       (long  endCoordX           "Endpunkt: Ortsreferenzierung nach Gauss-Kr¸ger, x-Koordinate" );   
   	column       (long  endCoordY           "Endpunkt: Ortsreferenzierung nach Gauss-Kr¸ger, y-Koordinate" );   
	column		 (long  geoDynId			"GeoDyn2-ID");
	
	foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;


history attribute datex2MSTCoordinates "Koordinaten einer linearen MeasurementSite-Location (Polyline)"
{
    primary key  (char 	mst[64]	    		"Eindeutige MST-Kennung");
	primary key  (char 	version[20]	        "Version");
	primary key  (long  id                  "Location-ID");                      

	primary key  ( long index				"Index des Punktes in der Polyline");
	column       ( double x					"x-Koordinate");
	column       ( double y                 "y-Koordinate");

	foreign key ( mst, version, id ) references datex2MSTLocation(mst, version, id) on delete cascade;
} keep OPERATING_DATA;


history class datex2MSTD2Location "Datex2-Location-Beschreibung"
{
    primary key  (char 	mst[64]	    		"Eindeutige MST-Kennung");
	primary key  (char 	version[20]	        "Version");
	primary key  (long  id                  "Location-ID");                      

	primary key  (long  count				"Nummerierung der Teile der XML-Beschreibung der Location" );
	column		 (char  defPart[2048]	    "Teil der XML-Beschreibung der Location");		
} keep OPERATING_DATA;


history attribute  datex2MSTDataSource
{
    primary key  ( char 			mst[64]	    				"eindeutige MST-Kennung");
	primary key  ( char 			version[20]					"Version");
	primary key  ( long				id );	

   	column       ( char  			datakind[64]      			"Name der Datenart" );   
   	column       ( char  			valueCol[64]     			"Name der Werte-Spalte" );   
   	column       ( char  			valueColCharacteristic[64]  "Eigenschaft der Werte-Spalte" );   

	column       ( char  			idColumn[64]      			"Name ID-Spalte in datakind" );
	column       ( char  			validColumn[64]      		"Name einer dataKind-Spalte, deren Werte die G¸ltigkeit der Daten anzeigen" );
	column       ( char  			invalidValues[256]     		",-getrennte Werte der Spalte validColumn, die die Ung¸ltigkeit anzeigen" );

   	column       ( char 			d2VehType[64]				"Datex-II: Fahrzeugart" ); 
   	column       ( char 			d2DataType[64]				"Datex-II: Art des Messwerts: MeasuredOrDerivedDataTypeEnum" ); 
	column       ( char  			d2BasicDataType[128]     	"Datex-II-Ableitung von basicData, deren Instanz einen Wert aufnimmt" );
	column       ( char  			d2ValueElement[128]      	"Name des Elements in basicDataType, das einen Wert aufnimmt" );
	column       ( char  			d2Value[64]      			"Name des Subelements von valueElement, das den Wert als Element-Text aufnimmt" );
	column       ( char  			d2ValueInner[64]      		"Name des Subelements von value, das den Wert als Element-Text aufnimmt" );
	column       ( char  			d2ValuePath[256]      		"Alternative zu d2ValueElement/d2Value/d2ValueInner: vollst‰ndiger Pfad, Elemente sind /-getrennt" );

	column		 ( char  			mappingType[64]             "Typ der Berechnung des Datex-II-Wertes");
	column		 ( double			factor						"Multiplikationsfaktor");
	column		 ( char				valueDataType[64]			"Datentyp der Werte");

   	column       ( long  			validInterval     			"Zeitschranke f¸r max. Alter der Werte(Sekunden): ");   

	foreign key ( mst, version ) references datex2MST(mst, version ) on delete cascade;
} keep OPERATING_DATA;



history attribute datex2MSTItem
{
   primary key  ( char 	mst[64]	    		"eindeutige MST-Kennung");
   primary key  ( char	version[20]			"Version");
   primary key  ( long  id					"id" );

   column		( long  index				"Index innerhalb des datex2MSTLocation-Parent-Objekts" );

   column       (char   name[64]        	"Name" );
   column       (char   carriageway[64]     "Fahrbahn: gem‰ﬂ Datex2" );
   column       (char   lane[64]       	    "betroffene Spur" );
   column       (long   period          	"Erfassungszyklus (Sekunden)" ); 
   	
   column       (long   dkRef             	"Id aus datex2MSTDataSource" );		// 'references datex2MSTDataSource(id) on delete restrict;' mag der DDL-Compiler nicht 
   column       (long   dkId             	"Id des Objekts in der durch dkRef gegebenen Datenart" );
   column       (long   locId             	"Id aus datex2MSTlocation" );		// 'on delete cascade;'references datex2MSTLocation(id) mag der DDL-Compiler nicht 

   foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;


history class datex2MSTObjectFilter
{
    primary key  (char 	mst[64]	    		"eindeutige MST-Kennung"); // references datex2MST on delete restrict;
	primary key  (char  locId[128]			"GeoDyn2-Permanent-ID, Querschnitt" );
	primary key	 (char  laneId[16]	   		"Kennung einer Spur");		// Der Wert "-" w‰hlt den gesamten Querschnitt, stellt keine Spur-ID dar		
} keep OPERATING_DATA;


history class datex2MSTRoadFilter
{
    primary key  (char 	mst[64]	    		"eindeutige MST-Kennung"); // references datex2MST on delete restrict;
	primary key  (char  roadName[128]		"GeoDyn2 rod number (directed)" );
	primary key	 (long  fromKm	   			"Beginn Kilometrierung");		// -1: nicht definiert
	column		 (long  toKm	   			"Ende Kilometrierung");			// -1: nicht definiert
} keep OPERATING_DATA;



history class datex2MSTBuildState "Status der MST-Erstellung"
{
	primary key ( char 			mst[64]         "MST-Id"); // references datex2MST(mst) on delete restrict;
    primary key ( char			version[20]		"Version");
	column		( timestamp		buildStarted    "Startzeitpunkt der Erstellung");
	column		( build_State	state           "Status");
	column		( char		    result[255]     "Ergebnis");

    foreign key ( mst, version ) references datex2MST(mst, version) on delete cascade;
} keep OPERATING_DATA;


history class datex2Mapping		 "Abbildung Verkehrsdatenmodell --> Datex2, gem‰ﬂ http://www.heuboe.de/mstConfig"
{
    primary key  (char 	d2Version[20]  		"Datex II Version");
	primary key  (long  count				"Nummerierung der Teile der XML-Definition der Abbildung" );
	column		 (char  mapDefPart[2048]	"Teil der XML-Definition der Abbildung");		
} keep OPERATING_DATA;

history class datex2Template
{
    primary key  (char 	d2Version[20]  					"Datex II Version");
    primary key  (char 	dk[64]  						"Geodyn2 Datenart");
    primary key  (char 	valColumn[64]  					"Geodyn2 Datenart-Spalte");
    primary key  (char 	valColumnCharacteristic[64]  	"Geodyn2 Datenart-Spalte");
	primary key  (long  count							"Nummerierung der Teile der XML-Definition der Abbildung" );
	column		 (char  mapDefPart[2048]				"Teil der XML-Definition der Abbildung");		
} keep OPERATING_DATA;




}

#endif
