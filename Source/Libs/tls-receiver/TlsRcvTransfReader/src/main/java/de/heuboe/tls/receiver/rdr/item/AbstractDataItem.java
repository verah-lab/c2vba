package de.heuboe.tls.receiver.rdr.item;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;

public abstract class AbstractDataItem implements DataItem {

	protected String name;
	protected int consumedSize;

	public AbstractDataItem(String name, int consumedSize) {
		this.name = name;
		this.consumedSize = consumedSize;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int getConsumedSize() {
		return consumedSize;
	}

	@Override
	public void setConsumedSize(int consumedSize) {
		this.consumedSize = consumedSize;
	}
	
	@Override
	public Long getAsLong() {
		return null;
	}

	@Override
	public Double getAsDouble() {
		return null;
	}

	@Override
	public String getAsString() {
		return null;
	}

	@Override
	public Date getAsDate() {
		return null;
	}

	@Override
	public byte[] getAsBlockRaw() {
		return new byte[0];
	}

        @Override
        public long[] getAsBlock() {
                return new long[0];
        }

        @Override
        public GregorianCalendar getAsGregorianCalendar() {
                return null;
        }

	@Override
	public List<DataObjectIf> getAsArray() {
		return new LinkedList<>();
	}
	
	@Override
	public List<DataItem> getAsItemList() {
                return new LinkedList<>();
	}
	
	//= !!! DataItem extends DataObjectIf ==================================

        @Override
        public List<DataItem> getItems() {
                // TODO Auto-generated method stub
                throw new IllegalStateException( "Rethink if Item is really an Object" );
        }

        @Override
        public Map<String, DataItem> getItemMap() {
                // TODO Auto-generated method stub
                throw new IllegalStateException( "Rethink if Item is really an Object" );
        }

        @Override
        public void addItem( DataItem dataItem ) {
                // TODO Auto-generated method stub
                throw new IllegalStateException( "Rethink if Item is really an Object" );
                
        }

        @Override
        public String getAddress() {
                // TODO Auto-generated method stub
                throw new IllegalStateException( "Rethink if Item is really an Object" );
        }

        @Override
        public boolean isSubsequent() {
                // TODO Auto-generated method stub
                throw new IllegalStateException( "Rethink if Item is really an Object" );
        }
}
