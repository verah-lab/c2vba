# datex2MDP

Die App **datex2MDPBuilder** erzeugt DATEX-II-MeasuredData-Publikationen. Sie meldet sich an der Datenverteilung auf die durch die Aufruf-Parameter mstId und mstVersion definierten Datenarten an und erzeugt zyklisch ein DATEX-II-XML-Dokument, das i.d.R. beim MDM publiziert wird.   

## Umstellung Java-11/Kafka/MongoDB

Im Kontext C2VBA werden Verkehrsdaten über Kafka verteilt und die MST-Beschreibung (DDP-Datenmodul datexMST, Datenarten datex2MSTDataSource, datex2MSTLocation, datex2MSTItem) wird in einer MongoDB abgelegt. Außerdem wird die neue Umgebung nur noch Java-11 unterstützen.

### Java-11

Dabei kann man sich an bereits portierten Artefakten wie **vms-publication-service** orientieren. Es steht noch keine allgemeine Datex2-MeasuredData-Parent-Pom zur Verfügung. (Für den MST-Teil ist datex2MSTParent bereits portiert). Man kann erst mal wls-kernel-parent verwenden. 

### Parameter/Properties

Bisher werden Aufruf-Parameter vom HbArg-Parser gelesen (s. Parameter.java). Das ist in eine Spring-Properties.java zu überführen. Da kann man sich etwa an der Umstellung von **Datex2/datex2vms/statusPublication** zu **vms-status-publication** orientieren.

### Publication-Objekt D2LogicalModel erzeugen

Das ist im Package **de.heuboe.datex2.mdp.builder.writer** implementiert. Der Inhalt wird textuell zusammengestellt. Die neue Version sollte das Java-Binding des XML-Schemas verwenden.

### Dataset

Verkehrswert-Daten werden bisher in **de.heuboe.ddp.Dataset/de.heuboe.ddp.Value** angenommen. Es ist vielleicht günstig, wenn identisch aussehende Interfaces (mit demselben Namen) angelegt, deren Implementierung an ProtoPojo-Datensätze delegieren. 

### Lesen der MST-Beschreibung

Für **D2MDPDataReader** ist eine MongoDB-Implementierung zu erstellen. Da ist das Artefakt **datex2-mst-persistence** heranzuziehen. Um D2MstPersistence.readMst() verwenden zu können, ist noch MstContentDb.fromDb() zu implementieren.

