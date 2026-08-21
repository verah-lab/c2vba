//////////////////////////////////////////////////
// allgemeine Formatdefinitionen fuer Texte
//////////////////////////////////////////////////

TEXTDEF         textHersteller Hersteller
TEXTVAL     0   "unbekannt"
TEXTVAL     1   "AEG"
TEXTVAL     2   "ANDI"
TEXTVAL     3   "Bosch Telecom"
TEXTVAL     4   "ave GmbH"
TEXTVAL     5   "Dambach"
TEXTVAL     6   "Heusch Boesefeldt"
TEXTVAL     7   "DST"
TEXTVAL     8   "Pintsch-Bamag"
TEXTVAL     9   "Prodata GmbH"
TEXTVAL     10  "Siemens AG"
TEXTVAL     11  "Socal-Light / TVB"
TEXTVAL     12  "Dasa"
TEXTVAL     13  "Weiss-Electronic GmbH"
TEXTVAL     14  "Segor GmbH"
TEXTVAL     15  "feig electronic"
TEXTVAL     16  "Erwin Sick GmbH"
TEXTVAL     17  "MicKS MSR GmbH"
TEXTVAL     18  "Weiss-Systeme"
TEXTVAL     19  "Blau Industrie-Elektronik"
TEXTVAL     20  "QSG"
TEXTVAL     21  "Boschung"
TEXTVAL     22  "Claus Schrick"
TEXTVAL     23  "Stuehrenberg GmbH"
TEXTVAL     24  "Zimmermann GmbH"
TEXTVAL     25  "Sauber + Gisin"
TEXTVAL     26  "Pietzsch"
TEXTVAL     27  "rec digitale Processtechnik"
TEXTVAL     28  "Steria Informatic AG"
TEXTVAL     29  "Robot Foto und Electronic GmbH"
TEXTVAL 30-255  "nicht definiert"


TEXTDEF         textKanalsteuerung Kanalsteuerung
TEXTVAL     0   aktiv
TEXTVAL     1   passiv


TEXTDEF         textDE-Fehler DE-Fehler
TEXTVAL     0   "ok"
TEXTVAL     1   "EAK-Fehler"
TEXTVAL     2   "SM-Fehler"
TEXTVAL     3   "verboten oder DUE-Fehler"


TEXTDEF         textLandeskennung Landeskennung
TEXTVAL     1   "Schleswig-Holstein"
TEXTVAL     2   "Hamburg"
TEXTVAL     3   "Niedersachsen"
TEXTVAL     4   "Bremen"
TEXTVAL     5   "Nordrhein-Westfalen"
TEXTVAL     6   "Hessen"
TEXTVAL     7   "Rheinland-Pfalz"
TEXTVAL     8   "Baden-Wuerttemberg"
TEXTVAL     9   "Bayern"
TEXTVAL     10  "Saarland"
TEXTVAL     11  "Berlin"
TEXTVAL     12  "Brandenburg"
TEXTVAL     13  "Mecklenburg-Vorpommern"
TEXTVAL     14  "Sachsen"
TEXTVAL     15  "Sachsen-Anhalt"
TEXTVAL     16  "Thueringen"

TEXTDEF         textStrassenart Strassenart
TEXTVAL     1   "Bundesautobahn"
TEXTVAL     2   "Bundesstrasse"
TEXTVAL     3   "Landstrasse"


//////////////////////////////////////////////////
// allgemeine Formatdefinitionen fuer Bitfelder
//////////////////////////////////////////////////

BITFIELDDEF 1   bitDE-Fehler        "Fehlercode"
SUBTEXT     3   textDE-Fehler 
BIT         2   "Projektierungsfehler"
BIT         3   "passiv"
BIT         4-7 "nach TLS 2000 verboten"

BITFIELDDEF 1   bitKanalsteuerung   "Steuerbyte"
SUBTEXT     1   textKanalsteuerung
BIT       1-3   "verboten"
SUBVALUE 0xf0   4 
    VALUE OBYTE "Herstellercode"

BITFIELDDEF 1   bitFahrtrichtung    "Fahrtrichtung"
BIT         0   "Nord bzw. Ost"
BIT         1   "Sued bzw. West"
BIT         2-7 "reserviert"

//////////////////////////////////////////////////
// allgemeine Formatdefinitionen fuer Subtypen
//////////////////////////////////////////////////

SUBTYPEDEF      subDE-Fehler        "DE-Fehler"
BITFIELD        bitDE-Fehler        
TEXT			textHersteller

SUBTYPEDEF subGeoKenn               "geogr. Kenndaten"
TEXT        textLandeskennung
TEXT        textStrassenart
VALUE BCD2  "Strassennummer"
VALUE BCD32 "Kilometrierung"        "km"
BITFIELD    bitFahrtrichtung
VALUE BYTE  "Reservebyte"

//////////////////////////////////////////////////
// FG 254
//////////////////////////////////////////////////


//////////////////////////////////////////////////
// Textdefinitionen fuer FG 254

TEXTDEF     textNeqQuittFg254                   "Ursache"
TEXTVAL 0 	"sonstige Fehlerursache"
TEXTVAL 1   "unbekannte oder nicht auswertbare ID"
TEXTVAL 2 	"unbekannter oder nicht auswertbarer Typ"
TEXTVAL 3		"keine Rueckmeldung moeglich, da Kanal gestoert"
TEXTVAL 4		"frei bzw. unbenutzt"
TEXTVAL 5		"Kommunikationsstatus enthaelt ungueltigen Wert"
TEXTVAL 6		"DE-Zuordnung enthaelt mehr EA-Kanaele als vorh."
TEXTVAL 7		"DE-Zuordnung enthaelt unzul. DE-Sammeladresse"
TEXTVAL 8		"DE-Zuordnung enthaelt zu EA-Kanal falsche FG"
TEXTVAL 64 	"Nicht vorhandene Funktionsgruppe"
TEXTVAL 65 	"Nicht vorhandene DE"
TEXTVAL 66	"Richtungsbit im Abruftelegramm war 1" 
TEXTVAL 67  "Jobnummer im Abruftelegramm war 0" 
TEXTVAL 68	"Anz. ET nicht mit OSI7-Laenge vereinbar" 
TEXTVAL 69	"Laenge ET ungleich Laengenbyte"
TEXTVAL 70	"Anz. DE-Bl nicht mit OSI7-Laenge vereinbar"
TEXTVAL 71  "Laenge DE-Bl. nicht m. OSI7-Laenge vereinbar"
TEXTVAL 72  "Anz. DE-Bl. nicht m. Laengenbyte vereinbar"
TEXTVAL 73  "Laenge DE-Bl. nicht m. Laengenbyte vereinbar"
TEXTVAL 74  "Empfaenger oder Uebertragungsweg gestoert"
TEXTVAL 75	"Anzahl DE-Bloecke ist unzulaessig"
TEXTVAL 76	"Falsche Knotennummer"
TEXTVAL 77	"Fehlerhafte OSI-3 Routinginformation"
TEXTVAL 78	"Anzahl Einzeltelegramme ist unzulaessig"


//////////////////////////////////////////////////
// Bitfield-Definitionen fuer FG 254

BITFIELDDEF 1   bitFG254Fehlerbyte1         "Fehlerbyte 1"
BIT         0   "Teilstoerung" 
BIT   1-3,6-7   "Fehler: Reserve-Bit gesetzt"
BIT         4   "Lokalbus ausgefallen"
BIT         5   "RNR an Lokalbus"

BITFIELDDEF 1   bitFG254Fehlerbyte2         "Fehlerbyte 2"
BIT         0   "Fehler DE-Zuordnung"
BIT       1-7   "Fehler: Reserve-Bit gesetzt"

BITFIELDDEF 1   bitFG254Fehlerbyte3         "Fehlerbyte 3"
BIT       0-7   "Fehler: Reserve-Bit gesetzt"

BITFIELDDEF 1   bitFG254Fehlerbyte4        "Fehlerbyte 4"
BIT       0-7   "Fehler: Reserve-Bit gesetzt"

BITFIELDDEF 1   bitKommunikationsstatus     "Status"
BIT         0   "gestoert"				    "ok"
BIT         1   "ungueltig"
BIT       2-3   "Fehler:  freies Bit gesetzt"
BIT       4-7   "Fehler: Reserve-Bit gesetzt"
     
BITFIELDDEF 1   bitGrundFuerReset           "Reset Grund"
BIT         0   "Hardware-Reset SM"
BIT         4   "Hardware-Reset EAK"
BIT   1-3,5-7   "reserviert"


//////////////////////////////////////////////////
// Subtyp-Definitionen fuer FG 254

SUBTYPEDEF      subDE-Zuordnung     "DE-Zuordnung"
VALUE BYTE      "phys. EA-Kanal"
VALUE BYTE      "FG" 
VALUE BYTE      "DE" 

SUBTYPEDEF      subErgDeFehler254   "Erg. DE-Fehler"
TEXT            textHersteller
VALUE BYTE      "Anzahl TLS-Fehlerbytes" 
BITFIELD        bitFG254Fehlerbyte1
BITFIELD        bitFG254Fehlerbyte2
BITFIELD        bitFG254Fehlerbyte3
BITFIELD        bitFG254Fehlerbyte4
VALARRAY VAR 
    VALUE BYTE  "Hersteller-Fehlerbyte"

SUBTYPEDEF      subKonfigurationstabelle    "Eintraege"
VALUE BYTE      "E/A-Konzentrator"
VALUE BYTE      "phys. EA-Kanal"
VALUE BYTE      "FG"
VALUE BYTE      "DE"


//////////////////////////////////////////////////
// DeBlock-Definitionen fuer FG 254

