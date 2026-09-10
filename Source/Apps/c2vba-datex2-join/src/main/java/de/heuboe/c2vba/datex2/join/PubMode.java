package de.heuboe.c2vba.datex2.join;

public enum PubMode {
	// Zyklischer Snapshot
	CyclicSnapshot,   
	
	// Zyklischer Snapshot nur bei Vorliegen von Änderungen
	CyclicSnapshotOnChange,
	
	// Bei Update Snapshot
	OnUpdateSnapshot,
	
	// Updates werden unmittelbar publiziert und zyklischer Snapshot
	OnUpdateCyclicSnapshot,
}
