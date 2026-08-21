package de.heuboe.tls.receiver.impl;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;

/**
 * A DataObject represents a single DataSet. 
 * It is made up of a name, an address and a list of data items.
 *  
 * @author ralfz
 *
 */
public class DataObject {
        public static class ETelMeta {
		private final int fg;
		private final int id;
		private final int job;
		private final int numDeInETel; // total number of de blocks in ETel
		public ETelMeta(int fg, int id, int job, int numDeInETel/*, String addressOfSender*/) {
			super();
			this.fg = fg;
			this.id = id;
			this.job = job;
			this.numDeInETel = numDeInETel;
		}
		public int getFg() {
			return fg;
		}
		public int getId() {
			return id;
		}
		public int getJob() {
			return job;
		}
		public int getNumDeInETel() {
			return numDeInETel;
		}
	}
	public static class DeMeta {
		private int deNr;
		private int typ;
		private int seqDeInETel; //  current number of de block in ETel. numDeInETel + 1 == seqDeInETel => last de block of etel
		private byte[] deBlockRaw; // without size, de number and type
		public DeMeta(int deNr, int typ, int seqDeInETel, byte[] deBlockRaw) {
			super();
			this.deNr = deNr;
			this.typ = typ;
			this.seqDeInETel = seqDeInETel;
			this.deBlockRaw = deBlockRaw;
		}
		public int getDeNr() {
			return deNr;
		}
		public int getTyp() {
			return typ;
		}
		public int getSeqDeInETel() {
			return seqDeInETel;
		}
		public byte[] getDeBlockRaw() {
			return deBlockRaw;
		}
	}

	private String name;
	private String address;
	private ETelMeta etelMeta;
	private DeMeta deMeta;
	private TlsETel etel; // Einzeltelegrammreferenz
        private TlsTele tele; // Sammeltelegrammreferenz
	private List<DataItem> items;
	private boolean subsequent = false; // nachgeliefert
	
	
	public DataObject() {
		super();
	}
	
	public DataObject(String name, String address) {
		super();
		this.name = name;
		this.address = address;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<DataItem> getItems() {
		if (items == null) {
			items = new ArrayList<>();
		}
		return items;
	}

	public ETelMeta getEtelMeta() {
		return etelMeta;
	}

	public void setEtelMeta(ETelMeta etelMeta) {
		this.etelMeta = etelMeta;
	}

	public DeMeta getDeMeta() {
		return deMeta;
	}

	public void setDeMeta(DeMeta deMeta) {
		this.deMeta = deMeta;
	}

        // Get reference to surrounding etel (Einzeltelegramm)
	public TlsETel getEtel() {
                return etel;
        }

        // Set reference to surrounding etel (Einzeltelegramm)
        public void setEtel( TlsETel etel ) {
                this.etel = etel;
        }

        // get reference to surrounding tel (Sammeltelegramm)
        public TlsTele getTele() {
                return tele;
        }

        // Set reference to surrounding tel (Sammeltelegramm)
        public void setTele( TlsTele tele ) {
                this.tele = tele;
        }

        // determine whether a DataObject is subsequent delivered (nachgeliefert) 
        public boolean isSubsequent() {
                return subsequent;
        }

        // set whether a DataObject is subsequent delivered (nachgeliefert) 
        public void setSubsequent( boolean subsequent ) {
                this.subsequent = subsequent;
        }
	
}