DEBLOCK 254 17,129      1   "DE-Fehler"
SUBTYPE                 subDE-Fehler 

DEBLOCK 254 1,17,129    14  "Erg. DE-Fehlermeldung"
SUBTYPE                 subErgDeFehler254

DEBLOCK 254 130         16  "negative Quittung"
TEXT                    textNeqQuittFg254 
TEXT                    textHersteller

DEBLOCK 254 130         17  "Initialisierungsmeldung"
SPECIAL NOTHING             ""

DEBLOCK 254 2           18  "Zeitsynchronisation"
TIME TYP18

DEBLOCK 254 130         19  "Kommunikationsstatus"
BITFIELD                bitKommunikationsstatus

DEBLOCK 254 131         28  "positive Quittung"
SPECIAL NOTHING             ""

DEBLOCK 254 129-133     30  "Zeitstempel"
TIME TYP30

DEBLOCK 254 3,19,131    32  "Statische Geraetekenndaten"
TEXT                    textHersteller
VALARRAY REST 
    VALUE BYTE              "Geraetekennbyte"

DEBLOCK 254 3,19,131    33  "DE-Zuordnung"
VALARRAY VAR 
    SUBTYPE             subDE-Zuordnung

DEBLOCK 254 3,19,131    34  "Konfigurationstabelle"
VALARRAY VAR 
    SUBTYPE             subKonfigurationstabelle  

DEBLOCK 254 3,19,131    35  "OSI3-Routingfeld"
SPECIAL OSI3ROUTING     "OSI3-Routing"

DEBLOCK 254 3,19,131    36  "geographische Kenndaten"
SUBTYPE                 subGeoKenn

DEBLOCK 254 3,19,131    37  "Knotennummer"
SPECIAL KNOTENNUMMER    "Knotennummer"

DEBLOCK 254 2           38  "Reset"
BITFIELD                bitGrundFuerReset


//////////////////////////////////////////////////
// FG 1
//////////////////////////////////////////////////

//////////////////////////////////////////////////
// Textdefinitionen fuer FG 1

TEXTDEF         textNeqQuittFg1             "Ursache"
TEXTVAL     0   "sonstige Fehlerursache"
TEXTVAL     1   "unbekannte oder nicht auswertbare Id"
TEXTVAL     2   "unbekannter oder nicht auswertbarer Typ"
TEXTVAL     3   "Version nicht verfuegbar"
TEXTVAL  4-11   "reserviert"
TEXTVAL    12   "Pufferunhalt nicht verfuegbar"
TEXTVAL 13-127  "reserviert"
TEXTVAL 128-255 "frei"


//////////////////////////////////////////////////
// Bitfield-Definitionen fuer FG 1

BITFIELDDEF 1   bitFG1Fehlerbyte2           "Fehlerbyte 2"
BIT         0   "Subbus ausgefallen"
BIT         1   "Subbus RNR"
BIT         2   "Detektor gestoert"
BIT         3   "Auswertung gestoert"
BIT         4   "Schleife 1 defekt"
BIT         5   "Schleife 2 defekt"
BIT         6   "Abgleich Schleife 1"
BIT         7   "Abgleich Schleife 2"

BITFIELDDEF 1 bitMittelwertbildung          "Mittelwertbildung"
SUBVALUE 0x7f  0 
    VALUE BYTE  "Startwert"       "km/h"
BIT 7           "arithmetisch"    "gleitend"

BITFIELDDEF 1 bitStunde           "Stunde"
SUBVALUE 0x7f  0 
    VALUE BYTE  "Stunde"         
BIT 7           "Sommerzeit"      "Normalzeit"

BITFIELDDEF 1 bitIntervallLang    "Intervalldauer"
SUBVALUE 0x7f  0 
    VALUE BYTE  "Intervalldauer"  ""         
BIT 7           "in Stunden"      "verboten"

BITFIELDDEF 1 bitKfzEineldatenStatus    "Status"
BIT 0           "Datensatz unvollstaendig"  "Datensatz vollstaendig"
BIT 1-7         "Datensatz unvollstaendig"


//////////////////////////////////////////////////
// Subtyp-Definitionen fuer FG 1
  
SUBTYPEDEF      subErgDeFehler1     "Erg. DE-Fehler"
TEXT            textHersteller
VALUE BYTE      "Anzahl TLS-Fehlerbytes" 
BITFIELD        bitFG254Fehlerbyte1
BITFIELD        bitFG1Fehlerbyte2
BITFIELD        bitFG254Fehlerbyte3
BITFIELD        bitFG254Fehlerbyte4
VALARRAY VAR 
    VALUE BYTE  "Hersteller-Fehlerbyte"

   
//////////////////////////////////////////////////
// DeBlock-Definitionen fuer FG 1
    
DEBLOCK 1   17,129      1   "DE-Fehler"
SUBTYPE                 subDE-Fehler

DEBLOCK 1   1,17,129    14  "Erg. DE-Fehlermeldung"
SUBTYPE                 subErgDeFehler1

DEBLOCK 1   130         16  "negative Quittung"
TEXT                    textNeqQuittFg1 
TEXT                    textHersteller

DEBLOCK 1   2           20  "Abruf Puffer"
VALUE   BYTE                "Jahr"
VALUE   BYTE                "Monat"
VALUE   BYTE                "Tag"
BITFIELD                bitStunde
VALUE   BYTE                "Anzahl Datensaetze"

DEBLOCK 1 2,18,130      29  "Kanalsteuerung"
BITFIELD                bitKanalsteuerung

DEBLOCK 1 129-133       30  "Zeitspempel" 				
TIME TYP30 

DEBLOCK 1 3,19,131      32  "Betriebsparameter"
VALUE   BYTEF	              "Datenversion Kurz"   
SPECIAL IVALDAUER	          "Intervalldauer Kurz" 
VALUE	BYTEF	                "Datenversion Lang"   
BITFIELD 	              bitIntervallLang
VALUE	FLOAT256              "alpha1"              
VALUE	FLOAT256              "alpha2"							 
SPECIAL LENGREN		          "Laengengrenzwert"		 
BITFIELD 	              bitMittelwertbildung

DEBLOCK 1 3,19,131      34  "SVE-Betriebsparameter"
VALUE   BYTEF               "Datenversion"
VALUE   BYTE                "Intervalldauer"    "mal 15 Sekunden"

DEBLOCK 1 3,19,131      36  "geographische Kenndaten"
SUBTYPE                 subGeoKenn

DEBLOCK 1 3,19,131      37  "Geschwindigkeitsklassen kurz"
VALUE   BYTE                "Fahrzeugklassen-Code"
VALARRAY VAR
    VALUE BYTE              "Grenze"            "km/h"
    
DEBLOCK 1 3,19,131      38  "Geschwindigkeitsklassen lang"
VALUE   BYTE                "Fahrzeugklassen-Code"
VALARRAY VAR
    VALUE BYTE              "Grenze"            "km/h"

DEBLOCK 1 132           48  "Zeitspempel" 				
TIME TYP48 

DEBLOCK 1 20,132        49  "LVE-Kurzzeit Version 0"
VALUE BYTEF             qKfz							 
VALUE BYTEF             qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"

DEBLOCK 1 20,132        113 "LVE-Kurzzeit Version 0 16Bit"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"

DEBLOCK 1 20,132        50  "LVE-Kurzzeit Version 1"
VALUE BYTEF             qKfz							 
VALUE BYTEF             qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE FLOAT10F          Nettozeitluecke         "s"

DEBLOCK 1 20,132        114 "LVE-Kurzzeit Version 1 16Bit"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE FLOAT10F          Nettozeitluecke         "s"

DEBLOCK 1 20,132        51  "LVE-Kurzzeit Version 2"
VALUE BYTEF             qKfz							 
VALUE BYTEF             qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE BYTEF             Belegung                "%"

DEBLOCK 1 20,132        115 "LVE-Kurzzeit Version 2 16Bit"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE BYTEF             Belegung                "%"

DEBLOCK 1 20,132        52  "LVE-Kurzzeit Version 3"
VALUE BYTEF             qKfz							 
VALUE BYTEF             qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE FLOAT10F          Nettozeitluecke         "s"
VALUE BYTEF             Belegung                "%"
VALUE BYTEF             Standardabweichung      "km/h"
VALUE BYTEF             v-expo                  "km/h"

DEBLOCK 1 20,132        116 "LVE-Kurzzeit Version 3 16Bit"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE FLOAT10F          Nettozeitluecke         "s"
VALUE BYTEF             Belegung                "%"
VALUE BYTEF             Standardabweichung      "km/h"
VALUE BYTEF             v-expo                  "km/h"

DEBLOCK 1 20,132        53  "LVE-Kurzzeit Version 4"
VALUE BYTEF             qKfz							 
VALUE BYTEF             qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw					          "km/h"
VALUE FLOAT10F          Nettozeitluecke         "s"
VALUE BYTEF             Belegung                "%"
VALUE BYTEF             Standardabweichung      "km/h"
VALUE BYTEF             v-expo                  "km/h"
VALARRAY VAR
    VALUE BYTE          "v-Kl. Pkw"
VALARRAY VAR
    VALUE BYTE          "v-Kl. Lkw"

DEBLOCK 1 20,132        117 "LVE-Kurzzeit Version 4 16Bit"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF		          vPkw					          "km/h"					 
VALUE BYTEF		          vLkw				          	"km/h"
VALUE FLOAT10F          Nettozeitluecke         "s"
VALUE BYTEF             Belegung                "%"
VALUE BYTEF             Standardabweichung      "km/h"
VALUE BYTEF             v-expo                  "km/h"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Pkw"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Lkw"

