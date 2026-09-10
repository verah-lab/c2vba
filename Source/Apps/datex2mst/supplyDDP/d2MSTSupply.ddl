///////////////////////////////////////////////////////////////////////
// 
// (C) Copyright 1998-1999 Heusch/Boesefeldt GmbH.
// ALL RIGHTS RESERVED
//
// FILE:        d2MSTSupply.ddl
// AUTHOR:      Peter Schmitz
// COMMENT:     Data model for MeasurementSiteTable (Datex 2)
// DATE:	02.12.1999
// HISTORY ////////////////////////////////////////////////////////////////////
// DATE			WHO		WHAT
///////////////////////////////////////////////////////////////////////////////
#ifndef D2MST_DDL		// DDL-Guard
#define D2MST_DDL

////////////////////////////////////////////////////////////////////////////////
// Include files
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// MODULE:      d2MST
// USING:     
// COMMENT:     Data model for MeasurementSiteTable (Datex 2)
///////////////////////////////////////////////////////////////////////////////

#include "keep-defines.ddl"
#include "properties.ddl"


module d2MST "MeasurementSiteTable (Datex 2) data model" 
{

domain d2MSTdataType "Messwert-Arten"
range {
0 "humidityInformation",
1 "pollutionInformation",
2 "precipitationInformation",
3 "pressureInformation",
4 "radiationInformation",
5 "roadSurfaceConditionInformation",
6 "temperatureInformation",
7 "visibilityInformation",
8 "windInformation",
9 "individualVehicleMeasurements",
10 "trafficConcentration",
11 "trafficFlow",
12 "trafficHeadway",
13 "trafficSpeed",
14 "travelTimeInformation",
15 "unknownn"
} as byte;


domain inpDataUnit "Einheit (GUI-Eingabe)"
range {
0 "nicht definiert",
1 "km/h",
2 "Fzg/h",
3 "Fzg/I",
4 "Fzg/5Min",
5 "Fzg/km"
} as byte;


domain inpVehicleType "Fahrzeugtyp (GUI-Eingabe)"
range {
0 "KFZ",
1 "LKW",
2 "PKW",
3 "nicht definiert"
} as byte;

domain d2MSTvehicleType "Fahrzeugtyp"
range {
0 "anyVehicle",
1 "articulatedVehicle",
2 "bus",
3 "car",
4 "carOrLightVehicle",
5 "carWithCaravan",
6 "carWithTrailer",
7 "fourWheelDrive",
8 "goodsVehicle",
9 "heavyLorry",
10 "heavyVehicle",
11 "highSidedVehicle",
12 "lightVehicle",
13 "lorry",
14 "motorcycle",
15 "twoWheeledVehicle",
16 "van",
17 "vehicleWithCatalyticConverter",
18 "vehicleWithoutCatalyticConverter",
19 "vehicleWithCaravan",
20 "vehicleWithTrailer",
21 "withEvenNumberedRegistrationPlates",
22 "withOddNumberedRegistrationPlates",
23 "other",
24 "noVehicle"
} as byte;



domain buildState "Status der MST-Erstellung"
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



history class d2MST "MST-Kennungen"
{
	primary key (char 		mst[64]	    		"eindeutige MST-Kennung");
	column      (char 		version[20]	    	"Version");
	column		(char 		description[512]	"MST-Beschreibung");
	column		(bool       active				"Für nicht aktive MST-Kennungen wird keine MSt angelegt.");
} keep OPERATING_DATA;




history attribute d2MSTLocation
{
    primary key  (char 	mst[64]	    		"eindeutige MST-Kennung") references d2MST on delete restrict;
	primary key  (long  id );

	column		 (char  locName[256]	   	"Location-Name");		
	column       (char  equipment[64]   	"Typ der Erfassungshardware" );

   	column       (char  rdsLocTblRef[3] 	"RDS Location Table Reference" );
   	column       (char  rdsLocTblVer[6] 	"RDS Location Table Version" );

   	column       (long  rdsPrimLocCode     	"RDS Primary Location" );
   	column       (long  rdsSecLocCode       "RDS Secondary Location" );

   	column       (char  rdsDirection[1]   	"RDS Direction" );
	column       (long  rdsPrimLocDist    	"RDS Primary-Location-Distance (meter)" );
	column       (long  rdsSecLocDist   	"RDS Secondary-Location-Distance (meter)" );

   	column       (long  startCoordX         "Anfangspunkt: Ortsreferenzierung nach Gauss-Krüger, x-Koordinate" );   
   	column       (long  startCoordY         "Anfangspunkt: Ortsreferenzierung nach Gauss-Krüger, y-Koordinate" );   

   	column       (long  endCoordX           "Endpunkt: Ortsreferenzierung nach Gauss-Krüger, x-Koordinate" );   
   	column       (long  endCoordY           "Endpunkt: Ortsreferenzierung nach Gauss-Krüger, y-Koordinate" );   
} keep OPERATING_DATA;



history attribute  d2MSTDataSource
{
    primary key  ( char 			mst[64]	    		"eindeutige MST-Kennung") references d2MST(mst) on delete restrict;
	primary key  ( long				id );	

   	column       ( d2MSTdataType 	dataType 			"Art des Messwerts" ); 

   	column       ( d2MSTvehicleType	vehClass    		"Fahrzeugklasse" );
   	column       ( inpVehicleType	inpVehClass    		"Fahrzeugklasse" );
	column		 ( char				unit[16]			"Einheit des Messwertes");
   	column       ( inpDataUnit		inpUnit    			"Einheit des Messwertes");


   	column       ( char  			dataKind[64]      	"Name der Datenart" );   
	column       ( char  			idCol[64]       	"Name der ID-Spalte" );   
   	column       ( char  			valueCol[64]     	"Name der Werte-Spalte" );   
   	column       ( char  			validCol[64]        "Name der Gueltigkeits-Spalte" );   
   	column       ( char  			invalidVals[128]   	"Ungueltig-Werte" );   
   	column       ( long  			validInterval     	"Zeitschranke für max. Alter der Werte(Sekunden)");   
} keep OPERATING_DATA;



history attribute d2MSTItem
{
   primary key  ( char 	mst[64]	    		"eindeutige MST-Kennung") references d2MST(mst) on delete restrict;
   primary key  ( long  id					"id" );

   column		( long  index				"Index innerhalb des d2MSTLocation-Parent-Objekts" );

   column       (char  msName[64]        	"Name" );
   column       (char  msDirection[1]    	"betroffene Fahrtrichtung" );
   column       (char  msLanes[10]       	"betroffene Spuren" );
   column       (long  msPeriod          	"Erfassungszyklus (Minuten)" ); 
   	
   column       (long  dkRef             	"Id aus d2MSTDataSource" );		// 'references d2MSTDataSource(id) on delete restrict;' mag der DDL-Compiler nicht 
   column       (long  dkId             	"Id des Objekts in der durch dkRef gegebenen Datenart" );
   column       (long  locId             	"Id aus d2MSTlocation" );		// 'on delete cascade;'references d2MSTLocation(id) mag der DDL-Compiler nicht 
} keep OPERATING_DATA;


history attribute d2MSTFilterObject
{
    primary key  (char 	mst[64]	    		"eindeutige MST-Kennung") references d2MST on delete restrict;
	primary key  (char  locId[128]			"GeoDyn2-Permanent-ID, Querschnitt" );
	primary key	 (char  laneId[16]	   		"Kennung einer Spur");		// Ein leerer Wert wählt den geesamten Querschnitt		
} keep OPERATING_DATA;




history class d2MSTDef "MST-Definition"
{
	primary key (char 		mst[64]         "MST-Id");			// references d2MST(mst)
	primary key	(long 		version        	"Version");
	primary key	(long 		count          	"Zaehler");
	column		(char 		defintion[512]  "XML-Definition");
} keep OPERATING_DATA;


history class d2MSTBuildState "Status der MST-Erstellung"
{
	primary key ( char 			mst[64]         "MST-Id");
	column		( long 			version        	"Version");		
//	primary key ( long 			version        	"Version");		// references d2MSTDef(mst,version) on delete cascade;
	column		( timestamp		buildStarted    "Startzeitpunkt der Erstellung");
	column		( buildState	state           "Status");
	column		( char		    result[255]     "Ergebnis");
} keep OPERATING_DATA;


history class msSiteCoordinates
{
	primary key	( long 			id        		"Objekt-ID");		
	column		( long			coord_x			"X-Koordinate");
	column		( buildState	state           "Y-Koordinate");
} keep OPERATING_DATA;




}

#endif
