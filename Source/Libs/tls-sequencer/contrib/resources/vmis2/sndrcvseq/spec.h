/******************************************************************************
*
* Name         : spec.h
*
* Beschreibung : Spezifikationsdatei fuer db2osi (Senden) fuer 
*                UZ-SM Kommunikation (nach unten).
*
*
*                Nummerierungsschema fuer: "Wertreaktion DA SPALTE bei NR..."
*                ============================================================
*
*                Aufbau von NR:
*
*                XIIITTT (immer 7-stellig !) mit
*
*                x = 1,      wenn "an DE" bei Abruf 
*                x = 2,      wenn "an SM" bei Abruf
*                x = 3,      wenn "an DE" beim Senden (Parameter)
*                x = 4,      wenn "an SM" beim Senden (Parameter)
*                x = 5,      wenn "an CLUSTER" bei Abruf
*                x = 6,      wenn "an CLUSTER" beim Senden (Parameter)
*                III = ID,   immer dreistellig !
*                TTT = TYP,  immer dreistellig !
*
*                Nummerierungsschema fuer die zugehoerigen defines :
*                ===================================================
*
*                Aufbau: FFF_XX_R_Text
*
*                FFF  : FG-Kuerzel gemaess Schema in db_desc:
*                   
*                       SYS : FG 254
*                       LVE : FG 1
*                           : FG 2
*                       UFD : FG 3
*                       WVZ : FG 4
*                       WWW : FG 5
*                       VLT : FG 6
*                       LSA : FG 128  (Zuflussregelung der A40)
*
*                XX   : DE (bei x=1 oder x=3) oder SM (bei x=2 oder x=4) oder
*                       Cluster (bei x=5 oder x=6)
*       
*                R    : ABRUF  (bei Datenabruf, x=1 oder x=2) bzw.
*                       SENDEN (bei Datenabruf, x=3 oder x=4) bzw.
*                
*                Text : Bezeichnung entspr. TLS, z.B. DEFEHLER oder 
*
*
*
* #define UFD_DE_ABRUF_UFD_BETRIEBSPARAMETER              1019032 
* #define UFD_SM_ABRUF_UFD_BETRIEBSPARAMETER              2019032 
* #define UFD_DE_SENDEN_UFD_BETRIEBSPARAMETER             3003032 
* #define UFD_SM_SENDEN_UFD_BETRIEBSPARAMETER             4003032 
* #define VLT_DE_ABRUF_UFD_BETRIEBSPARAMETER              1019032 
* #define VLT_SM_ABRUF_UFD_BETRIEBSPARAMETER              2019032 
* #define VLT_DE_SENDEN_UFD_BETRIEBSPARAMETER             3003032 
* #define VLT_SM_SENDEN_UFD_BETRIEBSPARAMETER             4003032 
*
*****************************************************************************/


#define DE_GUT_MELDUNG              0x00
#define DE_STOER_BITS               0x03

#define FEHLER_VOM_EAK_ERKANNT      0x01
#define FEHLER_VOM_SM_ERKANNT       0x02
#define FEHLER_PROJEKTIERUNG        0x04
#define KANAL_PASSIV                0x08

#define FEHLER_WEGEN_AUSFALL_SM     0xff
#define HERSTELLER_HB               0x06        

#define FOLGENUMMER_FEHLER(DA) DA !NFN 1

#define AUTARKBETRIEB	            0x04

/* ======================================================================== */
/* Steuerung der KRUZ / Telegramme der FG 254 / Globale Abfragen            */ 
/* ======================================================================== */

#define GLOBALE_ZEITSYNCHRONISATION     2002018
#define LOKALE_ZEITSYNCHRONISATION      4002018

/* ======================================================================== */
/* FG 254 - Abruftelegramme an DE                                           */ 
/* ======================================================================== */
                
#define SYS_DE_ABRUF_DE_FEHLER                          1017001 
#define SYS_DE_ABRUF_ERG_DE_FEHLER                      1017014 
#define SYS_DE_ABRUF_ZEITSYNC                      		1018018
#define SYS_DE_ABRUF_DUE_STATUS                         1018019
#define SYS_DE_ABRUF_STATISCHE_GERAETEKENNDATEN         1019032 
#define SYS_DE_ABRUF_KONFIG_TABELLE                     1019034
#define SYS_DE_ABRUF_OSI3_ROUTINGFELD                   1019035
#define SYS_DE_ABRUF_GEO_KENN                           1019036
#define SYS_DE_ABRUF_KNOTENNUMMERN                      1019037
#define SYS_DE_ABRUF_KONFTABKRI                         1019127

