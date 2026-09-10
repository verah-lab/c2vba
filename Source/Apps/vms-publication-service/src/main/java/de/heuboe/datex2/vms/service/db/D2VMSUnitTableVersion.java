package de.heuboe.datex2.vms.service.db;

import org.springframework.data.annotation.Id;

import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;

public class D2VMSUnitTableVersion {
	
	@Id
	private String id;
	
	public void createId() {
		this.id = tableD2Id + "__" + tableD2Version + "__" + unit.getD2Id();
	}
	
	private String tableD2Id;
	private String tableD2Version;
	private D2VMSUnit unit;

	public D2VMSUnitTableVersion(String tableD2Id, String tableD2Version, D2VMSUnit unit) {
		super();
		this.tableD2Id = tableD2Id;
		this.tableD2Version = tableD2Version;
		this.unit = unit;
	}
	public String getTableD2Id() {
		return tableD2Id;
	}
	public void setTableD2Id(String tableD2Id) {
		this.tableD2Id = tableD2Id;
	}
	public String getTableD2Version() {
		return tableD2Version;
	}
	public void setTableD2Version(String tableD2Version) {
		this.tableD2Version = tableD2Version;
	}
	public D2VMSUnit getUnit() {
		return unit;
	}
	public void setUnit(D2VMSUnit unit) {
		this.unit = unit;
	}
}
