package de.heuboe.srb.datex2.srp;

/**
 * 
 * Publication mode
 * 
 * @author peters
 *
 */
public enum PublicationMode
{
	// Zyklisch Snapshot-Publikation
	CyclicComplete,    																// NOSONAR
	
	// Zyklisch und bei Updates Snapshot-Publikation
	CyclicOnChangeComplete,															// NOSONAR
	
	// Snapshot-Publikation bei Updates
	OnUpdate,																		// NOSONAR
				
	// Zyklisch Snapshot-Publikation, Bei Updates Publikation mit Änderungen
	OnUpdateCyclicSnapshot,															// NOSONAR
}
