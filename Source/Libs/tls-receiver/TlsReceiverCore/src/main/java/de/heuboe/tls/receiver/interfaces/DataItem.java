package de.heuboe.tls.receiver.interfaces;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.heuboe.tls.receiver.impl.DataObject;

/**
 * A data item is a kind of an attribute with a type.
 * 
 * @author Ralf Zobel / Ronald Nikel
 *
 */
public interface DataItem {

	enum DataItemType { 
		INTEGER, 
		FLOAT, 
		STRING, 
		DATE, 
		BLOCK, 	   // this is a byte array
		SKIP, 
		ARRAY,     // array of data objects
		LIST,	   // list of data items
		GREGORIAN, // GregorianCalendar
                ILIST,     // list of constant integers
		}
	
	int getConsumedSize();
	void setConsumedSize(int size);
	
	String getName();
	void setName(String name);
	
	DataItemType getType();
	
	DataItem copy();				// makes a flat copy of itself
	
	Long getAsLong();
	Double getAsDouble();
	String getAsString();
	Date getAsDate();
        long[] getAsBlock();
	byte[] getAsBlockRaw();
        GregorianCalendar getAsGregorianCalendar();
	List<DataObject> getAsArray();
	List<DataItem> getAsItemList();	// in case of e.g. if getters, this is needed to get the collected items
}
