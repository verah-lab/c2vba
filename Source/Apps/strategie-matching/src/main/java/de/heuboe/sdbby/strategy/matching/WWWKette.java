package de.heuboe.sdbby.strategy.matching;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple bean for a www chain. It consists only of Getters and Setters.
 * 
 * @author ralfz
 *
 */
public class WWWKette {

	private String permId;
	private String name;
	private List<WWWSchild> schilder;
	
	public WWWKette(String permId, String name) {
		super();
		this.permId = permId;
		this.name = name;
	}

	public String getPermId() {
		return permId;
	}

	
	public String getName() {
		return name;
	}

	public List<WWWSchild> getSchilder() {
		if (schilder == null) {
			schilder = new ArrayList<>();
		}
		return schilder;
	}	
	
}
