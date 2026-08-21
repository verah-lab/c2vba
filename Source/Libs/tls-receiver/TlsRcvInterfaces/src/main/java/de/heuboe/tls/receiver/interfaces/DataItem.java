package de.heuboe.tls.receiver.interfaces;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A data item is a kind of an attribute with a type.
 *
 * @author Ralf Zobel / Ronald Nikel
 *
 */
public interface DataItem extends DataObjectIf {

	enum DataItemType {
	        NONE, // i.e. potentially SET
	        SKIP,  // skip instruction
	        COMPOUND, // can not be decided - compound (if) statement -> analyse contents
	        OPTIONAL, // OPT block of statements - compound statement -> analyse contents
		INTEGER,
		FLOAT,
		STRING,
		BLOCK, 	   // this is a byte array
		ARRAY,     // array of data objects - compound statement -> analyse contents
		LIST,	   // list of data items
		DATE,
		GREGORIAN, // GregorianCalendar
                ILIST,     // list of constant integers
        DURATION,
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
	List<DataObjectIf> getAsArray();
	List<DataItem> getAsItemList();	// in case of e.g. if getters, this is needed to get the collected items
}