/* ======================================================================== */
/* FG 254 - Abruftelegramme an SM                                           */ 
/* ======================================================================== */

#define SYS_SM_ABRUF_DE_FEHLER                          2017001 
#define SYS_SM_ABRUF_ERG_DE_FEHLER                      2017014 
#define SYS_SM_ABRUF_ZEITSYNC                      		2018018
#define SYS_SM_ABRUF_DUE_STATUS                         2018019
#define SYS_SM_ABRUF_STATISCHE_GERAETEKENNDATEN         2019032 
#define SYS_SM_ABRUF_KONFIG_TABELLE                     2019034
#define SYS_SM_ABRUF_OSI3_ROUTINGFELD                   2019035
#define SYS_SM_ABRUF_GEO_KENN                           2019036
#define SYS_SM_ABRUF_KNOTENNUMMERN                      2019037
#define SYS_SM_ABRUF_KONFTABKRI                         2019127

/* ======================================================================== */
/* FG 254 - Telegramme mit Daten senden (Wertreaktion) an DE                */ 
/* ======================================================================== */

#define SYS_DE_SENDEN_STATISCHE_GERAETEKENNDATEN        3003032 
#define SYS_DE_SENDEN_KONFIG_TABELLE                    3003034
#define SYS_DE_SENDEN_OSI3_ROUTINGFELD                  3003035
#define SYS_DE_SENDEN_GEO_KENN                          3003036
#define SYS_DE_SENDEN_KNOTENNUMMERN                     3003037

/* ======================================================================== */
/* FG 254 - Telegramme mit Daten senden (Wertreaktion) an SM                */ 
/* ======================================================================== */

#define SYS_SM_SENDEN_STATISCHE_GERAETEKENNDATEN        4003032 
#define SYS_SM_SENDEN_KONFIG_TABELLE                    4003034
#define SYS_SM_SENDEN_OSI3_ROUTINGFELD                  4003035
#define SYS_SM_SENDEN_GEO_KENN                          4003036
#define SYS_SM_SENDEN_KNOTENNUMMERN                     4003037

/* ======================================================================== */
/* FG 1 - Abruftelegramme an DE                                             */ 
/* ======================================================================== */

#define LVE_DE_ABRUF_DE_FEHLER                          1017001 
#define LVE_DE_ABRUF_ERG_DE_FEHLER                      1017014
#define LVE_DE_ABRUF_KANAL_STEUERUNG                    1018029
#define LVE_DE_ABRUF_LVE_BETRIEBSPARAMETER              1019032 
#define LVE_DE_ABRUF_SVE_BETRIEBSPARAMETER              1019034 
#define LVE_DE_ABRUF_GEO_KENN                           1019036
#define LVE_DE_ABRUF_VKLASSE_KURZ                       1019037
#define LVE_DE_ABRUF_VKLASSE_LANG                       1019038
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_0                 1020049
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_1                 1020050
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_2                 1020051
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_3                 1020052
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_4                 1020053

/* Ab hier Langzeitdaten */
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_10                1020065
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_11                1020066
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_12                1020067
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_13                1020068
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_14                1020069
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_15                1020070
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_16                1020071
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_17                1020072
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_18                1020073
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_19                1020074
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_20                1020075
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_21                1020076
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_22                1020077
#define LVE_DE_ABRUF_LVE_ERGEBNIS_OPT_23                1020078

#define LVE_DE_ABRUF_SVE_ERGEBNIS_OPT_0                 1020096
#define LVE_DE_ABRUF_SVE_ERGEBNIS_OPT_1                 1020097

/* ======================================================================== */
/* FG 1 - Abruftelegramme an SM                                             */ 
/* ======================================================================== */

