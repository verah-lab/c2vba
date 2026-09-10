package de.heuboe.sdbby.strategy.matching;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple bean for a www panel. It consists only of Getters and Setters.
 * 
 * @author ralfz
 *
 */
public class WWWSchild {

	private String permId;
	private String name;
	private List<WWWPrisma> prismen;
	
	public WWWSchild(String permId, String name) {
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

	public List<WWWPrisma> getPrismen() {
		if (prismen == null) {
			prismen = new ArrayList<>();
		}
		return prismen;
	}
		
}
