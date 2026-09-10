package de.heuboe.wls.by.cfg;

import java.util.HashSet;
import java.util.Set;

import eu.vmis_ehe.vmis2.configservice.LanePos.LaneType;

public class Mq extends Q {
	
	private Set<Integer> laneDes = new HashSet<>();
	private LaneType laneType;
	
	public Mq( String id, String road, Double meter, int knNr, int deNr, Set<Integer> laneDes, LaneType laneType ) {
		super( id, road, meter, knNr, 1, deNr );
		this.laneDes = laneDes;
		this.laneType = laneType;
	}

	@Override
	public Set<Integer> getLaneDes() {
		return laneDes;
	}

	public Set<Integer> getDefiningDes() {
		return getLaneDes();
	}

	public LaneType getLaneType() {
		return laneType;
	}
}
