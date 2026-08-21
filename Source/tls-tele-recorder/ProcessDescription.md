# Prozessbeschreibung

### Allgemein

- [Name:](http://localhost/ "Der Name des Prozesses.") tls-tele-recorder
- [Beschreibung:](http://localhost/ "Optional: Eine kurze Beschreibung, was der Prozess tut.") Applikation zum Aufzeichnen von Telegrammen in Empfangs- und Sendrichtung in Binärform in eine Datei.
- [Verantwortlich:](http://localhost/ "Ein oder mehrere für den Prozess verantwortliche Personen.") Alexander Schulze

### Netzwerk

- [Erreichbarkeit](http://localhost/ "Muss der Prozess von außerhalb [des Clusters] erreichbar sein?  Falls ja, welche zusätzlichen Ports müssen in welche Richtung geöffnet werden?  Dies ist NICHT der Kubernets-Port der App")
    - Notwendig [Ja/Nein]: Nein (kann aber, da er eine Schnittstelle zum streamen bzw. Abfragen der Telegramme ermöglicht)
    - Zusätzliche Ports: konfigurierbar wenn notwendig

### Abhängigkeiten

- [Prozesse:](http://localhost/ "Welche anderen Prozessen müssen zuerst laufen .")
- [SW-Versionen:](http://localhost/ "Kleinste und höchste erlaubte Versionsnummer von benutzen Tools (z.B. MongoDB)")
    - Kafka 2.x
- [Ressourcen:](http://localhost/ "Optional: Systemressourcen, die der Prozess benötigt.")
  ```
  resources:
    limits:
      cpu: "800m"
      memory: 500Mi
    requests:
      cpu: "300m"
      memory: 400Mi
  ```

### Konfiguration

- [Parameter:](http://localhost/ "Beschreibung von Parametern, die je nach Zielsystem konfiguriert werden müssen, für erweitertes Logging zum Zwecke der Installationshilfe und weitere für die Integration relevante Einstellungen")
    - Siehe Property-Datei.
- [Kafka-Topics:](http://localhost/ "Für einen ersten Vergleich mit der Kafka-Konfiguration.")
    - TeleReceived
    - TeleToSend
- [Property-Datei:](http://localhost/ "Sollten Paramter oder Topis systemabhängig sein, dann Angabe der beinhaltenden Property-Datei.")
    - Umgebungspezifisch Verbindungsparameter zum Kafka-Broker
    - Topic für AlarmMessage
    - Topic Prefix und Suffix für Empfangs-Topic
    - Topic Prefix und Suffix für Sende-Topic

### Daten

- [Festplatte/PVC:](http://localhost/ "Wird nicht-flüchtiger Speicher benötigt? Wie groß muss der Speicher sein?
  Müssen die erzeugten Daten zyklisch gesichert werden?
  Woher kommen evtl. initial vorhandene/notwendige Daten?")
    - Notwendig [Ja/Nein]: Ja
    - Größe: ein paar Gigabyte (abhängig von der Konfiguration der Log Anzahl und Intervalllänge für Dateirotation)
    - Sicherung: nicht notwendig
    - Ursprung: Dateien werden vom Prozess erstellt, keine Bereitstellung von Dateien notwendig
- [Datenbank:](http://localhost/ "Müssen Daten vorversorgt werden? Wo sind diese Daten zu finden? Müssen DB-Inhalte zyklisch gesichert werden? Trifft einer der Punkte zu: Typ der Datenbank und Zugang (z.B. Welche Collection einer MongoDB muss gesichert werden?)") Nein
    - Datenbank: Nein
