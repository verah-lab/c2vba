package de.heuboe.tls.receiver.rdr.core;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.DeBlockDefinitionIf;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.rdr.item.GregorianItem;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;

/**
 * Class holding the necessary contents to analyse one DE-Block 
 * @author Ralf Zobel
 *
 */
public class DeBlockDefinition implements DeBlockDefinitionIf {

        private List<Integer> fg = null;
	private List<Integer> id = null;
	private Integer typ = 0;
	
	private String specialLocationsName = null;
	private String specialLocationsExcludedName = null;
	private Set<Integer> specialLocations;
	
	private String name = "unset";
	private boolean isHeader = false;
	
	private Map<String, DataItemType> etelVarTypeMap;
	
	private List<GetterRule> getterRules;
	
	public DeBlockDefinition() {
	        etelVarTypeMap = new HashMap<>();
                etelVarTypeMap.put( "#Node",   DataItemType.INTEGER);
                etelVarTypeMap.put( "#TlsFg",  DataItemType.INTEGER);
                etelVarTypeMap.put( "#TlsId",  DataItemType.INTEGER);
                etelVarTypeMap.put( "#TlsJob", DataItemType.INTEGER);             
                etelVarTypeMap.put( "#De",     DataItemType.INTEGER);
                etelVarTypeMap.put( "#DeLen",  DataItemType.INTEGER);
                etelVarTypeMap.put( "#Puffer", DataItemType.INTEGER);

                etelVarTypeMap.put( "#ActualTime",  DataItemType.GREGORIAN);
                etelVarTypeMap.put( "#RcvTimeBase", DataItemType.GREGORIAN);
	}

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

        @Override
        public void add( GetterRule rule ) {
                if (getterRules == null) {
                        getterRules = new ArrayList<>();
                }
                getterRules.add( rule );
                rule.prepareType( "", etelVarTypeMap );
        }

        @Override
        public void complete() {
                etelVarTypeMap = null;
        }
}