DEBLOCK 1 20,132        63  "Kfz-Einzeldaten"
BITFIELD                bitKfzEineldatenStatus
VALUE BYTE              Fahrzeugklassencode
VALUE USHORTLF          vKfz                    "km/h"
VALUE USHORTLF          Belegtzeit              "ms"
VALUE FLOAT100FL2       Nettozeitluecke         "s"
VALUE BYTEF             Fahrzeuglaenge          "dm"

DEBLOCK 1 20,132,164    64  "Zeitspempel" 				
TIME TYP64

DEBLOCK 1 20,132,164    65  "LVE-Langzeit Version 10"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 

DEBLOCK 1 20,132,164    66  "LVE-Langzeit Version 11"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF             vKfz                    "km/h"

DEBLOCK 1 20,132,164    67  "LVE-Langzeit Version 12"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF             vPkw                    "km/h"
VALUE BYTEF             vLkw                    "km/h"
VALUE BYTEF             sPkw                    "km/h"
VALUE BYTEF             sLkw                    "km/h"

DEBLOCK 1 20,132,164    68  "LVE-Langzeit Version 13"
VALUE USHORTLF          qKfz							 
VALUE USHORTLF          qLkw							 
VALUE BYTEF             vPkw                    "km/h"
VALUE BYTEF             vLkw                    "km/h"
VALUE BYTEF             sPkw                    "km/h"
VALUE BYTEF             sLkw                    "km/h"
VALUE BYTEF             v85-Pkw                 "km/h"

DEBLOCK 1 20,132,164    69  "LVE-Langzeit Version 14"
VALUE USHORTLF          "q PkwGruppe"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Bus"							 
VALUE USHORTLF          "q NichtKlassifizierbar"							 

DEBLOCK 1 20,132,164    70  "LVE-Langzeit Version 15"
VALUE USHORTLF          "q PkwGruppe"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Bus"							 
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"

DEBLOCK 1 20,132,164    71  "LVE-Langzeit Version 16"
VALUE USHORTLF          "q PkwGruppe"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Bus"							 
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"
VALUE BYTEF             "v85,Pkw"               "km/h"

DEBLOCK 1 20,132,164    72  "LVE-Langzeit Version 17"
VALUE USHORTLF          "q PkwGruppe"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Bus"							 
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"
VALUE BYTEF             "v85,Pkw"               "km/h"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Pkw"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Lkw"

DEBLOCK 1 20,132,164    73  "LVE-Langzeit Version 18"
VALUE USHORTLF          "q PkwGruppe"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Bus"							 
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"
VALUE BYTEF             "v85,Pkw"               "km/h"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. PkwGruppe"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. PkwAnhaenger"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Lkw"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. LkwAnhaenger"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Bus"

DEBLOCK 1 20,132,164    74  "LVE-Langzeit Version 19"
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE USHORTLF          "q Motorraeder"							 
VALUE USHORTLF          "q Pkw"							 
VALUE USHORTLF          "q Lieferwagen"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Sattel-Kfz"							 
VALUE USHORTLF          "q Bus"							 

DEBLOCK 1 20,132,164    75  "LVE-Langzeit Version 20"
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE USHORTLF          "q Motorraeder"							 
VALUE USHORTLF          "q Pkw"							 
VALUE USHORTLF          "q Lieferwagen"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Sattel-Kfz"							 
VALUE USHORTLF          "q Bus"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"

DEBLOCK 1 20,132,164    76  "LVE-Langzeit Version 21"
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE USHORTLF          "q Motorraeder"							 
VALUE USHORTLF          "q Pkw"							 
VALUE USHORTLF          "q Lieferwagen"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Sattel-Kfz"							 
VALUE USHORTLF          "q Bus"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"
VALUE BYTEF             "v85,Pkw"               "km/h"

DEBLOCK 1 20,132,164    77  "LVE-Langzeit Version 22"
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE USHORTLF          "q Motorraeder"							 
VALUE USHORTLF          "q Pkw"							 
VALUE USHORTLF          "q Lieferwagen"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Sattel-Kfz"							 
VALUE USHORTLF          "q Bus"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"
VALUE BYTEF             "v85,Pkw"               "km/h"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Pkw"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Lkw"

DEBLOCK 1 20,132,164    78  "LVE-Langzeit Version 23"
VALUE USHORTLF          "q NichtKlassifizierbar"							 
VALUE USHORTLF          "q Motorraeder"							 
VALUE USHORTLF          "q Pkw"							 
VALUE USHORTLF          "q Lieferwagen"							 
VALUE USHORTLF          "q PkwAnhaenger"							 
VALUE USHORTLF          "q Lkw"							 
VALUE USHORTLF          "q LkwAnhaenger"							 
VALUE USHORTLF          "q Sattel-Kfz"							 
VALUE USHORTLF          "q Bus"							 
VALUE BYTEF             "vPkw"                  "km/h"
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "sPkw"                  "km/h"
VALUE BYTEF             "sLkw"                  "km/h"
VALUE BYTEF             "v85,Pkw"               "km/h"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Motorraeder"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Pkw"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Lieferwagen"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. PkwAnhaenger"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Lkw"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. LkwAnhaenger"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Sattel-Kfz"
VALARRAY VAR
    VALUE USHORTL       "v-Kl. Busse"

DEBLOCK 1 20,132        96  "SVE-ergebnismeldung Version 0"
VALUE USHORTLF          "kKfz"                  "Fz/km"							 
VALUE BYTEF             "vKfz"                  "km/h"

DEBLOCK 1 20,132        97  "SVE-ergebnismeldung Version 0"
VALUE USHORTLF          "kKfz"                  "Fz/km"							 
VALUE BYTEF             "vKfz"                  "km/h"
VALUE USHORTLF          "kPkw"                  "Fz/km"							 
VALUE USHORTLF          "kLkw"                  "Fz/km"							 
VALUE BYTEF             "vLkw"                  "km/h"
VALUE BYTEF             "vPkw"                  "km/h"


//////////////////////////////////////////////////
// FG 2
//////////////////////////////////////////////////

//////////////////////////////////////////////////
// Textdefinitionen fuer FG 2

TEXTDEF         textNeqQuittFg2             "Ursache"
TEXTVAL     0   "sonstige Fehlerursache"
TEXTVAL     1   "unbekannte oder nicht auswertbare Id"
TEXTVAL     2   "unbekannter oder nicht auswertbarer Typ"
TEXTVAL     3   "Version nicht verfuegbar"
TEXTVAL  4-11   "reserviert"
TEXTVAL    12   "Pufferunhalt nicht verfuegbar"
TEXTVAL 13-14   "reserviert"
TEXTVAL    15   "Kanal passiviert"
TEXTVAL 16-127  "reserviert"
TEXTVAL 128-255 "frei"

TEXTDEF         textFahrzeugtyp             "Fahrzeugtyp"
TEXTVAL   0     "unbekannt"
TEXTVAL   1     "Pkw"
TEXTVAL   2,3   "Pkw mit Anhaenger"
TEXTVAL   4     "Lieferwagen"
TEXTVAL   5,6   "Lieferwagen mit Anhaenger"
TEXTVAL   8-12  "Lkw"
TEXTVAL   32-37,40-45,48-53 "Lkw mit Anhaenger"
TEXTVAL   56-61,64-69       "Lkw mit Anhaenger"
TEXTVAL   96-99,104-107     "Sattel-Kfz"
TEXTVAL   120-125           "Bus"
TEXTVAL   7,38,39,46,47,54,55,62,63       "reserviert"
TEXTVAL   70-95,100-103,108-119,126-255   "reserviert"

TEXTDEF         textAchsArt               "Achsart"
TEXTVAL   0     "undefiniert"
TEXTVAL   4-31  "undefiniert"
TEXTVAL   1     "Einzelachse"
TEXTVAL   2     "Doppelachse"
TEXTVAL   3     "Dreifachachse"

TEXTDEF         textSpeicherungGeraet     "Speicherung im Geraet"
TEXTVAL   0     "keine Speicherung"
TEXTVAL   1     "Speicherung incl. PKW"
TEXTVAL   2     "Speicherung excl. PKW"
TEXTVAL 3-255   "reserviert"

//////////////////////////////////////////////////
// Bitfield-Definitionen fuer FG 2

BITFIELDDEF 1   bitFG2Fehlerbyte2         "Fehlerbyte 2"
BIT         0   "Subbus ausgefallen"
BIT         1   "Detektor gestoert"
BIT         2   "Achslastsensor gestoert"
BIT         3   "Detektor Abgleich"
BIT         4-7 "reserviert"

BITFIELDDEF 1 bitIntervallLang2           "Intervalldauer"
SUBVALUE 0x7f  0 
    VALUE BYTE  "Intervalldauer"  ""         
BIT 7           "in Stunden"              "in Viertelstunden"

BITFIELDDEF 1   bitUeberladung            "Ueberladung"
BIT         0   "ist ueberladen"          "ist nicht ueberladen"
BIT         1-7 "reserviert"

BITFIELDDEF 1   bitAchsArt                "Achsart"
SUBTEXT   31    textAchsArt
BIT       5,6   "reserviert"
BIT       7     "Ueberschreitung"         "keine Ueberschreitung"

//////////////////////////////////////////////////
// Subtyp-Definitionen fuer FG 2
  
SUBTYPEDEF      subErgDeFehler2     "Erg. DE-Fehler"
TEXT                    textHersteller
VALUE BYTE              "Anzahl TLS-Fehlerbytes" 
BITFIELD                bitFG254Fehlerbyte1
BITFIELD                bitFG2Fehlerbyte2
BITFIELD                bitFG254Fehlerbyte3
BITFIELD                bitFG254Fehlerbyte4
VALARRAY VAR 
    VALUE BYTE  "Hersteller-Fehlerbyte"

