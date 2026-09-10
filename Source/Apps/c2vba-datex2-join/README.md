# c2vba-datex2-join

Joins DATEX-II publications from Nord and Süd to a single one and sends it to MDM.

Subscribes on Kafka topics for input:

* de.heuboe.c2vba.datex2.join.topicSued    
* de.heuboe.c2vba.datex2.join.topicNord 


Spring profile has to be set to specify the type of data:


* VMS: FG4, Schaltdaten   
* MDP: FG1, FG3  
* SRP: Strategien, DATEX-II situations


 