#define LVE_SM_ABRUF_DE_FEHLER                          2017001
#define LVE_SM_ABRUF_ERG_DE_FEHLER                      2017014
#define LVE_SM_ABRUF_PUFFER_LANG                        2002020 
#define LVE_SM_ABRUF_KANAL_STEUERUNG                    2018029
#define LVE_SM_ABRUF_LVE_BETRIEBSPARAMETER              2019032 
#define LVE_SM_ABRUF_SVE_BETRIEBSPARAMETER              2019034 
#define LVE_SM_ABRUF_GEO_KENN                           2019036
#define LVE_SM_ABRUF_VKLASSE_KURZ                       2019037
#define LVE_SM_ABRUF_VKLASSE_LANG                       2019038
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_0                 2020049
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_1                 2020050
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_2                 2020051
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_3                 2020052
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_4                 2020053

/* Ab hier Langzeitdaten */
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_10                2020065
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_11                2020066
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_12                2020067
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_13                2020068
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_14                2020069
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_15                2020070
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_16                2020071
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_17                2020072
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_18                2020073
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_19                2020074
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_20                2020075
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_21                2020076
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_22                2020077
#define LVE_SM_ABRUF_LVE_ERGEBNIS_OPT_23                2020078

#define LVE_SM_ABRUF_SVE_ERGEBNIS_OPT_0                 2020096
#define LVE_SM_ABRUF_SVE_ERGEBNIS_OPT_1                 2020097


/* ======================================================================== */
/* FG 1 - Telegramme mit Daten senden (Wertreaktion) an DE                  */ 
/* ======================================================================== */

#define LVE_DE_SENDEN_KANAL_STEUERUNG                   3002029
#define LVE_DE_SENDEN_LVE_BETRIEBSPARAMETER             3003032 
#define LVE_DE_SENDEN_SVE_BETRIEBSPARAMETER             3003034 
#define LVE_DE_SENDEN_GEO_KENN                          3003036
#define LVE_DE_SENDEN_VKLASSE_KURZ                      3019037
#define LVE_DE_SENDEN_VKLASSE_LANG                      3019038


/* ======================================================================== */
/* FG 1 - Telegramme mit Daten senden (Wertreaktion) an SM                  */ 
/* ======================================================================== */

#define LVE_SM_SENDEN_KANAL_STEUERUNG                   4002029
#define LVE_SM_SENDEN_LVE_BETRIEBSPARAMETER             4003032 
#define LVE_SM_SENDEN_SVE_BETRIEBSPARAMETER             4003034 
#define LVE_SM_SENDEN_GEO_KENN                          4003036
#define LVE_SM_SENDEN_VKLASSE_KURZ                      4019037
#define LVE_SM_SENDEN_VKLASSE_LANG                      4019038


/* ======================================================================== */
/* FG 2 - Abruftelegramme an DE                                             */ 
/* ======================================================================== */

#define AXL_DE_ABRUF_DE_FEHLER                          1017001 
#define AXL_DE_ABRUF_ERG_DE_FEHLER                      1017014
#define AXL_DE_ABRUF_KANAL_STEUERUNG                    1018029
#define AXL_DE_ABRUF_BETRIEBSPARAMETER                  1019032 
#define AXL_DE_ABRUF_GEO_KENN                           1019036
#define AXL_DE_ABRUF_GRENZWERTE                         1019037
#define AXL_DE_ABRUF_PARAMETER                          1019038
#define AXL_DE_ABRUF_OPTION10                           1012065


/* ======================================================================== */
/* FG 2 - Abruftelegramme an SM                                             */ 
/* ======================================================================== */

#define AXL_SM_ABRUF_DE_FEHLER                          2017001 
#define AXL_SM_ABRUF_ERG_DE_FEHLER                      2017014
#define AXL_SM_ABRUF_KANAL_STEUERUNG                    2018029
#define AXL_SM_ABRUF_BETRIEBSPARAMETER                  2019032 
#define AXL_SM_ABRUF_GEO_KENN                           2019036
#define AXL_SM_ABRUF_GRENZWERTE                         2019037
#define AXL_SM_ABRUF_PARAMETER                          2019038
#define AXL_SM_ABRUF_OPTION10                           2020065


/* ======================================================================== */
/* FG 2 - Telegramme mit Daten senden (Wertreaktion) an DE                  */
/* ======================================================================== */

#define AXL_DE_SENDEN_KANAL_STEUERUNG                   3002029
#define AXL_DE_SENDEN_BETRIEBSPARAMETER                 3003032
#define AXL_DE_SENDEN_GRENZWERTE                        3003037
#define AXL_DE_SENDEN_PARAMETER                         3003038


