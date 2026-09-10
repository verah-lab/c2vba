# strategie-matching

Evaluiert bei Eingang neuer Schaltdaten die booleschen Strategie-Definitionen (StrategieRegeln.txt). Ändert sich die Aktivität einer Strategie, wird das in der MongoDB 'c2vba', Collection 'strategy' gespeichert und im Topic 'StrategyStates' Kafka-publiziert.  


(Die Entwicklung zur GeoDyn-Umgebung wird auf dem Branch geodyn-java8 weitergeführt.)
