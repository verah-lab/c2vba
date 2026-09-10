package de.heuboe.c2vba.datex2.join;

import java.util.Date;


/**
 * 
 * Abstract operations on DATEX-II publication
 * 
 * @author peters
 *
 * @param <T> D2LogicalModel of a profile
 */
public interface ResolverPub<T> {

	/**
	 * 
	 * Snapshot data? 
	 * 
	 * @param d2lm	D2LogicalModel
	 * @return	true: is snapshot data
	 */
	boolean isSnapshot( T d2lm ); 
	
	
	/**
	 * 
	 * Get table ID
	 * 
	 * @param d2lm	D2LogicalModel
	 * @return Table ID
	 */
	String getTableId( T d2lm );
	
	/**
	 * 
	 * Get table version
	 * 
	 * @param d2lm	D2LogicalModel
	 * @return Table version
	 */
	String getTableVersion( T d2lm );
	
	/**
	 * 
	 * Number of elements in PayloadPublication (e.g MeasurementSites, Situations )
	 * 
	 * @param d2lm 	D2LogicalModel
	 * @return Number of elements
	 */
	int getElementCount( T d2lm );
	
	/**
	 * 
	 * Get publication time
	 * 
	 * @param d2lm	D2LogicalModel
	 * @return Publication time
	 */
	Date getPubTime( T d2lm );
	
	/**
	 * 
	 * Update (snapshot) publication by content of update publication
	 * 
	 * @param pub			Snapshot publication
	 * @param updatePub		Update publication
	 */
	void updatePub( T pub, T updatePub );

	/**
	 * 
	 * Clones publication
	 * 
	 * @param d2lm  D2LogicalModel
	 * @return Clone of publication
	 */
	T clone( T d2lm );
	
	
	/**
	 * 
	 * 
	 * Updates table reference in PayloadPublication
	 * 
	 * @param d2lm			D2LogicalModel
	 * @param tableId		Table ID
	 * @param tableVersion	Table version
	 */
	void updateTableRef( eu.datex2.schema._2._2_0.D2LogicalModel d2lm, String tableId, String tableVersion );
	
	/**
	 * 
	 * Remove all elements in publication
	 * 
	 * @param d2lm	Publication 
	 */
	void clearElements( eu.datex2.schema._2._2_0.D2LogicalModel d2lm );
}
