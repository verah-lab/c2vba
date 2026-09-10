package de.heuboe.wls.by.cfg;

import java.util.HashSet;
import java.util.Set;

public class Aq extends Q {
	
	private Set<Integer> wzgDes = new HashSet<>();
	private Set<Integer> nonClusterDes = new HashSet<>();
	
	public Aq( String id, String road, Double meter, int knNr, int deNr, Set<Integer> wzgDes ) {
		super( id, road, meter, knNr, 4, deNr );
		this.wzgDes = wzgDes;
	}

	@Override
	public Set<Integer> getDefiningDes() {
		return getWzgDes();
	}

	public Set<Integer> getWzgDes() {
		return wzgDes;
	}

	public Set<Integer> getNonClusterDes() {
		return nonClusterDes;
	}

	public void setNonClusterDes(Set<Integer> nonClusterDes) {
		this.nonClusterDes = nonClusterDes;
	}
}