/* ======================================================================== */
/* FG 2 - Telegramme mit Daten senden (Wertreaktion) an SM                  */
/* ======================================================================== */

#define AXL_SM_SENDEN_KANAL_STEUERUNG                   4002029
#define AXL_SM_SENDEN_BETRIEBSPARAMETER                 4003032
#define AXL_SM_SENDEN_GRENZWERTE                        4003037
#define AXL_SM_SENDEN_PARAMETER                         4003038


/* ======================================================================== */
/* FG 3 - Abruftelegramme an DE                                             */ 
/* ======================================================================== */

#define UFD_DE_ABRUF_DE_FEHLER                          1017001 
#define UFD_DE_ABRUF_ERG_DE_FEHLER                      1017014
#define UFD_DE_ABRUF_KANAL_STEUERUNG                    1018029
#define UFD_DE_ABRUF_UFD_BETRIEBSPARAMETER              1019032 
#define UFD_DE_ABRUF_BETRIEBSPARAMETER                  1019032 
#define UFD_DE_ABRUF_TREPPENFUNKTION                    1019035 
#define UFD_DE_ABRUF_GEO_KENN                           1019036
#define UFD_DE_ABRUF_LUFT_TEMP                          1020048
#define UFD_DE_ABRUF_FAHRBAHNOBERFLAECHEN_TEMP          1020049
#define UFD_DE_ABRUF_FAHRBAHN_FEUCHTE                   1020050
#define UFD_DE_ABRUF_FAHRBAHN_ZUSTAND                   1020051
#define UFD_DE_ABRUF_RESTSALZGEHALT                     1020052
#define UFD_DE_ABRUF_NIEDERSCHLAG                       1020053
#define UFD_DE_ABRUF_LUFTDRUCK                          1020054
#define UFD_DE_ABRUF_RELATIVE_LUFTFEUCHTE               1020055
#define UFD_DE_ABRUF_WINDRICHTUNG                       1020056
#define UFD_DE_ABRUF_WINDGESCHW_MITTEL                  1020057
#define UFD_DE_ABRUF_SCHNEEHOHE                         1020058
#define UFD_DE_ABRUF_FAHRBAHN_GLAETTE                   1020059
#define UFD_DE_ABRUF_SICHTWEITE                         1020060
#define UFD_DE_ABRUF_HELLIGKEIT                         1020061
#define UFD_DE_ABRUF_NIEDERSCHLAGSMENGE                 1020062
#define UFD_DE_ABRUF_NIEDERSCHLAGSART                   1020063
#define UFD_DE_ABRUF_WINDGESCHW_MAX                     1020064
#define UFD_DE_ABRUF_GEFRIER_TEMP                       1020065
#define UFD_DE_ABRUF_TAUPUNKT_TEMP                      1020066
#define UFD_DE_ABRUF_TIEFE1_TEMP                        1020067
#define UFD_DE_ABRUF_TIEFE2_TEMP                        1020068
#define UFD_DE_ABRUF_TIEFE3_TEMP                        1020069
#define UFD_DE_ABRUF_FBZ                                1020070
#define UFD_DE_ABRUF_NS                                 1020071
#define UFD_DE_ABRUF_WFD                                1020072


/* ======================================================================== */
/* FG 3 - Abruftelegramme an SM                                             */ 
/* ======================================================================== */

