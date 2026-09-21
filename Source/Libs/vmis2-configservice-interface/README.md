# VMIS2 Config Service gRPC Interface

**Table Of Contents**

* [About](#about)
* [Features](#features)
* [Diskussionspunkte](#diskussionspunkte)


## About

Dieses Projekt ist ein erster Diskussions-Vorschlag für das Interface des VMIS2 Config Service.
Die relevanten Files finden sich in 
* [ConfigService.proto](../src/main/proto/ConfigService.proto)
* [ConfigData.proto](../src/main/proto/ConfigData.proto)

## Features

Das Projekt enthält zu Testzwecken rudimentäre Server- und Client-Implementierungen: 
* [TestServer](../src/test/java/de/heuboe/asfinag/vmis2/configservice/system/TestServer.java)
* [TestClient](../src/test/java/de/heuboe/asfinag/vmis2/configservice/system/TestClient.java)

Das sind jeweils einfache Java Applikationen, die z.B. aus der IDE im Debugger gestartet werden können.


## Diskussionspunkte

### Denglish
Die Bezeichnungen EQ und AQ scheinen weitgehend akzeptiert zu sein (oder doch eher MQ ? :) ).
Nicht so dolle find' ich 

* WsSensor: UQ passt nicht (ist kein Querschnitt), FG3 wolln wir wegen 'kein TLS' nicht, und 'EnvironmentSensor' ist
etwas lang...

* VltSensor: Ähnlich.

Fällt jemand da etwas besseres ein ?

### Typ-Enums 
* AQ und WVZ: Da habe ich aus der alten ZSS übernommen, was mir am sinnvollsten erschien. Brauchen wir noch mehr ?
* WsSensor/VltSensor: Das sind erst mal Strings, wegen der Fülle der möglichen Typen. Lohnt es sich, dafür auch Enums 
zu definieren ? Dann erfordert jeder neue Sensor-Typ eine Software-Änderung... wolln wir das ? Wenn nicht, wie definieren
wir dann die möglichen Typen ?

### Schnittstellen
Bei getUzs(...) kann man alle bekannten Objekte abfragen; für die anderen 'get' -Funktionen hab' ich das erst mal nicht vorgesehen
(das könnten ziemlich viele werden). Braucht man so etwas ? Ev. gefiltert nach Typen ?