SUBTYPEDEF      subAchslast         "Achslastgruppe"
BITFIELD        bitAchsArt
VALUE USHORTLF  "Achslast"          "kg"
VALUE USHORTLF  "Achsabstand"       "cm"
   
SUBTYPEDEF      subAchslast2        "Achslastgruppe"
BITFIELD        bitAchsArt
VALUE FLOAT10F  "Achslast links"    "t"
VALUE FLOAT10F  "Achslast rechts"   "t"
VALUE USHORTLF  "Achsabstand"       "cm"
   
//////////////////////////////////////////////////
// DeBlock-Definitionen fuer FG 2

DEBLOCK 2   17,129      1   "DE-Fehler"
SUBTYPE                 subDE-Fehler

DEBLOCK 2   1,17,129    14  "Erg. DE-Fehlermeldung"
SUBTYPE                 subErgDeFehler2

DEBLOCK 2   130         16  "negative Quittung"
TEXT                    textNeqQuittFg2 
TEXT                    textHersteller

DEBLOCK 2   2           20  "Abruf Puffer"
VALUE   BYTE                "Jahr"
VALUE   BYTE                "Monat"
VALUE   BYTE                "Tag"
BITFIELD                bitStunde
VALUE   BYTE                "Minute"
VALUE   BYTE                "Anzahl Datensaetze"

DEBLOCK 2   2           21  "Abruf Puffer Einzelergebnis"
VALUE   USHORTLF            "Jahr"
VALUE   BYTE                "Monat"
VALUE   BYTE                "Tag"
BITFIELD                bitStunde
VALUE   BYTE                "Minute"
VALUE   BYTE                "Sekunde"
VALUE   USHORTL             "Anzahl Datensaetze"

DEBLOCK 2 2,18,130      29  "Kanalsteuerung"
BITFIELD                bitKanalsteuerung

DEBLOCK 2 129-133       30  "Zeitspempel" 				
TIME TYP30 

DEBLOCK 2 129-133       31  "Zeitspempel Einzelergebnis" 				
VALUE   USHORTL             "Jahr"
VALUE   BYTE                "Monat"
VALUE   BYTE                "Tag"
BITFIELD                bitStunde
VALUE   BYTE                "Minute"
VALUE   BYTE                "Sekunde"
VALUE   BYTE                "hundertstel Sekunde"
VALUE   USHORTL             "Datensatznummer"
VALUE   USHORTL             "Gesmtzahl Datensaetze"

DEBLOCK 2 3,19,131      32  "Betriebsparameter"
VALUE   BYTEF	              "reserviert"   
VALUE   BYTEF	              "reserviert" 
VALUE	BYTEF	                "Datenversion"   
BITFIELD 	              bitIntervallLang2
VALUE	BYTEF                 "reserviert"              
VALUE	BYTEF                 "Datenversion Einzel"							 
TEXT                    textSpeicherungGeraet

DEBLOCK 2 3,19,131      36  "geographische Kenndaten"
SUBTYPE                 subGeoKenn

DEBLOCK 2 3,19,131      37  "Grenzwerte"
VALUE USHORTLF              "max. Einzelachslast 1."    "kg"
VALUE USHORTLF              "max. Einzelachslast"       "kg"
VALUE BYTEF                 "max. Abstand 1 Doppela."   "cm"
VALUE USHORTLF              "max. Doppelachlast 1"      "kg"
VALUE BYTEF                 "max. Abstand 2 Doppela."   "cm"
VALUE USHORTLF              "max. Doppelachlast 2"      "kg"
VALUE BYTEF                 "max. Abstand 3 Doppela."   "cm"
VALUE USHORTLF              "max. Doppelachlast 3"      "kg"
VALUE BYTEF                 "max. Abstand 1 Dreifach"   "cm"
VALUE USHORTLF              "max. Dreifachachlast 1"    "kg"
VALUE BYTEF                 "max. Abstand 2 Dreifach"   "cm"
VALUE USHORTLF              "max. Dreifachachlast 2"    "kg"
VALARRAY VAR
    VALUE USHORTLF          "max. Gesamtgewicht"        "kg"

DEBLOCK 2 3,19,131      38  "Parameter"
VALARRAY VAR
    VALUE USHORTLF          "Gr. Einzelachslastkl."     "kg"
VALARRAY VAR
    VALUE USHORTLF          "Gr. Doppelachslastkl."     "kg"
VALARRAY VAR
    VALUE USHORTLF          "Gr. Dreifachachslastkl."   "kg"
VALARRAY VAR
    VALUE USHORTLF          "Fz-Kl. 3 Gesamtgew.-Kl."   "kg"
VALARRAY VAR
    VALUE USHORTLF          "Fz-Kl. 4 Gesamtgew.-Kl."   "kg"
VALARRAY VAR
    VALUE USHORTLF          "Fz-Kl. 5 Gesamtgew.-Kl."   "kg"

DEBLOCK 2 20,132,164    60  "Einzelergebnis Version 1"
TEXT                    textFahrzeugtyp
VALUE USHORTLF              "Fahrzeuglaenge"            "dm"
VALUE BYTEF                 "Geschwindigkeit"           "km/h"
VALUE USHORTLF              "Fahrzeugabstand"           "dm"
VALUE USHORTLF              "tats. Gesamtgewicht"       "kg"
BITFIELD                bitUeberladung
VALARRAY VAR  
    SUBTYPE             subAchslast

DEBLOCK 2 20,132,164    61  "Einzelergebnis Version 2"
TEXT                    textFahrzeugtyp
VALUE USHORTLF              "Fahrzeuglaenge"            "dm"
VALUE BYTEF                 "Geschwindigkeit"           "km/h"
VALUE USHORTLF              "Fahrzeugabstand"           "dm"
VALUE USHORTLF              "tats. Gesamtgewicht"       "kg"
BITFIELD                bitUeberladung
VALARRAY VAR  
    SUBTYPE             subAchslast2

DEBLOCK 2    132,164    64  "Zeitspempel" 				
VALUE BYTE              "Jahr"
VALUE BYTE              "Monat"
VALUE BYTE              "Tag"
BITFIELD                bitStunde
VALUE BYTE              "Minute"
BITFIELD 	              bitIntervallLang2

DEBLOCK 2 20,132,164    65  "Ergebnis Version 10" 				
VALARRAY VAR
    VALUE USHORTLF          "Einzelachslast-Kl."        "kg"
VALARRAY VAR
    VALUE USHORTLF          "Doppelachslast-Kl."        "kg"
VALARRAY VAR
    VALUE USHORTLF          "Dreifachachslast-Kl."      "kg"
VALUE BYTEF                 "Ueberladungen Fzg-Kl. 3"
VALUE BYTEF                 "Ueberladungen Fzg-Kl. 4"
VALUE BYTEF                 "Ueberladungen Fzg-Kl. 5"
VALARRAY VAR
    VALUE USHORTLF          "Fzg-Kl. 3 Gesamtgew.-Kl."  "kg"
VALARRAY VAR
    VALUE USHORTLF          "Fzg-Kl. 4 Gesamtgew.-Kl."  "kg"
VALARRAY VAR
    VALUE USHORTLF          "Fzg-Kl. 5 Gesamtgew.-Kl."  "kg"


//////////////////////////////////////////////////
// FG 3
//////////////////////////////////////////////////

//////////////////////////////////////////////////
// Textdefinitionen fuer FG 3

TEXTDEF         textNeqQuittFg3             "Ursache"
TEXTVAL     0   "sonstige Fehlerursache"
TEXTVAL     1   "unbekannte oder nicht auswertbare Id"
TEXTVAL     2   "unbekannter oder nicht auswertbarer Typ"
TEXTVAL 3-127   "reserviert"
TEXTVAL 128-255 "frei"

TEXTDEF         textUebertragungsverfahren     "Uebertragungsverfahren"
TEXTVAL     0   "Meldung nach Abruf"          
TEXTVAL     1   "zyklische Meldungen"
TEXTVAL 2-255   "nicht definiert"

TEXTDEF         textFahrbahnzustand            "Fahrbahnzustand"
TEXTVAL     0   "vollkommen trocken"
TEXTVAL     1   "feucht, Bedeckungsart unbestimmt"
TEXTVAL  2-31   "frei"
TEXTVAL    32   "benetzt mit fluessigem Wasser"
TEXTVAL 33-34   "frei"
TEXTVAL    64   "bedeckt mit gefrorenem Wasser"
TEXTVAL    65   "bedeckt mit Schnee"
TEXTVAL    66   "bedeckt mit Eis"
TEXTVAL    67   "bedeckt mit Rauhreif"
TEXTVAL 68-254  "frei"
TEXTVAL   255   "unbestimmt"

TEXTDEF         textNiederschlagsart            "Niederschlagsart"
TEXTVAL     0   "kein Niederschlag"
TEXTVAL  1-39   "nicht zu benutzen"
TEXTVAL    40   "Niederschlag aller Art"
TEXTVAL    41   "Leichter oder mittlerer Niederschlag"
TEXTVAL    42   "Starker Niederschlag"
TEXTVAL 43-49   "frei"
TEXTVAL    50   "Spruehregen"
TEXTVAL 51-59   "Spruehregen nach WMO klassifiziert"
TEXTVAL    60   "Regen"
TEXTVAL 61-69   "Regen nach WMO klassifiziert"
TEXTVAL    70   "Schnee"
TEXTVAL 71-73   "Schnee nach WMO klassifiziert"
TEXTVAL 74-76   "Graupel nach WMO klassifiziert"
TEXTVAL 77-79   "Hagel nach WMO klassifiziert"
TEXTVAL 80-254  "frei"
TEXTVAL   255   "unbestimmt"