#define UFD_SM_ABRUF_DE_FEHLER                          2017001 
#define UFD_SM_ABRUF_ERG_DE_FEHLER                      2017014
#define UFD_SM_ABRUF_KANAL_STEUERUNG                    2018029
#define UFD_SM_ABRUF_UFD_BETRIEBSPARAMETER              2019032 
#define UFD_SM_ABRUF_BETRIEBSPARAMETER                  2019032 
#define UFD_SM_ABRUF_TREPPENFUNKTION                    2019035 
#define UFD_SM_ABRUF_GEO_KENN                           2019036
#define UFD_SM_ABRUF_LUFT_TEMP                          2020048
#define UFD_SM_ABRUF_FAHRBAHNOBERFLAECHEN_TEMP          2020049
#define UFD_SM_ABRUF_FAHRBAHN_FEUCHTE                   2020050
#define UFD_SM_ABRUF_FAHRBAHN_ZUSTAND                   2020051
#define UFD_SM_ABRUF_RESTSALZGEHALT                     2020052
#define UFD_SM_ABRUF_NIEDERSCHLAG                       2020053
#define UFD_SM_ABRUF_LUFTDRUCK                          2020054
#define UFD_SM_ABRUF_RELATIVE_LUFTFEUCHTE               2020055
#define UFD_SM_ABRUF_WINDRICHTUNG                       2020056
#define UFD_SM_ABRUF_WINDGESCHW_MITTEL                  2020057
#define UFD_SM_ABRUF_SCHNEEHOHE                         2020058
#define UFD_SM_ABRUF_FAHRBAHN_GLAETTE                   2020059
#define UFD_SM_ABRUF_SICHTWEITE                         2020060
#define UFD_SM_ABRUF_HELLIGKEIT                         2020061
#define UFD_SM_ABRUF_NIEDERSCHLAGSMENGE                 2020062
#define UFD_SM_ABRUF_NIEDERSCHLAGSART                   2020063
#define UFD_SM_ABRUF_WINDGESCHW_MAX                     2020064
#define UFD_SM_ABRUF_GEFRIER_TEMP                       2020065
#define UFD_SM_ABRUF_TAUPUNKT_TEMP                      2020066
#define UFD_SM_ABRUF_TIEFE1_TEMP                        2020067
#define UFD_SM_ABRUF_TIEFE2_TEMP                        2020068
#define UFD_SM_ABRUF_TIEFE3_TEMP                        2020069
#define UFD_SM_ABRUF_FBZ                                2020070
#define UFD_SM_ABRUF_NS                                 2020071
#define UFD_SM_ABRUF_WFD                                2020072

/* ======================================================================== */
/* FG 3 - Telegramme mit Daten senden (Wertreaktion) an DE                  */ 
/* ======================================================================== */

#define UFD_DE_SENDEN_KANAL_STEUERUNG                   3002029
#define UFD_DE_SENDEN_UFD_BETRIEBSPARAMETER             3003032 
#define UFD_DE_SENDEN_BETRIEBSPARAMETER                 3003032 
#define UFD_DE_SENDEN_GEO_KENN                          3003036

/* ======================================================================== */
/* FG 3 - Telegramme mit Daten senden (Wertreaktion) an SM                  */ 
/* ======================================================================== */

#define UFD_SM_SENDEN_KANAL_STEUERUNG                   4002029
#define UFD_SM_SENDEN_UFD_BETRIEBSPARAMETER             4003032 
#define UFD_SM_SENDEN_BETRIEBSPARAMETER                 4003032 
#define UFD_SM_SENDEN_GEO_KENN                          4003036

/* ======================================================================== */
/* FG 4 - Abruftelegramme an DE                                             */ 
/* ======================================================================== */

#define WVZ_DE_ABRUF_DE_FEHLER                          1017001 
#define WVZ_DE_ABRUF_NICHT_DARSTELLBARE_WVZ             1017002 
#define WVZ_DE_ABRUF_DEFEKTE_LAMPEN                     1017003 
#define WVZ_DE_ABRUF_GESTOERTE_TEXTPOS                  1017004 
#define WVZ_DE_ABRUF_ERG_DE_FEHLER                      1017014
#define WVZ_DE_ABRUF_BETRIEBSART                        1018017
#define WVZ_DE_ABRUF_PUFFER                             1018020
#define WVZ_DE_ABRUF_CODEDEF                            1018021
#define WVZ_DE_ABRUF_KANAL_STEUERUNG                    1018029
#define WVZ_DE_ABRUF_GRUNDEINSTELLUNG                   1019033 
#define WVZ_DE_ABRUF_GEO_KENN                           1019036
#define WVZ_DE_ABRUF_CODELISTE                          1019043
#define WVZ_DE_ABRUF_CODEDEFINITION                     1019044
#define WVZ_DE_ABRUF_HELLIGKEIT                         1021049
#define WVZ_DE_ABRUF_STELLZUSTAND                       1021055
#define WVZ_DE_ABRUF_SLT_ZULAESSIGE_PRG                 1019120

/* ======================================================================== */
/* FG 4 - Abruftelegramme an SM                                             */ 
/* ======================================================================== */

