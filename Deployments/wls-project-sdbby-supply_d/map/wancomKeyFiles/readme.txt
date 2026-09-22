Grundlage der UZ-Daten-Statistiken sind die TLS-Tagesmitschnitte des tls-tele-recorder im PVC: /mnt/pvc/c2vba/tls-data/tele-recorder


Eine Tages-Datei kann mit den Tools in

https://gitlab.heuboe.hbintern/c2vba/loc-dist-node-script

in eine fg1-loc-dist-de-Schlüssel-Extrakt-Datei konvertiert werden



Über seine REST-Schnittstelle kann der WLS zur fg1-loc-dist-de-Schlüssel-Extrakt-Datei eine UZ-Statistik der Datenlieferungen erstellen:


http://<host>:<port>/wancomStatistics?wancomKeyFile=<fileName>&fg=<fg>&format=<format>


<fg>: 1, 3, 4

<format>: csv, table   default: csv

<fileName>: Etwa by-220831-020000.nodeDe.txt, Datei muss in diesem Verzeichnis liegen


Beispiele:
----------

Entwicklungsumgebung:   http://localhost:4795/wancomStatistics?wancomKeyFile=c2vba_node_de_220725.txt&fg=1&format=table



DEV-System:             http://master1.d.c2vba.heuboe.hbintern:32393/wancomStatistics?wancomKeyFile=20250628-000000.txt&fg=4&format=table



Problem: Chrome (und Firefox) wollen partout nur noch https-URLs (nicht http). Das kann man abstellen oder Edge verwenden.
Abtellen unter Chrome: "Go to chrome://net-internals/#hsts. Enter master1.d.c2vba.heuboe.hbintern under 'Delete domain security policies' and press the Delete button."

  