//////////////////////////////////////////////////
// Bitfield-Definitionen fuer FG 3

BITFIELDDEF 1   bitFG3Fehlerbyte2         "Fehlerbyte 2"
BIT         0   "Subbus ausgefallen"
BIT         1   "RNR am Subbus"
BIT         2   "Sensor defekt"
BIT         3   "Rohwert ausserhalb der Grenzen"
BIT         4-7 "reserviert"


//////////////////////////////////////////////////
// Subtyp-Definitionen fuer FG 3
  
SUBTYPEDEF      subErgDeFehler3     "Erg. DE-Fehler"
TEXT                    textHersteller
VALUE BYTE              "Anzahl TLS-Fehlerbytes" 
BITFIELD                bitFG254Fehlerbyte1
BITFIELD                bitFG3Fehlerbyte2
BITFIELD                bitFG254Fehlerbyte3
BITFIELD                bitFG254Fehlerbyte4
VALARRAY VAR 
    VALUE BYTE  "Hersteller-Fehlerbyte"


//////////////////////////////////////////////////
// DeBlock-Definitionen fuer FG 3

DEBLOCK 3   17,129      1   "DE-Fehler"
SUBTYPE                 subDE-Fehler

DEBLOCK 3   1,17,129    14  "Erg. DE-Fehlermeldung"
SUBTYPE                 subErgDeFehler3

DEBLOCK 3   130         16  "negative Quittung"
TEXT                    textNeqQuittFg3
TEXT                    textHersteller


DEBLOCK 3 2,18,130      29  "Kanalsteuerung"
BITFIELD                bitKanalsteuerung

DEBLOCK 3 129-133       30  "Zeitspempel" 				
TIME TYP30 

DEBLOCK 3 3,19,131      32  "Betriebsparameter"
VALUE   USHORTL             "Erfassungsperiodendauer"     "s"  
TEXT     	              textUebertragungsverfahren

DEBLOCK 3 3,19,131      36  "geographische Kenndaten"
SUBTYPE                 subGeoKenn

DEBLOCK 3 20,132        48  "Lufttemperatur"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        49  "Fahrbahntemperatur"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        50  "Fahrbahnfeuchte, veraltet"
VALUE BYTEF             "Feuchte"               "%"

DEBLOCK 3 20,132        51  "Fahrbahnzustand, veraltet"
VALUE BYTEF             "Zustand"         

DEBLOCK 3 20,132        52  "Restsalz"
VALUE BYTEF             "Restsalz"              "%"

DEBLOCK 3 20,132        53  "Niederschlagsintensitaet"
VALUE FLOAT10FL2         "Nieder.-Intens."       "mm/h"

DEBLOCK 3 20,132        54  "Luftdruck"
VALUE   USHORTLF        "Luftdruck"             "hPa"  

DEBLOCK 3 20,132        55  "Relative Luftfeuchte"
VALUE BYTEF             "Luftfeuchte"           "%"

DEBLOCK 3 20,132        56  "Windrichtung"
VALUE   USHORTLF        "Windrichtung"        "Grad"  

DEBLOCK 3 20,132        57  "Windgeschwindigkeit Mittel"
VALUE FLOAT10FL2         "Windgeschw. mittel"    "m/s"

DEBLOCK 3 20,132        58  "Schneehoehe"
VALUE BYTEF             "Schneehoehe"           "cm"

DEBLOCK 3 20,132        60  "Sichtweite"
VALUE   USHORTLF        "Sichtweite"            "m"  

DEBLOCK 3 20,132        61  "Helligkeit"
VALUE   USHORTLF        "Helligkeit"            "Lx"  

DEBLOCK 3 20,132        63  "Niederschlagsart, veraltet"
VALUE BYTEF             "Art"         

DEBLOCK 3 20,132        64  "Windgeschwindigkeit Spitze"
VALUE FLOAT10FL2         "Windgeschw. max."      "m/s"

DEBLOCK 3 20,132        65  "Gefrierpunkttemperatur"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        66  "Taupunkttemperatur"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        67  "Bodentemperatur Tiefe 1"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        68  "Bodentemperatur Tiefe 2"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        69  "Bodentemperatur Tiefe 3"
VALUE FLOAT10FL2          "Temperatur"            "Grad C"

DEBLOCK 3 20,132        70  "Fahrbahnzustand"
TEXT                    textFahrbahnzustand

DEBLOCK 3 20,132        71  "Niederschlagsart"
TEXT                    textNiederschlagsart

DEBLOCK 3 20,132        72  "Wasserfilmdicke"
VALUE FLOAT100FL2       "Wasserfilmdicke"       "mm"


//////////////////////////////////////////////////
// FG 4
//////////////////////////////////////////////////

//////////////////////////////////////////////////
// Textdefinitionen fuer FG 4

TEXTDEF         textNeqQuittFg4             "Ursache"
TEXTVAL     0   "sonstige Fehlerursache"
TEXTVAL     1   "unbekannte oder nicht auswertbare Id"
TEXTVAL     2   "unbekannter oder nicht auswertbarer Typ"
TEXTVAL     3   "reserviert"
TEXTVAL     4 	"Stellcode auf diesem WZG nicht vorhanden"
TEXTVAL     5 	"Stellcode wegen Lampendefekt nicht schaltbar"
TEXTVAL     6 	"Stellcode wegen sonst. Hardwarestoerung n. schaltb."
TEXTVAL     7 	"Helligkeitswert nicht einstellbar"
TEXTVAL     8 	"keine automatische Helligkeitssteuerung moeglich"
TEXTVAL     9 	"Betriebsart unbekannt bzw. nicht einstellbar"
TEXTVAL    10 	"Befehl in dieser Betriebsart nicht ausfuehrbar"
TEXTVAL    11 	"nicht bearbeitbar, da Projektierungsdaten fehlerhaft"
TEXTVAL    12 	"Pufferinhalt nicht verfuegbar"
TEXTVAL    13 	"Prismencode nicht zulaessig"
TEXTVAL    14 	"Funktionsbyte unzulaessig"
TEXTVAL    15 	"Kanal passiviert"
TEXTVAL    16 	"Verriegelungsmatrix verletzt"
TEXTVAL    17 	"Anzahl Prismen/Anzeigen falsch"
TEXTVAL    18 	"kein freier Speicher fuer Programmdaten"
TEXTVAL    19 	"Wechseltext nicht darstellbar, da zu lang" 
TEXTVAL    20 	"Wechseltext enthaelt nicht darstellbare Zeichen" 
TEXTVAL    21   "falsches Anzeigeprinzip"
TEXTVAL 22-31   "reserviert"
TEXTVAL    32 	"vorheriges Programm noch nicht abgeschlossen"
TEXTVAL    33 	"Betriebsartenwechsel nicht abgeschlossen"
TEXTVAL    34 	"Betriebsart nicht eindeutig feststellbar"
TEXTVAL    35 	"Helligkeitsumschaltung noch nicht abgeschlossen"
TEXTVAL    36 	"Helligkeit nicht eindeutig feststellbar"
TEXTVAL    37 	"Keine Programmdaten verfuegbar" 
TEXTVAL    38 	"Uebergang zum Signalprogramm nicht definiert"
TEXTVAL    39 	"Uebergang zum Grundprogramm nicht definiert"
TEXTVAL    40 	"Programmdaten unvollstaendig"
TEXTVAL    41 	"Stellcode Vorrat nicht aenderbar"
TEXTVAL    42 	"Stellcode-Definition nicht aenderbar"
TEXTVAL    43 	"Unzulaessiger Stellcode"
TEXTVAL    44 	"Kurzversion mit diesem Stellcode nicht zulaessig"
TEXTVAL    45 	"Stellcode bei Steuerungprinzip 1 nicht zulaessig"
TEXTVAL    46 	"Langversion nicht zulaessig"
TEXTVAL    47 	"Loeschen benutzter Stellcodes nicht zulaessig"
TEXTVAL    48 	"Programmdefinition enthaelt undefinierten Stellcode"
TEXTVAL 49-127 	"reserviert"
TEXTVAL 128-255 "frei"

