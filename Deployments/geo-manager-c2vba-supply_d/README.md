# geo-manager-c2vba-supply_D

## Einleitung

Der Inhalt des Verzeichnisses **map** ist Bestandteil der Versorgung des GeoManagers. Er umfasst einmal eine vollständige Karte (OSM-Karte, Kilometrierung, LCL und Betriebsmittel), die beim Erst-Start in einem System verwendet wird. Daraus wird eine MongoDB-Repräsentation erzeugt, die bei darauffolgenden Starts gelesen wird.  
Für den GeoManager ist ein PVC ('map-geo-data', s. geo-manager-c2vba-pvc.yaml) definiert, der in einem Kubernetes-Storage angelegt ist. Dort ist der Inhalt des Verzeichnisses _map_ abzulegen. 

## Mount-Point des GeoManager-Storage

I.d.R. ist der PVC auf dem ersten OKD-Master im Verzeichnis 

    /mnt/pvc/map-geo-data

gemountet.
