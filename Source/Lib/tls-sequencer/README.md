[![Quality Gate Status](https://sonar.heuboe.hbintern/api/project_badges/measure?project=de.heuboe.tls%3Atls-sequencer&metric=alert_status&token=sqb_2f37f66acfa026ea48c1d66ea62e46561f73077b)](https://sonar.heuboe.hbintern/dashboard?id=de.heuboe.tls%3Atls-sequencer)
[![Coverage](https://sonar.heuboe.hbintern/api/project_badges/measure?project=de.heuboe.tls%3Atls-sequencer&metric=coverage&token=sqb_2f37f66acfa026ea48c1d66ea62e46561f73077b)](https://sonar.heuboe.hbintern/dashboard?id=de.heuboe.tls%3Atls-sequencer)
[![Security Hotspots](https://sonar.heuboe.hbintern/api/project_badges/measure?project=de.heuboe.tls%3Atls-sequencer&metric=security_hotspots&token=sqb_2f37f66acfa026ea48c1d66ea62e46561f73077b)](https://sonar.heuboe.hbintern/dashboard?id=de.heuboe.tls%3Atls-sequencer)

# TLS Sequencer

## Funktionsweise

Der TLS Sequencer soll auf Basis äußerer Ereignisse Sequenzen von Aktionen und Telegrammen einleiten. Dabei werden
Datensätze nach bestimmten Vorgaben analyisiert und entsprechende Folgeaktionen ausgeführt.

---

## Konfiguration
Für die Konfiguration des Sequencers steht ein
[Template für eine application.yml](src/main/resources/templates/config/application.yml) bereit. In dieser sind die
relevanten Konfigurationen mittels Kommentaren erklärt.

Damit der Sequencer korrekt funktioniert, muss er mit entsprechenden Skripten intialisiert werden. Der Ablageort der 
Skripte wird in der Property `de.heuboe.tls.sequencer.script.path` angegeben. Dabei muss es sich um eine absolute 
Pfadangabe handeln. Sämtliche Skripte die in dem angegebenen Pfad liegen, werden immer vom Sequencer geladen, solang sie
der weiter unten beschriebenen Namensgebung folgen.

### Skript Name
Es ist zwinged notwendig, dass die Dateinamen der Sequencerskripte einer entsprechenden Syntax folgen, damit diese vom
Sequencer erkannt werden. Folgende Regeln sind dabei zu beachten:
* Das Skript muss mit `seq-uz_` beginnen.
* Das Skript muss mit `.txt` enden.

### spezifische Skripte
TODO -> Verwendung und Priorisierung von Skripten mit System und UZ Namen beschreiben
* seq-uz_${STAGE_NAME}.txt
* seq-uz_${SYSTEM_NAME}.txt
* seq-uz_\${SYSTEM_NAME}-${STAGE_NAME}.txt

### Test Skripte
TODO -> Verwendung und Priorisierung der Test Skripte beschreiben

## Phasen
Der Sequencer arbeitet in zwei wesentlichen Phasen.

### Initialisierungsphase
In dieser Phase werden die konfigurierenden Skripte eingelesen. Darüber hinaus werden alte Objekte der jeweiligen Topics
aus dem Compaction Log gelesen und im Speicher vorgehalten.

### Laufzeitphase
In dieser Phase werden Datensätze der konfigurierten Topics entgegengenommen, und gemäß der Konvertierungsvorschrift in
neue Datensätze umgewandelt.

---

## Zustand von Objekten
Es ist möglich, auf unterschiedliche Zustände von Objekten zu zugreifen. Dabei werden jeweils 2 Zustände unterstützt.
Ein *neuer* und ein *alter* Zustand.
 
### Definition *Neu*
Ein Objekt gilt als *Neu*, wenn es auf einer angemeldeten Topic aus dem Kafka empfangen wurde und den aktuellen Zustand
eines TLS Objektes darstellt.

### Definition *Alt*
Ein Objekt gilt als *Alt*, wenn es aus dem

### Kafka Compaction Log
Im Kafka Compaction Log werden alte Zustände von Objekten gehalten. Diese werden in der Initialisierungsphase des
Sequencers initial geladen. Im Laufe des Betriebs werden aktualisierte Einträge im Compaction Log stehts abgegelichen.

---

## Config Service
Der Sequencer verwendet Informationen vom Config Service um bestimmte Aufgaben zu erfüllen. Darunter gehört u.a. die
Ausführung bestimmter Funktionen, die Geräte auf bestimmte Bedingungen prüft.

---

## Syntax für Skripte

### Block Definitionen
Für jedes zu behandelnde Topic muss ein eigener Block definiert werden. Ein Block muss mit dem Kommando `Bei Eingang 
von` starten, worauf dann der Name der Topic folgt, für welche dieser Block gilt. Für jeden Block können optional
Eigenschaften definiert werden. Diese werden hinter dem Namen der Topic in Klammern geschrieben. Innerhalb der Klammern
können beliebig viele Eigenschaften als Komma separierte Liste in Form von `property=value` Einträgen definiert werden.
Dabei sind nur definierte Eigenschaften erlaubt.   

```
Bei Eingang von DaWVZStekkzustandABIst (self=true)
{

}
```


### Block Definition Eigenschaften
Derzeit werden folgende Eigenschaften unterstützt:
* `self=boolean`: Definiert, ob für diese Topic auf Sequencer generierte Nachrichten reagiert werden soll. Jeder Wert,
  der nicht `true` (unabhängig von Groß- / Kleinschreibung) ist, wird als `false` interpretiert.
* `name="String"`: Definiert den Namen des Skript Blocks. Dieser Name wird im Log beim Versenden eines Objektes durch
  den Sequencer ausgegeben. Das kann hilfreich sein, um einen besseren Überblick über die Abläufe in den Sequencer 
  Skripten zu erhalten.
* `autoFillTlsTime=boolean`: Definiert, ob für die in diesem Block erzeugten Objekte automatisch die aktuelle Zeit als
  `tlsTime` gesetzt werden soll. Das greift dann, wenn diese nicht explizit über eine Variablenzuweisung gesetzt wird
  oder nicht im Quell-Objekt (bei der Verwendung von [copy](#objektkopien)) vorhanden ist. 

### Operatoren
Für die Operationen zwischen Operanden stehen verschiedene Operationen zur Verfügung. Im folgenden sollen diese
Kategorisiert aufgelistet werden. Es wird darauf verzichtet, die Funktionsweise der Operationen zu erklären, da sich
diese nach der jeweiligen allgemein gültigen Funktionsweise richtet. 

#### Arithmetische Operationen
* \+
* \-
* \*
* /
* %

 
#### Bit Operationen
* \<\<
* \>\>
* &
* |
* ^

#### Relationale Operationen
* \>
* \<
* \>=
* \<=
* ==
* !=
 
#### Logische Operationen
* && 
* || 


### Variablen
Innerhalb der Skripte können verschiedene Typen von Variablen definiert und verwendet werden.

#### Objekt Variablen
Die Objekt Variablen sind Parameter eines Objektes, welches die entsprechende Block Definition ausgelöst hat. 
```
if (stellcode == 100) {
  ...
}
```

#### Objekt Array-Variablen
Neben dem Zugriff auf normale Variablen, ist auch der Zugriff auf Array Variablen möglich. Dabei muss der entsprechende
Index angegeben werden.
```
if (vGrenzen[1] == 100) {
  ...
}
```

#### Objekt Variablen eines vorherigen Zustandes
Neben dem Zugriff auf Attribute des aktuellen Objektes der zu prüfenden Datenart, kann auch auf den vorherigen Zustand
einer beliebigen Datenart zugegriffen werden. Dabei muss die Datenart zusammen mit dem Kommando `*Old` angegeben werden. 
```
If (UFDLufttemperatur*Old.messwert == 80) {
  ...
}
```

#### spezielle Variablen
Für jedes Objekt stehen neben seinen eigenen Parametern noch weitere Informationen zur Verfügung. Diese können mittels
`$` Zeichen und dem entsprechenden Namen abgerufen werden.
```
if ($Eaid == 2002479) {
  ...
}
```

Folgende Variablen sind dabei verwendbar:<br>
* `DeNummer` - Die DeNummer des aktuellen Objektes
* `Eaid` - Die numerische Repräsentation der EA Id des aktuellen Objektes
* `NodeId` - Die ID des Knoten zu dem das aktuelle Objekt gehört 
* `ClusterId` - Die ID des Clusters zu dem das aktuelle Objekt gehört

#### Globale Variablen
Innerhalb eines Skriptes können globale Variablen verwendet werden, die über Block Definitionen hinaus verwendbar sind.
Diesen globalen Variablen können Werte zugewiesen oder Werte abgerufen werden, um sie z.B. in einem If-Statement oder
bei einer Wertezuweisung zu verwenden.

Dazu müssen vor der ersten Block Definition, die im Skript zu verwendenden globalen Variablen mit dem Kommando `Global`
definiert werden. Dabei muss der Variablenname mit einem Buchstaben oder Unterstrich beginnen. Danach sind Buchstaben,
Zahlen und Unterstriche erlaubt.

```
Global G80_AN_AQ_169_A1
Global G80_AN_AQ_169_A2
Global G80_AN_AQ_169_A3

Bei Eingang von ...
``` 

Um einer globalen Variable Werte zu zuweisen, wird die Syntax einer normalen Zuweisung mit Aufruf der Variable und 
vorstehendem `$` Zeichen verwendet.
```
$G80_AN_AQ_169_A1 := 10
```

Die Abfrage von Inhalten einer globalen Variable erfolgt entsprechend der Verwendung lokaler Objekt Variablen.
```
If ($G80_AN_AQ_169_A1 > 20) {
  messwert := $G80_AN_AQ_169_A1
}
```

#### Vordefinierte Variablen aus spec.yaml
Neben globalen Variablen, die nur innerhalb eines Skriptes gütlig sind, dort aber dynamisch gesetzt werden können, gibt
es noch fest vordefinierte und nicht veränderbare Variablen. Diese werden in der Datei `spec.yaml` definiert und stehen
in jedem Skript zur Verfügung.

```
GLOBALE_ZEITSYNCHRONISATION = "2002018"
```

---

## Aktionen
Es gibt verschiedene Aktionen, die in Sequencer Skripten angewandt werden können.

### If-Bedingungen
Eine If-Bedingung wird als If Block, bzw. If-Else Block definiert.

```
if (condition)
{
  ...
} else {
  ...
}
```
Die If-Bedingung selbst besteht aus zwei Operanden die über einen Operator verbunden sind. Die If-Bedingungen können
beliebig geschachtelt werden. Als Operanden dienen neben fest spezifizierten Variablen auch freie Zahlenwerte.
Zeichenketten werden derzeit nicht unterstützt!

_Beispiele:_
Hier wird auf Basis des Attributes `fehlercode` eine Bit Operation auf der Spezifizierung `DE_STOER_BITS` durchgeführt
und anschließend auf Gleichheit mit der Spezifizierung `DE_GUT_MELDUNG` verglichen.
```
if ( (fehlercode & DE_STOER_BITS) == DE_GUT_MELDUNG) {
  ...
}
```

### Switch-Case-Bedingungen
TODO

_Beispiele:_

TODO

### Objektmanipulation
Es kann auf verschiedene Arten ein Eingangsobjekt manipuliert, oder gar vollständig neue Objekte erzeugt werden.

#### Änderung von Werten eines Objektes
Hier wird das Attribut `messwert` des Objektes `UFDRestsalz` auf einen Wert kleiner 80 geprüft. Wenn die Prüfung wahr
ergibt, wird der Wert des Attributes `messwert` auf den Wert 100 gesetzt.  
```
Bei Eingang von UFDRestsalz
{
  If (messwert < 80) {
    messwert := 100
  }
}
```
**Achtung** zu Array Variablen können keine neuen Werte zugewiesen werden. 

### Erzeugung eines neuen Objektes
Es können gänzlich neue Objekte von anderen Datenarten erzeugt werden. Dazu muss einfach nur der Objektname und die 
Felder mit den entsprechenden Wertezuweisung in der Syntax `object.field := value` angegeben werden. Die Id
(also das Ziel der neuen Nachricht) für das neu erzeugte Objekt wird standarmäßig aus dem auslösenden Objekt übernommen.
Für die Anpassung der Id gibt es die folgenden verschiedene Möglichkeiten:
* `Ea("") in` sendet Nachricht an die in den Klammern definierte Id, wobei die Id optional ist. Sollte keine Id 
   angegeben sein, wird die Id des auslösenden Objektes verwendet.
* `DEs des Cluster ("...")` sendet Nachricht an alle DEs die unterhalb des Clusters mit der in den Klammern definierten 
  Id hängen und an das Cluster selbst
* `DEs des Knoten ("...") fg(...)` sendet Nachricht an alle DEs die unterhalb des Knoten mit der in den Klammern 
  definierten Id hängen und zu der zugehörigen Funktionsgruppe gehören.
* `DEs der KRI ("...") fg (...)` sendet Nachricht an alle DEs die unterhalb der KRI mit der in den Klammer definierten 
  Id hängen  und zu der zugehörigen Funktionsgruppe gehören.
* `Cluster des DEs ("...")` sucht zu der in den Klammern definierten Id das übergeordnete Cluster und sendet Nachricht 
  an dieses
* `Knoten des DEs ("...")` sucht zu der in den Klammern definierten Id den übergeordneten Knoten und sendet Nachricht an
  diesen
* `Knoten der KRI ("...")`: Sendet Nachricht an alle Knoten vom Typ Streckenstation die unterhalb der KRI mit der in den
  Klammern definierten Id hängen. Sollte die definierte Id nicht zu einem Gerät vom Typ KRI gehören, wird keine 
  Nachricht versandt.

Alle Optionen können auch ohne die Klammerung und Defintion einer festen Id verwendet werden. In diesem Fall wird die Id
des Eingangsobjektes verwendet.

_Beispiele:_
```
Ea ("MQ_123") in Steuersequenz.action via out.SYSSteuerSequenz := 1
Ea in Steuersequenz.action via out.SYSSteuerSequenz := 1
DEs des Cluster ("MQ_123") in Steuersequenz.action via out.SYSSteuersequenz := 1
DEs des Cluster in Steuersequenz.action via out.SYSSteuersequenz := 1
DEs des Knoten ("MQ_123") in Steuersequenz.action via out.SYSSteuersequenz := 1
DEs des Knoten in Steuersequenz.action via out.SYSSteuersequenz := 1
Cluster des DEs ("MQ_123") in Steuersequenz.action via out.SYSSteuersequenz := 1
Cluster des DEs in Steuersequenz.action via out.SYSSteuersequenz := 1
Knoten des DEs ("MQ_123") in Steuersequenz.action via out.SYSSteuersequenz := 1
Knoten des DEs in Steuersequenz.action via out.SYSSteuersequenz := 1
```

### Ziel Topic eines Objektes verändern
Im Normalfall werden Objekte, die durch den Sequenzer manipuliert bzw. erzeugt werden, an das entsprechende Kafka Topic
gesendet, welches sich aus dem Namen des Objektes und dem konfigurierten Topic Prefix und Topic Suffix ableitet.  Das 
Ziel Topic eines Objektes lässt sich jedoch auf folgende Arten manipulieren.

#### Anpassung des Topic Prefix und Suffix
In der Konfiguration können jeweils Prefix und Suffix für die Sende- und Empfangsrichtung von Daten definiert werden.
Dafür stehen die folgenden Schlüsselwörter zur Verfügung:
* `in` - Verwendet Prefix und Suffix in Empfangsrichtung (Standard: wird verwendet wenn kein Schlüsselwort angegeben 
  ist)
* `out` - Verwendet Prefix und Suffix in Sendereichtung

Die Verwendung der Schlüsselwörter ist überall dort möglich, wo eine Datenart definiert werden kann. Die Verwendung ist
Optional, wobei der Standard immer die Empfangsrichtung ist.

_Beispiel:_
```
Bei Eingang von out.WZGStellzustandSoll {
    in.WZGDeFehler.folgenummer := 1
    in.WZGDeFehler.fehlercode := 2
    in.WZGDeFehler.hersteller := 3
}
```

#### Änderung des kompletten Ziel Topics
Falls es für eine Datenart mehrere Topics gibt, so kann das Ziel Topic im Skript definiert werden. Dafür kann nach einer
Datenart mit dem Schlüsselwort `via` das Ziel Topic angegeben werden.

_Beispiel:_
```
Bei Eingang von SteuerSequenz via out.SYSSteuerSequenz {
    if (action == LOKALE_ZEITSYNCHRONISATION) {
        SteuerSequenz.action via out.WZGSteuerSequenz := GLOBALE_ZEITSYNCHRONISATION
    }
}
```

### Objektkopien
Es besteht die Möglichkeit, ein Objekt, dass auf einer definierten Topic empfangen wird, auf eine andere Topic zu 
kopieren. Dabei muss das Ziel mit dem Datentyp des empfangenen Objektes übereinstimmen. Sonst wird kein Kopiervorgang
durchgeführt.

```
Bei Eingang von WZGStellzustandSoll {
    copy to WZGStellzustand
}
``` 

### Funktionen
Es gibt bestimmte vordefinierte Funktionen, die innerhalb des Skriptes verwendet werden können. Im folgenden sollen
diese beschrieben werden:

#### isKri
Mit dieser Funktion kann geprüft werden, ob die übergebene Id zu einer KRI gehört.

#### getAtIndex
Realisierung des Index-Operators als (partielle) Funktion. Zwei Argumente werden erwartet:
* `array`: Das zu verwendene Array.
* `index`: Der Index.

getAtIndex( array, index ) ~ array[index]

Der Grund hierfür liegt in der modellierenden Grammatik und wo sie realisiert wird.

```
LVEDeFehler.fehlercode via out.LVEDeFehler := getAtIndex( vKlassenPkwAe, 0 )
```

*Parameter:* Es wird exakt ein Parameter für diese Funktion erwartet. Dabei sollte es sich idealerweise um eine ID eines
Endgerätes handeln.

*Beispiel:*
```
If (isKri("KRI_ID")) {
  ...
}
```

#### dateTime
Diese Funktion liefert ein Datum und eine Uhrzeit in der UTC Zeitzone. Diese kann als Zuweisung für eine Variable
verwendet werden.

*Parameter:* Es kann entweder kein, oder exakt ein Parameter übergeben werden. Wenn kein Parameter übergeben wird, wird
ein aktuelles Datum und Uhrzeit erstellt. Wenn ein Parameter übergeben wird, wird dieser als Datum und Uhrzeit geparsed.
Dafür ist es notwendig, folgendes Format zu verwenden: `YYYY-MM-DDTHH:mm:ssZ`

*Beispiel:*
```
WZGNegativeQuittung.processTime := dateTime()
WZGNegativeQuittung.tlsTime := dateTime("2024-11-07T10:00:00Z")
```

**Hinweis**: Die Funktion `dateTime` hat keine Auswirkung auf das Feld `processTime`. Dieses wird immer durch den
Sequencer mit einem aktuellen Zeitstempel gefüllt.

### Monoflops
Monoflops sind Timer gesteuerte Ausführungen von Anweisungen. Dabei sind verschiedene Arten von Monoflops vorhanden, die
sich auf das auslösende Objekt beziehen. Die Zeitangabe bei Flops ist immer in Millisekunden.

#### retriggerbar
Jeder Monoflop ist optional retriggerbar. Das bedeutet, dass der Timer eines Monoflops neugestartet werden kann, bevor
er abgelaufen ist. Dabei wird jedoch eine maximale Timer Zeit berücksichtig, nachdem der Timer spätestens ausgelöst
wird, egal wie oft dieser retriggert wurde. Der Befehl für die Retrigger Option ist `retriggerbarer` und muss zwischen
dem Typen und Monoflop stehen.
```
einmaliger retriggerbarer Monoflop ( 5, 15 )
{
  Ea("1234) in out.SteuerSequenz.action via SYSSteuerSequenz := GLOBALE_ZEITSYNCHRONISATION
}
```

##### Einmaliger Monoflop
Dieser Monoflop gilt für eine eindeutige Datenart. Somit kann jedes E/A, welches die entsprechende Datenart sendet den
Monoflop auslösen. Der Befehl für diesen Monoflop lautet `einmaliger Monoflop`.
```
einmaliger Monoflop ( 20 )
{
  Ea("1234) in out.SteuerSequenz.action via SYSSteuerSequenz := 1
}
```
Innerhalb einer Datenart kann es mehrere Monoflops geben. Dabei werden diese nach ihrer konfigurierten Auslösezeit 
verwaltet. Das folgende Beispiel würde 2 Monoflops erzeugen und ausführen. Dabei würde nach 20 Millisekunden ein
SteuerSequenz Objekt mit der `id = 1234` und `action = 1` auf das Topic `SYSSteuerSequenz` geschrieben werden. Nach 40 
Millisekunden wird dann ein SteuerSequenz Objekt mit der `id = 5678` und `action = 2` auf das Topic `SYSSteuerSequenz` 
geschrieben. Der Auslösezeitpunkt startet für alle Monoflops zu dem Zeitpunkt, an dem die Eingangsnachricht verarbeitet
wurde und der entsprechende Block ausgeführt wird.

```
Bei Eingang von WZGDeFehler
  einmaliger Monoflop ( 20 )
  {
    Ea("1234) in out.SteuerSequenz.action via SYSSteuerSequenz := 1
  }
  
  einmaliger Monoflop ( 40 )
  {
    Ea("5678) in out.SteuerSequenz.action via SYSSteuerSequenz := 2
  }
```

**Achtung:** Da Monoflops nach ihrere Auslösezeit verwaltet werden, würde die folgende Anweisung nicht 2 Monoflops 
erzeugen, sondern der zweite Block, den Ausführungsblock des ersten Monoflops überschreiben.
```
Bei Eingang von WZGDeFehler
  einmaliger Monoflop ( 2 )
  {
    Ea("1234) in out.SteuerSequenz.action via SYSSteuerSequenz := 1
  }
  
  einmaliger Monoflop ( 2 )
  {
    Ea("5678) in out.SteuerSequenz.action via SYSSteuerSequenz := 2
  }
```

##### Eaweiser Monoflop
Dieser Monoflop gilt für ein eindeutiges E/A. Somit kann nur von diesem E/A der Monoflop ausgelöst werden. Der Befehl
für diesen Monoflop lautet `eaweiser Monoflop`.

##### Clusterweiser Monoflop
Dieser Monoflop gilt für einen eindeutigen E/A Konzentrator und alle seine zugehörigen E/A's. Somit können alle E/As des
Clusters den Monoflop auslösen. Der Befehl für diesen Monoflop lautet `clusterweiser Monoflop`.

##### Knotenweiser Monoflop
Dieser Monoflop gilt für ein eindeutiges Steuermodul und alle seine zugehörigen E/A Konzentratoren und deren E/A's.
Somit können alle E/A's des Knoten den Monoflop auslösen. Der Befehl für diesen Monoflop lautet `knotenweiser Monoflop`.

##### Anwendung
Der Skript Befehl sieht die Übergabe von einem bis zwei Parametern vor, abhängig davon, ob der Monoflop retriggerbar
sein soll.
1. *maxTriggerTime:* Definiert, nach wievielen Millisekunden die Aktion spätestens ausgeführt werden soll.
2. *maxRetriggerTime:* Definiert das maximale Zeitlimit in Millisekunden, nachdem eine Aktion trotz retrigger ausgeführt 
   werden soll.
   
```
einmaliger retriggerbarer Monoflop ( maxTriggerTime, maxRetriggerTime )
{
  SteuerSequenz.action := GLOBALE_ZEITSYNCHRONISATION
}
```

### Schreiben von Log Nachrichten
Es besteht die Möglichkeit eine individuelle Benachrichtigung für das Log hinzuzufügen.
Dafür gibt es zwei verschiedene Arten von Nachrichten:
* `SystemMessage`: Schreibt die Nachricht auf `INFO` Level in das Log.
* `ErrorMessage`: Schreibt die Nachricht auf `ERROR` Level in das Log und sendet dieselbe Nachricht auch an das  
  Meldungsmanagement

Die Nachricht selbst besteht aus einer Zeichenkette die Platzhalter unterstützt. Als Platzhalter muss die Zeichenkette
`{}` verwendet werden. Nach der eigentlichen Zeichenkette, können die Inhalte der Platzhalter definiert werden (z.B.
Variablen).
```
SystemMessage ("ZeitSync durch {}", $Eaid)
```

Sollte die Anzahl von Platzhaltern und Variablen unterschiedlich sein, wird die Nachricht nicht gesendet, sondern eine
Fehlermeldung ins Log geschrieben.

---
---

### offene Fragen
* was ist diese DeNummer (Nummer des Daten-Endgerätes) und EAID?!
  * DeNummer (Daten-Endgeräte Nummer):
    * ???
    * über 193 -> Clusterkanäle
  * EAID (Ein-/Ausgabegeräte ID):
    * Identifikator einer kleinsten Infomations emfpangenden / sendenen Einheit -> z.B. Wechselzeichen oder Sensor 
    * z.B. 2017001, siehe spec.yaml bzgl. Zusammensetzung
* woran wird festgemacht, welche UZ aufgerufen wird? Wird das durch die DeNummer bestimmt? Oder doch eher die EAID?

### Hinweise
* Topic -> vor dem Start lesen in Init Phase
  * pro Key mind. einen Datensatz
  * Datensatz kann man löschen, indem man Key null sitzt
  * Datensätze mit null können kommen, dann muss der Datensatz gelöscht werden (im Sequencer)
* Kafka eine Topic und mehrere Partitions
* Nutzung der `vmis2-tls-cfglib` (https://gitlab.heuboe.hbintern/VMIS2/tls/vmis2-tls-cfglib), welche mit Daten vom
  `ConfigService` initalisiert wird.
  * z.B. für Ermittlung einer `DeNummer` anhand einer `EAID`
* Cluster Kanäle sind `DeNummern` über 193
* es sollten Teile aus `seq.g` verwendet werden (`contrib/resources/gdSqu`)
  * pushContext / popContext -> wie z.B If Block in Java, wo neu definierte Variablen nur innerhalb des Kontextes gültig
    sind
* Bedeutung von `Ea(8436497) in DaTriggerSchaltzustand.schaltzustand := 1`:
  * erzeugt Objekt vom Typ `DaTriggerSchaltzustand` und setzt das Attribut `schaltzustand` auf den Wert 1
  * setzt die `id` des erzeugten Objektes auf Basis von `EA(2003876)` -> definiert das Ziel des Objektes (in dem Fall 
    immer eine KRI)
    * über `Osi7Cfg` die String ID für `eaid=8436497` raussuchen (z.B. `KRI_2B_ABM_K_VBA_U`)
    * `Osi7Cfg` enthält Map `eaPermId2DevMap`, welche das Mapping einer EAID auf eine DeNummer enthält. Darauf kann mit
      `getDeviceOfEa(String EAID)` zugegriffen werden.

## Backlog
- Timeout pro DE via Script implementieren -> kein Thread.sleep, sondern ein Timer Event starten