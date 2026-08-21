# BY-Config-Reader
Dieses Tool liest die TLS-Konfiguration von Bayern für C2VBA ein.

## Ressourcen
Die passende Konfiguration liegt in dem GIT Projekt: https://gitlab.heuboe.hbintern/tls-konfiguration/bayern/c2vba
Dort liegen die originalen Zulieferungen als Excel und die für den Import aufbereiteten Dateien. Zusätzlich enthält das Projekt weitere Dateien die für den Import nötig sind.

## Konfiguration
Um das Tool erfolgreich starten zu können muss dieses noch entsprechend konfiguriert werden.
### application.properties
- configService Adresse
- kriFile, findet sich in dem Konfigprojekt (Export_20221013_hb.xlsx)
- wwwDir, findet sich in dem Konfigprojekt (www_v1.0.txt)

### run.bat
- configDir, das "aubereiotet"-Verzeichnis in dem Konfigprojekt
- applicationProperties, die vorher konfigurierte application.properties