TEXTDEF         textWvzCode                 "WVZ-Code"
TEXTVAL     0   "---"
TEXTVAL     1   "Gefahrenstelle"
TEXTVAL     2   "Unebene Fahrbahn"
TEXTVAL     3   "Glaettegefahr"
TEXTVAL     4   "Schleudergefahr bei Naesse oder Schmutz"
TEXTVAL     5   "verengte Fahrbahn"
TEXTVAL     6   "einseitig verengte Fahrbahn rechts"
TEXTVAL     7   "einseitig verengte Fahrbahn links"
TEXTVAL     8   "Baustelle"
TEXTVAL     9   "Stau"
TEXTVAL    10   "Gegenverkehr"
TEXTVAL    11   "Schneefall"
TEXTVAL    12   "Lichtzeichenanlage"
TEXTVAL    13   "frei, Code 13"
TEXTVAL    14   "frei, Code 14"
TEXTVAL    15   "frei, Code 15"
TEXTVAL    16   "frei, Code 16"
TEXTVAL    17   "frei, Code 17"
TEXTVAL    18   "frei, Code 18"
TEXTVAL    19   "frei, Code 19"
TEXTVAL    20   "Hoechstgeschw.  20 km/h"
TEXTVAL    21   "Hoechstgeschw.  30 km/h"
TEXTVAL    22   "Hoechstgeschw.  40 km/h"
TEXTVAL    23   "Hoechstgeschw.  50 km/h"
TEXTVAL    24   "Hoechstgeschw.  60 km/h"
TEXTVAL    25   "Hoechstgeschw.  70 km/h"
TEXTVAL    26   "Hoechstgeschw.  80 km/h"
TEXTVAL    27   "Hoechstgeschw.  90 km/h"
TEXTVAL    28   "Hoechstgeschw. 100 km/h"
TEXTVAL    29   "Hoechstgeschw. 110 km/h"
TEXTVAL    30   "Hoechstgeschw. 120 km/h"
TEXTVAL    31   "Ueberholverbot allgemein"
TEXTVAL    32   "Ueberholverbot fuer LKW"
TEXTVAL    33   "Hoechstgeschw. 130 km/h"
TEXTVAL    34   "frei, Code 34"
TEXTVAL    35   "frei, Code 35"
TEXTVAL    36   "frei, Code 36"
TEXTVAL    37   "frei, Code 37"
TEXTVAL    38   "Verkehrsverbot bei Smog"
TEXTVAL    39   "Verbot fuer Fahrzeuge aller Art"
TEXTVAL    40   "Aufhebung Hoechstgeschw.  20 km/h"
TEXTVAL    41   "Aufhebung Hoechstgeschw.  30 km/h"
TEXTVAL    42   "Aufhebung Hoechstgeschw.  40 km/h"
TEXTVAL    43   "Aufhebung Hoechstgeschw.  50 km/h"
TEXTVAL    44   "Aufhebung Hoechstgeschw.  60 km/h"
TEXTVAL    45   "Aufhebung Hoechstgeschw.  70 km/h"
TEXTVAL    46   "Aufhebung Hoechstgeschw.  80 km/h"
TEXTVAL    47   "Aufhebung Hoechstgeschw.  90 km/h"
TEXTVAL    48   "Aufhebung Hoechstgeschw. 100 km/h"
TEXTVAL    49   "Aufhebung Hoechstgeschw. 110 km/h"
TEXTVAL    50   "Aufhebung Hoechstgeschw. 120 km/h"
TEXTVAL    51   "Ende Ueberholverbot allgemein"
TEXTVAL    52   "Ende Ueberholverbot fuer LKW"
TEXTVAL    53   "Ende aller Streckenverbote"
TEXTVAL    54   "Aufhebung Hoechstgeschw. 130 km/h"
TEXTVAL    55   "frei, Code 55"
TEXTVAL    56   "frei, Code 56"
TEXTVAL    57   "frei, Code 57"
TEXTVAL    58   "frei, Code 58"
TEXTVAL    59   "frei, Code 59"
TEXTVAL    60   "frei, Code 60"
TEXTVAL    61   "gelbes Blinklicht"
TEXTVAL    62   "STAU"
TEXTVAL    63   "STAUGEFAHR"
TEXTVAL    64   "NEBEL"
TEXTVAL    65   "NAESSE"
TEXTVAL    66   "UNFALL"
TEXTVAL    67   "SICHT" 
TEXTVAL    68   "SMOG" 
TEXTVAL    69   "ROLLSPLIT" 
TEXTVAL    70   "MAEHARBEITEN" 
TEXTVAL    71   "Ozon" 
TEXTVAL    72   "Laermschutz" 
TEXTVAL    73   "frei, Code 73" 
TEXTVAL    74   "600 m" 
TEXTVAL    75   "reserviert fuer 1004, Code 75" 
TEXTVAL    76   "reserviert fuer 1004, Code 76" 
TEXTVAL    77   "reserviert fuer 1004, Code 77" 
TEXTVAL    78   "200 m" 
TEXTVAL    79   "300 m" 
TEXTVAL    80   "400 m" 
TEXTVAL    81   "500 m"
TEXTVAL    82   "1000 m" 
TEXTVAL    83   "1500 m" 
TEXTVAL    84   "2000 m" 
TEXTVAL    85   "2500 m" 
TEXTVAL    86   "3000 m" 
TEXTVAL    87   "4000 m" 
TEXTVAL    88   "5000 m"
TEXTVAL    89   "frei, Code 89" 
TEXTVAL    90   "frei, Code 90" 
TEXTVAL    91   "auf 500 m" 
TEXTVAL    92   "auf 1000 m" 
TEXTVAL    93   "auf 1500 m" 
TEXTVAL    94   "auf 2000 m" 
TEXTVAL    95   "auf 2500 m" 
TEXTVAL    96   "auf 3000 m" 
TEXTVAL    97   "auf 4000 m" 
TEXTVAL    98   "auf 5000 m"
TEXTVAL    99   "frei, Code 99" 
TEXTVAL   100   "2,8t"                  
TEXTVAL   101   "4 t"
TEXTVAL   102   "7,5 t"
TEXTVAL   103   "Gefahr bei unerwarteter Glatteisbildung" 
TEXTVAL   104   "nur Lkw (Piktogramm)" 
TEXTVAL   105   "nur Pkw (Piktogramm)" 
TEXTVAL   106   "Staugefahr (Piktogramm)" 
TEXTVAL   107   "ROT" 
TEXTVAL   108   "GELB" 
TEXTVAL   109   "GRUEN" 
TEXTVAL   110   "ROT/GELB" 
TEXTVAL   111   "Fahrstreifen gesperrt=diagonales Kreuz rot"
TEXTVAL   112   "Fahrstreifen halten=Pfeil nach unten"
TEXTVAL   113   "Fahrstr. wechseln=gelber Pfeil n. l. unten"
TEXTVAL   114   "Fahrstr. wechseln=gelber Pfeil n. r. unten"
TEXTVAL   115   "frei, Code 115"
TEXTVAL   116   "frei, Code 116"
TEXTVAL   117   "frei, Code 117"
TEXTVAL   118   "frei, Code 118"
TEXTVAL   119   "frei, Code 119"
TEXTVAL   120   "frei, Code 120"
TEXTVAL   121   "Richtgeschwindigkeit  30 km/h"
TEXTVAL   122   "Richtgeschwindigkeit  40 km/h"
TEXTVAL   123   "Richtgeschwindigkeit  50 km/h"
TEXTVAL   124   "Richtgeschwindigkeit  60 km/h"
TEXTVAL   125   "Richtgeschwindigkeit  70 km/h"
TEXTVAL   126   "Richtgeschwindigkeit  80 km/h"
TEXTVAL   127   "Richtgeschwindigkeit  90 km/h"
TEXTVAL   128   "Richtgeschwindigkeit 100 km/h"
TEXTVAL   129   "Richtgeschwindigkeit 110 km/h"
TEXTVAL   130   "Richtgeschwindigkeit 120 km/h"
TEXTVAL   131   "Richtgeschwindigkeit 130 km/h"
TEXTVAL   132   "frei, Code 132"
TEXTVAL   133   "frei, Code 133"
TEXTVAL   134   "frei, Code 134"
TEXTVAL   135   "frei, Code 135"
TEXTVAL   136   "frei, Code 136"
TEXTVAL   137   "frei, Code 137"
TEXTVAL   138   "frei, Code 138"
TEXTVAL   139   "frei, Code 139"
TEXTVAL   140   "frei, Code 140"
TEXTVAL   141   "Aufhebung der Richtgeschw.  30 km/h"
TEXTVAL   142   "Aufhebung der Richtgeschw.  40 km/h"
TEXTVAL   143   "Aufhebung der Richtgeschw.  50 km/h"
TEXTVAL   144   "Aufhebung der Richtgeschw.  60 km/h"
TEXTVAL   145   "Aufhebung der Richtgeschw.  70 km/h"
TEXTVAL   146   "Aufhebung der Richtgeschw.  80 km/h"
TEXTVAL   147   "Aufhebung der Richtgeschw.  90 km/h"
TEXTVAL   148   "Aufhebung der Richtgeschw. 100 km/h"
TEXTVAL   149   "Aufhebung der Richtgeschw. 110 km/h"
TEXTVAL   150   "Aufhebung der Richtgeschw. 120 km/h"           
TEXTVAL   151   "Aufhebung der Richtgeschw. 130 km/h"
TEXTVAL   152   "frei, Code 152"
TEXTVAL   153   "frei, Code 153"
TEXTVAL   154   "frei, Code 154"
TEXTVAL   155   "frei, Code 155"
TEXTVAL   156   "frei, Code 156"
TEXTVAL   157   "frei, Code 157"
TEXTVAL   158   "frei, Code 158"
TEXTVAL   159   "frei, Code 159"
TEXTVAL   160   "frei, Code 160"
TEXTVAL   161   "frei, Code 161"
TEXTVAL   162   "frei, Code 162"
TEXTVAL   163   "frei, Code 163"
TEXTVAL   164   "frei, Code 164"
TEXTVAL   165   "frei, Code 165"
TEXTVAL   166   "frei, Code 166"
TEXTVAL   167   "frei, Code 167"
TEXTVAL   168   "frei, Code 168"
TEXTVAL   169   "frei, Code 169"
TEXTVAL   170   "frei, Code 170"
TEXTVAL   171   "frei, Code 171"
TEXTVAL   172   "frei, Code 172"
TEXTVAL   173   "frei, Code 173"
TEXTVAL   174   "frei, Code 174"
TEXTVAL   175   "frei, Code 175"
TEXTVAL   176   "frei, Code 176"
TEXTVAL   177   "frei, Code 177"
TEXTVAL   178   "frei, Code 178"
TEXTVAL   179   "frei, Code 179"
TEXTVAL   180   "frei, Code 180"
TEXTVAL   181   "frei, Code 181"
TEXTVAL   182   "frei, Code 182"
TEXTVAL   183   "frei, Code 183"
TEXTVAL   184   "frei, Code 184"
TEXTVAL   185   "frei, Code 185"
TEXTVAL   186   "frei, Code 186"
TEXTVAL   187   "frei, Code 187"
TEXTVAL   188   "frei, Code 188"
TEXTVAL   189   "frei, Code 189"
TEXTVAL   190   "frei, Code 190"
TEXTVAL   191   "frei, Code 191"
TEXTVAL   192   "frei, Code 192"
TEXTVAL   193   "frei, Code 193"
TEXTVAL   194   "frei, Code 194"
TEXTVAL   195   "frei, Code 195"
TEXTVAL   196   "frei, Code 196"
TEXTVAL   197   "frei, Code 197"
TEXTVAL   198   "frei, Code 198"
TEXTVAL   199   "frei, Code 199"
TEXTVAL   200   "Anlagenspezifisch 0"
TEXTVAL   201   "Anlagenspezifisch 1"
TEXTVAL   202   "Anlagenspezifisch 2"
TEXTVAL   203   "Anlagenspezifisch 3"
TEXTVAL   204   "Anlagenspezifisch 4"
TEXTVAL   205   "Anlagenspezifisch 5"
TEXTVAL   206   "Anlagenspezifisch 6"
TEXTVAL   207   "Anlagenspezifisch 7"
TEXTVAL   208   "Anlagenspezifisch 8"
TEXTVAL   209   "Anlagenspezifisch 9"
TEXTVAL   210   "Anlagenspezifisch 10"
TEXTVAL   211   "frei, Code 211"
TEXTVAL   212   "frei, Code 212"
TEXTVAL   213   "frei, Code 213"
TEXTVAL   214   "frei, Code 214"
TEXTVAL   215   "frei, Code 215"
TEXTVAL   216   "frei, Code 216"
TEXTVAL   217   "frei, Code 217"
TEXTVAL   218   "frei, Code 218"
TEXTVAL   219   "frei, Code 219"
TEXTVAL   220   "frei, Code 220"
TEXTVAL   221   "frei, Code 221"
TEXTVAL   222   "frei, Code 222"
TEXTVAL   223   "frei, Code 223"
TEXTVAL   224   "frei, Code 224"
TEXTVAL   225   "frei, Code 225"
TEXTVAL   226   "frei, Code 226"
TEXTVAL   227   "frei, Code 227"
TEXTVAL   228   "frei, Code 228"
TEXTVAL   229   "frei, Code 229"
TEXTVAL   230   "frei, Code 230"
TEXTVAL   231   "frei, Code 231"
TEXTVAL   232   "frei, Code 232"
TEXTVAL   233   "frei, Code 233"
TEXTVAL   234   "frei, Code 234"
TEXTVAL   235   "frei, Code 235"
TEXTVAL   236   "frei, Code 236"
TEXTVAL   237   "frei, Code 237"
TEXTVAL   238   "frei, Code 238"
TEXTVAL   239   "frei, Code 239"
TEXTVAL   240   "frei, Code 240"
TEXTVAL   241   "Prisma Seite 1"
TEXTVAL   242   "Prisma Seite 2"
TEXTVAL   243   "Prisma Seite 3"
TEXTVAL   244   "Prisma Seite 4"
TEXTVAL   245   "undefinierte Stellung"
TEXTVAL   246   "frei, Code 246"
TEXTVAL   247   "frei, Code 247"
TEXTVAL   248   "frei, Code 248"
TEXTVAL   249   "frei, Code 249"
TEXTVAL   250   "frei, Code 250"
TEXTVAL   251   "Wartungsstellung, (Rollo geschlossen)"
TEXTVAL   252   "frei, Code 252"
TEXTVAL   253   "frei, Code 253"
TEXTVAL   254   "frei, Code 254"
TEXTVAL   255   "Zustand bleibt erhalten (fuer Grundeinstellung)"

