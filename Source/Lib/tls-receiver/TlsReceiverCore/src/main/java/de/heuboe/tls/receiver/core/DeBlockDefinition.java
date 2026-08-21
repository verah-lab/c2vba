package de.heuboe.tls.receiver.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.heuboe.tls.receiver.interfaces.GetterRule;

/**
 * Class holding the necessary contents to analyse one DE-Block 
 * @author Ralf Zobel
 *
 */
public class DeBlockDefinition {

        private List<Integer> fg = null;
	private List<Integer> id = null;
	private Integer typ = 0;
	
	private String specialLocationsName = null;
	private String specialLocationsExcludedName = null;
	private Set<Integer> specialLocations;
	
	private String name = "unset";
	private boolean isHeader = false;
	
	private List<GetterRule> getterRules;

	public List<Integer> getFg() {
		if (fg == null) {
			fg = new ArrayList<>();
		}
		return fg;
	}

	public List<Integer> getId() {
		if (id == null) {
			id = new ArrayList<>();
		}
		return id;
	}

	public Integer getTyp() {
		return typ;
	}

	public void setTyp(Integer typ) {
		this.typ = typ;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public List<GetterRule> getGetterRules() {
		if (getterRules == null) {
			getterRules = new ArrayList<>();
		}
		return getterRules;
	}

	public String getSpecialLocationsName() {
                return specialLocationsName;
        }

        public void setSpecialLocationsName( String specialLocationsName ) {
                this.specialLocationsName = specialLocationsName;
        }

        public String getSpecialLocationsExcludedName() {
                return specialLocationsExcludedName;
        }

        public void setSpecialLocationsExcludedName( String specialLocationsExcludedName ) {
                this.specialLocationsExcludedName = specialLocationsExcludedName;
        }

        public Set<Integer> getSpecialLocations() {
                return specialLocations;
        }

        public void setSpecialLocations( Set<Integer> specialLocations ) {
                this.specialLocations = specialLocations;
        }

}
