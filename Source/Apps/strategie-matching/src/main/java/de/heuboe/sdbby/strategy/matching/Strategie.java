package de.heuboe.sdbby.strategy.matching;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.sdbby.strategy.matching.data.Rule;

/**
 * A simple bean for a strategy. It consists only of Getters and Setters.
 * 
 * @author ralfz
 *
 */
public class Strategie {

	private String id;
	private String name;
	private List<Rule> rules;
	private boolean active;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param id			ID
	 * @param name			Name
	 */
	public Strategie(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.rules = new ArrayList<>();
		this.active = false;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}	
	
}
