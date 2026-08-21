package de.heuboe.tls.receiver.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import de.heuboe.log.Logger;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataWriter;

public class DataWriterDummyImpl implements DataWriter {

	private static final Logger LOGGER = Logger.getLogger(DataWriterDummyImpl.class);
	@Override
	public void write(DataObject obj) {
		LOGGER.info("Dataset received:" + obj.getAddress() + ", Name=" + obj.getName());
		LOGGER.info("  Address=" + obj.getAddress() + ", Name=" + obj.getName());
		LOGGER.info("  Name=" + obj.getName());
		for(DataItem item : obj.getItems()) {
			String text = getText(item);
			LOGGER.info("  " + text);
		}

	}

	private String getText(DataItem item) {
		String text = item.getName() + "=";
		switch (item.getType()) {
		case INTEGER:
			text += item.getAsLong();
			break;
		case FLOAT:
			text += item.getAsDouble();
			break;
		case STRING:
			text += item.getAsString();
			break;
		case DATE:
			SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");
			text += fmt.format(item.getAsDate());
			break;
		case BLOCK:
			text += Arrays.toString(item.getAsBlock());
			break;
		case SKIP:
			text += "<byte ignored>";
			break;
		case ARRAY:
			text += getArray(item);
			break;
		case LIST:
			throw new IllegalStateException("didn't expect a list data item here!");
		default:
		        throw new IllegalStateException( item.getType() + " type not expected here!");
		}
		return text;
	}

	private String getArray(DataItem item) {
	        StringBuilder sb = new StringBuilder( " Array:\n" );
		List<DataObject> array = item.getAsArray();
		int i=0;
		for(DataObject obj : array) {
		        sb.append( " " ).append( ++i ).append( ". Element:\n" );
			for(DataItem dataItem : obj.getItems()) {
			        sb.append( "   " ).append( getText(dataItem) ).append( "\n" );
			}
		}
		return sb.toString();
	}

        @Override
        public void beginEtel() {
                // intentionally left blank
                
        }

        @Override
        public void endEtel() {
                // intentionally left blank
                
        }
}