TEXTDEF         textBetriebsart            "Betriebsart"
TEXTVAL     0   "frei"
TEXTVAL     1   "Normalbetrieb"
TEXTVAL     2   "Blindbetrieb"
TEXTVAL     3   "Handbetrieb"
TEXTVAL     4   "Autarker Betrieb"
TEXTVAL     5   "Testbetrieb"
TEXTVAL     6   "Notbetrieb"
TEXTVAL 7-255   "nicht definiert"

TEXTDEF         textAnzeigeprinzip        "Anzeigeprinzip"
TEXTVAL     0   "a"
TEXTVAL     1   "b"
TEXTVAL     2   "c"
TEXTVAL     3   "d"
TEXTVAL     4   "e"
TEXTVAL     8   "Cluster"
TEXTVAL 5-7,9-255 "undefiniert"

TEXTDEF         textFunktionsbyte         "Funktionsbyte"
TEXTVAL     0   "ausschalten"
TEXTVAL     1   "einschalten"
TEXTVAL     2   "blinken"
TEXTVAL     3   "reserviert"


//////////////////////////////////////////////////
// Bitfield-Definitionen fuer FG 4

BITFIELDDEF 1   bitFG4Fehlerbyte2         "Fehlerbyte 2"
BIT         0   "Subbus ausgefallen"
BIT         1   "RNR am Subbus"
BIT         2   "WZG defekt"
BIT         3   "WZG Versorgungsspannung"
BIT         4   "Fehler in Prismenansteuerung"
BIT         5   "fehler in Wechseltextansteuerung"
BIT         6-7 "reserviert"

BITFIELDDEF 1   bitFunktionsbyte        "Funktionsbyte"
SUBTEXT     3   textFunktionsbyte
BIT         2   "Stoerung"
BIT         3   "Programm nicht abgeschlossen"
SUBVALUE 0xf0 4
    VALUE OBYTE         "Blinkzeit" "in 200 ms"
    
BITFIELDDEF 1   bitHelligkeitsstaus     "Status"
BIT         0   "automatische, lokale Helligkeitssteuerung"         "Helligkeit wird durch Zentrale eingestellt"  
BIT         1   "nach jeder Aenderung Helliglkeit spontan senden"   "keine spontanen Meldungen"

BITFIELDDEF 1   bitProgrammStatus       "Status"
BIT         0   "Programm nicht abgeschlossen"    "Programm abgeschlossen"
BIT         1   "Fehler aufgetreten"
BIT       2-7   "reserviert"

    
//////////////////////////////////////////////////
// Subtyp-Definitionen fuer FG 4
  
SUBTYPEDEF      subErgDeFehler4     "Erg. DE-Fehler"
TEXT                    textHersteller
VALUE BYTE              "Anzahl TLS-Fehlerbytes" 
BITFIELD                bitFG254Fehlerbyte1
BITFIELD                bitFG4Fehlerbyte2
BITFIELD                bitFG254Fehlerbyte3
BITFIELD                bitFG254Fehlerbyte4
VALARRAY VAR 
    VALUE BYTE  "Hersteller-Fehlerbyte"

SUBTYPEDEF              subWVZCodeFunktion
TEXT                    textWvzCode
BITFIELD                bitFunktionsbyte

SUBTYPEDEF              subStellCodeFunktion
VALUE BYTE              "Stellcode"
BITFIELD                bitFunktionsbyte


SUBTYPEDEF              subAnzeigeAB     "Anzeigeprinzip"
TEXT                    textAnzeigeprinzip      
TEXT                    textWvzCode
BITFIELD                bitFunktionsbyte

SUBTYPEDEF              subAnzeigeCD     "Anzeigeprinzip"
TEXT                    textAnzeigeprinzip      
VALUE BYTE              "Stellcode"
BITFIELD                bitFunktionsbyte
OPT PSTRING             "Text"

SUBTYPEDEF              subAnzeigeE      "Anzeigeprinzip"
TEXT                    textAnzeigeprinzip      
VALUE BYTE              "Stellcode"
BITFIELD                bitFunktionsbyte
OPT VALARRAY VAR
    SUBTYPE             subWVZCodeFunktion

SUBTYPEDEF              subAnzeigeCluster "Anzeigeprinzip"
TEXT                    textAnzeigeprinzip      
SUBTYPE                 subStellCodeFunktion


SUBTYPEDEF              subAnzeigeListeAB
TEXT                    textAnzeigeprinzip 
VALARRAY VAR
    TEXT                textWvzCode
    
SUBTYPEDEF              subAnzeigeListe
TEXT                    textAnzeigeprinzip 
VALARRAY VAR
    VALUE BYTE          "Stellcode"


SUBTYPEDEF              subClusterKomponente
VALUE BYTE              "DE-Nummer"
TEXT                    textAnzeigeprinzip
SUBTYPE                 subStellCodeFunktion
 
SUBTYPEDEF              subCodeDefinitionB
TEXT                    textAnzeigeprinzip 
TEXT                    textWvzCode
VALARRAY VAR
    VALUE BYTE          "Lampennummer"

SUBTYPEDEF              subCodeDefinitionCD
TEXT                    textAnzeigeprinzip 
VALUE BYTE              "Stellcode"
PSTRING                 "Text"

SUBTYPEDEF              subCodeDefinitionE
TEXT                    textAnzeigeprinzip 
VALUE BYTE              "Stellcode"
VALARRAY VAR
    SUBTYPE             subWVZCodeFunktion
    
SUBTYPEDEF              subCodeDefinitionCluster
TEXT                    textAnzeigeprinzip 
VALUE BYTE              "Stellcode"
VALARRAY VAR
    SUBTYPE             subClusterKomponente
      

//////////////////////////////////////////////////
// DeBlock-Definitionen fuer FG 4

DEBLOCK 4   17,129,161  1   "DE-Fehler"
SUBTYPE                 subDE-Fehler

DEBLOCK 4   17,129,161  2   "nicht darstellbare WVZ"
VALARRAY VAR
    TEXT                textWvzCode
    
DEBLOCK 4   17,129      3   "Defekte Lampen"
SPECIAL DEFEKTELAMPEN       "Defekte Lampen"
     
DEBLOCK 4   17,129,161  4   "gestoerte Textposition"
VALARRAY VAR
    VALUE BYTE          "Textposition"
    