#define WVZ_SM_ABRUF_DE_FEHLER                          2017001 
#define WVZ_SM_ABRUF_NICHT_DARSTELLBARE_WVZ             2017002 
#define WVZ_SM_ABRUF_DEFEKTE_LAMPEN                     2017003 
#define WVZ_SM_ABRUF_GESTOERTE_TEXTPOS                  2017004 
#define WVZ_SM_ABRUF_ERG_DE_FEHLER                      2017014
#define WVZ_SM_ABRUF_BETRIEBSART                        2018017
#define WVZ_SM_ABRUF_PUFFER                             2018020
#define WVZ_SM_ABRUF_CODEDEF                            2018021
#define WVZ_SM_ABRUF_KANAL_STEUERUNG                    2018029
#define WVZ_SM_ABRUF_GRUNDEINSTELLUNG                   2019033 
#define WVZ_SM_ABRUF_GEO_KENN                           2019036
#define WVZ_SM_ABRUF_CODELISTE                          2019043
#define WVZ_SM_ABRUF_CODEDEFINITION                     2019044
#define WVZ_SM_ABRUF_HELLIGKEIT                         2021049
#define WVZ_SM_ABRUF_STELLZUSTAND                       2021055
#define WVZ_SM_ABRUF_SLT_ZULAESSIGE_PRG                 2019120

/* ======================================================================== */
/* FG 4 - Telegramme mit Daten senden (Wertreaktion) an DE                  */ 
/* ======================================================================== */

#define WVZ_DE_SENDEN_BETRIEBSART                       3002017
#define WVZ_DE_SENDEN_KANAL_STEUERUNG                   3002029
#define WVZ_DE_SENDEN_GRUNDEINSTELLUNG                  3003032 
#define WVZ_DE_SENDEN_GEO_KENN                          3003036
#define WVZ_DE_SENDEN_HELLIGKEIT                        3005049
#define WVZ_DE_SENDEN_STELLZUSTAND                      3005055

/* ======================================================================== */
/* FG 4 - Telegramme mit Daten senden (Wertreaktion) an SM                  */ 
/* ======================================================================== */

#define WVZ_SM_SENDEN_BETRIEBSART                       4002017
#define WVZ_SM_SENDEN_KANAL_STEUERUNG                   4002029
#define WVZ_SM_SENDEN_GRUNDEINSTELLUNG                  4003032 
#define WVZ_SM_SENDEN_GEO_KENN                          4003036
#define WVZ_SM_SENDEN_HELLIGKEIT                        4005055
#define WVZ_SM_SENDEN_STELLZUSTAND                      4005048

#define WVZ_CLUSTER_ABRUF_PUFFER                        5018020


/* ======================================================================== */
/* FG 6 - Abruftelegramme an DE                                             */ 
/* ======================================================================== */

#define VLT_DE_ABRUF_DE_FEHLER                          1017001 
#define VLT_DE_ABRUF_ERG_DE_FEHLER                      1017014 
#define VLT_DE_ABRUF_KANAL_STEUERUNG                    1018029
#define VLT_DE_ABRUF_BETRIEBSPARAMETER                  1019032 
#define VLT_DE_ABRUF_GEO_KENN                           1019036
#define VLT_DE_ABRUF_TUER                               1020048
#define VLT_DE_ABRUF_TEMPERATUR                         1020049
#define VLT_DE_ABRUF_LICHT                              1020050
#define VLT_DE_ABRUF_STROM                              1020051
#define VLT_DE_ABRUF_HEIZUNG                            1020052
#define VLT_DE_ABRUF_LUEFTUNG                           1020053
#define VLT_DE_ABRUF_UEBERSPANNUNG                      1020054
#define VLT_DE_ABRUF_DIEBSTAHL                          1020055
#define VLT_DE_ABRUF_SOLARANLAGE                        1020056

/* ======================================================================== */
/* FG 6 - Abruftelegramme an SM                                             */ 
/* ======================================================================== */

