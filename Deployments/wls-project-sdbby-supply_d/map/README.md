# wls-project-sdbby-supply_D

## Einleitung

Der Inhalt des Verzeichnisses **map** ist Bestandteil der Versorgung des wls-project-sdbby. Er umfasst einmal eine vollständige Karte (OSM-Karte, Kilometrierung, LCL und Betriebsmittel), die beim Erst-Start in einem System verwendet wird. Daraus wird eine MongoDB-Repräsentation erzeugt, die bei darauffolgenden Starts gelesen wird.  
Für den GeoManager ist ein PVC ('map-data', s. wls-project-sdbby-pvc.yaml) definiert, der in einem Kubernetes-Storage angelegt ist. Dort ist der Inhalt des Verzeichnisses _map_ abzulegen. 

## Mount-Point des wls-project-sdbby-Storage

I.d.R. ist der PVC auf dem ersten OKD-Master im Verzeichnis 

    /mnt/pvc/map-data

gemountet.
