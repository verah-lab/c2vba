Der Aufruf von generatePartialMST.cmd erzeugt eine partielle MST zu einer vorher erzeugten kompletten MST 
für die MST-ID 'LVE-MDM'. Es wird die Java-Anwendung d2MSTPubBuilder ausgeführt. Die Aufruf-Parameter sind in 
generateSubMST.arg zusammengefasst. Es sind noch die Werte von

-mmtLocFilterFile
-pubFileNameExt
-mstVersion

in dieser Reihenfolge anzugeben. Die Filterdatei (-mmtLocFilterFile) sollte im Unterverzeichnis 'filter' abgelegt sein. 
Die erzeugte MST-Publikation wird im Verzeichnis der Komplett-MSt abgelegt und ihr Name wird um den Wert von 
-pubFileNameExt erweitert. Der Auszug wird aus der mit -mstVersion angegebenen Version erzeugt.

Beispielaufruf:

generatePartialMST.cmd ./filter/mmtLocFilterMST_Q_A5part.txt vhPart 10


