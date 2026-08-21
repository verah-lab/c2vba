# Prozessbeschreibung

### Allgemein

- [Name:](http://localhost/ "Der Name des Prozesses.") vmis2-synchronize-vd-app
- [Beschreibung:](http://localhost/ "Optional: Eine kurze Beschreibung, was der Prozess tut.") Applikation, die FG1-Kurzzeitdaten sammelt und synchronisiert und bei Bedarf eine Zeitsychronisation generiert.
- [Verantwortlich:](http://localhost/ "Ein oder mehrere für den Prozess verantwortliche Personen.") Marion Keune, David Hermanns
### Netzwerk

 - [Erreichbarkeit](http://localhost/ "Muss der Prozess von außerhalb [des Clusters] erreichbar sein?  Falls ja, welche zusätzlichen Ports müssen in welche Richtung geöffnet werden?  Dies ist NICHT der Kubernets-Port der App")
   - Notwendig [Ja/Nein]: Nein
   - Zusätzliche Ports:
   
### Abhängigkeiten

- [Prozesse:](http://localhost/ "Welche anderen Prozessen müssen zuerst laufen .") ConfigService und tls-receiver, aber nur als Lieferant der Input-Topics
- [SW-Versionen:](http://localhost/ "Kleinste und höchste erlaubte Versionsnummer von benutzen Tools (z.B. MongoDB)")        
  - GeoManager mindestens: 3.7.0
  - ConfigService mindestens: 1.15.1
- [Ressourcen:](http://localhost/ "Optional: Systemressourcen, die der Prozess benötigt.") Resourcen vom HB-Testsystem =>
            limits:
              cpu: "200m"
              memory: 800Mi
            requests:
              cpu: "100m"
              memory: 500Mi
              
### Konfiguration

- [Parameter:](http://localhost/ "Beschreibung von Parametern, die je nach Zielsystem konfiguriert werden müssen, für erweitertes Logging zum Zwecke der Installationshilfe und weitere für die Integration relevante Einstellungen") siehe Property-Datei
  - Parameter 1:
  - Parameter 2:
  - ...
- [Kafka-Topics:](http://localhost/ "Für einen ersten Vergleich mit der Kafka-Konfiguration.") siehe Property-Datei
  - Topic 1:
  - Topic 2:
  - ...
- [Property-Datei:](http://localhost/ "Sollten Paramter oder Topis systemabhängig sein, dann Angabe der beinhaltenden Property-Datei.")
  - Pfad: https:https://gitlab.heuboe.hbintern/VMIS2/kubernetes/kubernetes-config/-/blob/hb-testsystem/vmis2/plausibility-substitution-vd-app/configs/application.properties
  - Name: application.properties
  
  
### Daten

- [Festplatte/PVC:](http://localhost/ "Wird nicht-flüchtiger Speicher benötigt? Wie groß muss der Speicher sein?  Nein
     Müssen die erzeugten Daten zyklisch gesichert werden?
     Woher kommen evtl. initial vorhandene/notwendige Daten?")
     - Notwendig [Ja/Nein]: Nein
     - Größe:
     - Zyklische Sicherung [Ja/Nein]:
     - Quelle initial notwendiger Daten: 
- [Datenbank:](http://localhost/ "Müssen Daten vorversorgt werden? Wo sind diese Daten zu finden? Müssen DB-Inhalte zyklisch gesichert werden? Trifft einer der Punkte zu: Typ der Datenbank und Zugang (z.B. Welche Collection einer MongoDB muss gesichert werden?)") Nein
     - Datenbank: 
     - Datenbankreferenz (Tabelle/Collection):
     - Zyklische Sicherung [Ja/Nein]:
     - Quelle initial notwendiger Daten: 