DEBLOCK 4   1,17,129    14  "Erg. DE-Fehlermeldung"
SUBTYPE                 subErgDeFehler4

DEBLOCK 4   130         16  "negative Quittung"
TEXT                    textNeqQuittFg4
TEXT                    textHersteller

DEBLOCK 4 2,18,130,162  17  "Betriebsart"
TEXT                    textBetriebsart

DEBLOCK 4   2           20  "Abruf Puffer"
VALUE   USHORTL         "erste Folgenummer"
VALUE   BYTE            "Anzahl Datensaetze"

DEBLOCK 4   3           21  "Abruf Codedefinition"
VALUE   BYTE            "Stellcode"

DEBLOCK 4 2,18,130      29  "Kanalsteuerung"
BITFIELD                bitKanalsteuerung

DEBLOCK 4 129           30  "Zeitspempel"         
TIME TYP30 

DEBLOCK 4 129-133,160-65 31 "Zeitspempel"         
TIME TYP31 

DEBLOCK 4 3,19,131      32  "Grundeinstellung (alt)"
SUBTYPE                 subWVZCodeFunktion

DEBLOCK 4 3,19,131      33  "Grundeinstellung"
IFVAL       0,1 SUBTYPE subAnzeigeAB
ELSE IFVAL  2,3 SUBTYPE subAnzeigeCD
ELSE IFVAL  4   SUBTYPE subAnzeigeE
ELSE IFVAL  8   SUBTYPE subAnzeigeCluster   

//DEBLOCK 4 3,19,131      36  "geographische Kenndaten"
//SUBTYPE                 subGeoKenn

DEBLOCK 4 3,19,131      42  "Grundprogramm (alt)"
VALUE BYTE              "Grundprogramm"

DEBLOCK 4 3,19,131      43  "Codeliste"
IFVAL       0,1 SUBTYPE subAnzeigeListeAB
ELSE            SUBTYPE subAnzeigeListe

DEBLOCK 4 3,131         44  "Codedefinition"
IFVAL       1   SUBTYPE subCodeDefinitionB
ELSE IFVAL  2,3 SUBTYPE subCodeDefinitionCD
ELSE IFVAL  4   SUBTYPE subCodeDefinitionE
ELSE IFVAL  8   SUBTYPE subCodeDefinitionCluster

DEBLOCK 4 5,21,133      48  "Stellzustand (alt)"
SUBTYPE                 subWVZCodeFunktion

DEBLOCK 4 5,21,133      49  "Helligkeit"
VALUE BYTE              "Helligkeit"      "%"
BITFIELD                bitHelligkeitsstaus

DEBLOCK 4 5,21,133      50  "Wechseltext (alt)"
BITFIELD                bitFunktionsbyte
PSTRING                 "Text"

DEBLOCK 4 5,21,133      55  "Stellzustand"
IFVAL       0,1 SUBTYPE subAnzeigeAB
ELSE IFVAL  2,3 SUBTYPE subAnzeigeCD
ELSE IFVAL  4   SUBTYPE subAnzeigeE
ELSE IFVAL  8   SUBTYPE subAnzeigeCluster   

DEBLOCK 4 5,21,133      58  "Stellprogramm (alt)"
VALUE BYTE              "Programm-Nummer"
BITFIELD                bitProgrammStatus


//////////////////////////////////////////////////
// FG 6
//////////////////////////////////////////////////

//////////////////////////////////////////////////
// Textdefinitionen fuer FG 6

TEXTDEF     textNeqQuittFg6                   "Ursache"
TEXTVAL 0 	"sonstige Fehlerursache"
TEXTVAL 1   "unbekannte oder nicht auswertbare ID"
TEXTVAL 2 	"unbekannter oder nicht auswertbarer Typ"
TEXTVAL 3-127 "reserviert"
TEXTVAL 128-255 "Herstellerdefiniert"

TEXTDEF     textUebtragungsverfahren   "Uebertragungsverfahren"
TEXTVAL 0   "Meldung nur nach Abruf"
TEXTVAL 1   "zyklische Abgabe von Meldungen"
TEXTVAL 2   "nach Zustandsaenderung"
TEXTVAL 3-255 "nicht definiert"

//////////////////////////////////////////////////
// Bitfield-Definitionen fuer FG 6

BITFIELDDEF 1   bitFG6Fehlerbyte2         "Fehlerbyte 2"
BIT         0   "Subbus ausgefallen"
BIT         1   "RNR am Subbus"
BIT         2   "Sensor defekt"
BIT         3-7 "reserviert"

BITFIELDDEF 1   bitTuer           "Tuer"
BIT         0   "offen"           "geschlossen"
BIT       1-7   "verboten"

BITFIELDDEF 1   bitTemperaturueberwachung         "Temperaturueberwachung"
BIT         0   "defekt"          "ok"
BIT         1   "Bereich ueberschritten"
BIT         2   "Bereich unterschritten"
BIT       3-7   "verboten"

BITFIELDDEF 1   bitLicht          "Licht"
BIT         0   "eingeschaltet"   "ausgeschaltet"
BIT       1-7   "verboten"

BITFIELDDEF 1   bitStrom          "Stromversorgung"
BIT         0   "Netzspannung ausgefallen"        "Netzspannung ok"
BIT         1   "USV defekt"
BIT         2   "Akku entladen"
BIT         3   "Akku ueberladen"
BIT         4   "FI-Schutzschalter ausgeloest"
BIT       5-7   "verboten"

BITFIELDDEF 1   bitHeizung        "Heizung"
BIT         0   "Heizung defekt"  "Heizung ok"
BIT         1   "eingeschaltet"   "ausgeschaltet"
BIT       2-7   "verboten"

BITFIELDDEF 1   bitLueftung       "Lueftung"
BIT         0   "Lueftung defekt" "Lueftung ok"
BIT         1   "eingeschaltet"   "ausgeschaltet"
BIT       2-7   "verboten"

BITFIELDDEF 1   bitUeberspannungsschutz           "Ueberspannungsschutz"
BIT         0   "defekt"          "ok"
BIT       1-7   "verboten"

BITFIELDDEF 1   bitDiebstahlschutz                "Diebstahlschutz"
BIT         0   "ALARM"           "kein Alarm"
BIT       1-7   "verboten"

BITFIELDDEF 1   bitHoehenkontrolle                "Hoehenkontrolle"
BIT         0   "WARNUNG"         "keine Warnung"
BIT         1   "ALARM"           "kein Alarm"
BIT       2-7   "verboten"


//////////////////////////////////////////////////
// Subtyp-Definitionen fuer FG 6
  
SUBTYPEDEF      subErgDeFehler6     "Erg. DE-Fehler"
TEXT                    textHersteller
VALUE BYTE              "Anzahl TLS-Fehlerbytes" 
BITFIELD                bitFG254Fehlerbyte1
BITFIELD                bitFG6Fehlerbyte2
BITFIELD                bitFG254Fehlerbyte3
BITFIELD                bitFG254Fehlerbyte4
VALARRAY VAR 
    VALUE BYTE  "Hersteller-Fehlerbyte"


//////////////////////////////////////////////////
// DeBlock-Definitionen fuer FG 6

DEBLOCK 6   17,129      1   "DE-Fehler"
SUBTYPE                 subDE-Fehler

DEBLOCK 6   1,17,129    14  "Erg. DE-Fehlermeldung"
SUBTYPE                 subErgDeFehler6

DEBLOCK 6   130         16  "negative Quittung"
TEXT                    textNeqQuittFg6 
TEXT                    textHersteller

DEBLOCK 6 2,18,130      29  "Kanalsteuerung"
BITFIELD                bitKanalsteuerung

DEBLOCK 6 129-133       30  "Zeitspempel" 				
TIME TYP30

DEBLOCK 6  3,19,131     32  "Betriebsparameter"
VALUE USHORTL           "Erfassungsperiodendauer"   "Sekunden"
TEXT                    textUebtragungsverfahren

DEBLOCK 6 3,19,131      36  "geographische Kenndaten"
SUBTYPE                 subGeoKenn


DEBLOCK 6 20,132 48 Tuerkontakt
BITFIELD        bitTuer

DEBLOCK 6 20,132 49 Temperaturueberwachung
BITFIELD        bitTemperaturueberwachung

DEBLOCK 6 20,132 50 Licht
BITFIELD        bitLicht

DEBLOCK 6 20,132 51 bitStromversorgung
BITFIELD        bitStrom

DEBLOCK 6 20,132 52 Heizung
BITFIELD        bitHeizung

DEBLOCK 6 20,132 53 Lueftung
BITFIELD        bitLueftung

DEBLOCK 6 20,132 54 Ueberspannungsschutz
BITFIELD        bitUeberspannungsschutz

DEBLOCK 6 20,132 55 Diebstahlschutz
BITFIELD        bitDiebstahlschutz

DEBLOCK 6 20,132 56 "Fernueberwachung Solaranlagen"
VALUE BYTEF     "Ladespannung"  "Volt"
VALUE FLOAT100F "Ladestrom"     "Ampere"

DEBLOCK 6 20,132 220 "Encoder Alarm"
VALUE BYTE        Zustand

DEBLOCK 6 20,132 221 "Encoder Alarm Erdschluss"
VALUE BYTE        Zustand

DEBLOCK 6 20,132 222 "Schluesselschalter"
VALUE BYTE        Zustand

DEBLOCK 6 20,132 223 "Encoder Alarm Temp."
VALUE BYTE        Zustand

// S1-spezifische Erweiterungen

DEBLOCK 6 132 132 "Hoehenkontrolle"
TEXT             textHersteller
VALUE   BYTE     "Anzahl der Ereignismeldungen"
BITFIELD         bitHoehenkontrolle