#define VLT_SM_ABRUF_DE_FEHLER                          2017001 
#define VLT_SM_ABRUF_ERG_DE_FEHLER                      2017014 
#define VLT_SM_ABRUF_KANAL_STEUERUNG                    2018029
#define VLT_SM_ABRUF_BETRIEBSPARAMETER                  2019032 
#define VLT_SM_ABRUF_GEO_KENN                           2019036
#define VLT_SM_ABRUF_TUER                               2020048
#define VLT_SM_ABRUF_TEMPERATUR                         2020049
#define VLT_SM_ABRUF_LICHT                              2020050
#define VLT_SM_ABRUF_STROM                              2020051
#define VLT_SM_ABRUF_HEIZUNG                            2020052
#define VLT_SM_ABRUF_LUEFTUNG                           2020053
#define VLT_SM_ABRUF_UEBERSPANNUNG                      2020054
#define VLT_SM_ABRUF_DIEBSTAHL                          2020055
#define VLT_SM_ABRUF_SOLARANLAGE                        2020056

/* ======================================================================== */
/* FG 6 - Telegramme mit Daten senden (Wertreaktion) an DE                  */ 
/* ======================================================================== */

#define VLT_DE_SENDEN_KANAL_STEUERUNG                   3002029
#define VLT_DE_SENDEN_BETRIEBSPARAMETER                 3003032 
#define VLT_DE_SENDEN_BETRIEBSPARAMETER                 3003032 
#define VLT_DE_SENDEN_GEO_KENN                          3003036

/* ======================================================================== */
/* FG 6 - Telegramme mit Daten senden (Wertreaktion) an SM                  */ 
/* ======================================================================== */

#define VLT_SM_SENDEN_KANAL_STEUERUNG                   4002029
#define VLT_SM_SENDEN_BETRIEBSPARAMETER                 4003032 
#define VLT_SM_SENDEN_BETRIEBSPARAMETER                 4003032 
#define VLT_SM_SENDEN_GEO_KENN                          4003036

/* ======================================================================== */
/* FG 8 - Abruftelegramme an DE                                             */ 
/* ======================================================================== */

#define GSW_DE_ABRUF_DE_FEHLER                          1017001 
#define GSW_DE_ABRUF_ERG_DE_FEHLER                      1017014 
#define GSW_DE_ABRUF_KANAL_STEUERUNG                    1018029
#define GSW_DE_ABRUF_BETRPARAMKAMERA                    1019032 
#define GSW_DE_ABRUF_BETRPARAMMESS                      1019033
#define GSW_DE_ABRUF_GEO_KENN                           1019036
#define GSW_DE_ABRUF_BILDZAEHLER                        1020048

/* ======================================================================== */
/* FG 8 - Abruftelegramme an SM                                             */ 
/* ======================================================================== */

#define GSW_SM_ABRUF_SM_FEHLER                          2017001 
#define GSW_SM_ABRUF_ERG_SM_FEHLER                      2017014 
#define GSW_SM_ABRUF_KANAL_STEUERUNG                    2018029
#define GSW_SM_ABRUF_BETRPARAMKAMERA                    2019032 
#define GSW_SM_ABRUF_BETRPARAMMESS                      2019033
#define GSW_SM_ABRUF_GEO_KENN                           2019036
#define GSW_SM_ABRUF_BILDZAEHLER                        2020048

/* ======================================================================== */
/* FG 8 - Telegramme mit Daten senden (Wertreaktion) an DE                  */ 
/* ======================================================================== */

#define GSW_DE_SENDEN_KANAL_STEUERUNG                   3002029
#define GSW_DE_SENDEN_BETRPARAMKAMERA               	3003032 
#define GSW_DE_SENDEN_BETRPARAMMESS                 	3003033
#define GSW_DE_SENDEN_BETRIEBSPARAMETER                 3003032 
#define GSW_DE_SENDEN_GEO_KENN                          3003036

/* ======================================================================== */
/* FG 8 - Telegramme mit Daten senden (Wertreaktion) an SM                  */ 
/* ======================================================================== */

#define GSW_SM_SENDEN_KANAL_STEUERUNG                   4002029
#define GSW_SM_SENDEN_BETRPARAMKAMERA               	4003032 
#define GSW_SM_SENDEN_BETRPARAMMESS                 	4003033
#define GSW_SM_SENDEN_BETRIEBSPARAMETER                 4003032 
#define GSW_SM_SENDEN_GEO_KENN                          4003036

/* ======================================================================== */
/* ENDE                                                                     */ 
/* ======================================================================== */

