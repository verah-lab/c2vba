package de.heuboe.wls.by.cfg;

import eu.vmis_ehe.vmis2.configservice.CfgUdeSensor.UdeType;

public class Ufd extends Q {
	
	private UdeType type;

	public Ufd( UdeType type, String id, String road, Double meter, int knNr, int deNr ) {
		super( id, road, meter, knNr, 3, deNr );
		this.type = type;
	}

	public UdeType getType() {
		return type;
	}

	@Override
	public String getTypeName() {
		return type.name();
	}

}
