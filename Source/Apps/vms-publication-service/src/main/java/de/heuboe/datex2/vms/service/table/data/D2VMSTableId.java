package de.heuboe.datex2.vms.service.table.data;

public class D2VMSTableId {
	private String d2Id;
	private String d2Version;
	
	public D2VMSTableId(String d2Id, String d2Version) {
		super();
		this.d2Id = d2Id;
		this.d2Version = d2Version;
	}
	public String getD2Version() {
		return d2Version;
	}
	public void setD2Version(String d2Version) {
		this.d2Version = d2Version;
	}
	public String getD2Id() {
		return d2Id;
	}
	public void setD2Id(String d2Id) {
		this.d2Id = d2Id;
	}
}

