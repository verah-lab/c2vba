package de.heuboe.wls.by.cfg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Infrastructuree object
 * 
 * @author peters
 *
 */
public abstract class Q {
    
    public static final String DRIVING_DIRECTION_NO = "NO";
    public static final String DRIVING_DIRECTION_SW = "SW";
    
    public static final String D2_MAIN_CARRIAGEWAY = "mainCarriageway";
    public static final String D2_EXIT_SLIP_ROAD = "exitSlipRoad";
    public static final String D2_ENTRY_SLIP_ROAD = "entrySlipRoad";

	private String id;
	private String road;
	private Double meter;

	private int knNr;
	private int fg;
	private int deNr;

	private int glanetype = -1;
	private Set<Integer> laneDes = new HashSet<>();
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param type     qtype
	 * @param id       ID
	 * @param bab      Road number
	 * @param meter    Kilometer    
	 * @param de       DE number
     * @param fg       Funktionsgruppe
	 */
	public Q( String id, String road, Double meter, int knNr, int fg, int de ) {
		super();
		this.id = id;
		this.road = road;
		this.meter = meter;
		this.knNr = knNr;
		this.fg = fg;
		this.deNr = de;
	}
	
	public String getId() {
		return id;
	}
	public Double getMeter() {
		return meter;
	}
	public int getDeNr() {
		return deNr;
	}
	
	public Set<Integer> getDefiningDes() {
		return new HashSet<>( Arrays.asList( deNr ) );
	}

    public void setMeter(Double meter) {
        this.meter = meter;
    }

    public String getD2Carriageway() {   // NOSONAR
        return getD2Carriageway( deNr );
    }
	
    /**
     * 
     * Returns DATEX-II carriageway
     * 
     * @return  DATEX-II carriageway
     */
    public static String getD2Carriageway( int de ) {   // NOSONAR
        
        if( ( ( de >= 65 ) && ( de <= 71 )  ) ||
            ( ( de >= 81 ) && ( de <= 87 )  ) ||
            ( ( de >= 73 ) && ( de <= 79 )  ) ||

            ( ( de >= 113 ) && ( de <= 119 )  ) ||
            ( ( de >= 97 ) && ( de <= 103 )  ) ||
            ( ( de >= 105 ) && ( de <= 111 )  ) ) {
            return Q.D2_ENTRY_SLIP_ROAD;
        } else if( ( ( de >= 129 ) && ( de <= 135 )  ) ||
                   ( ( de >= 137 ) && ( de <= 143 )  ) || 
                   ( ( de >= 145 ) && ( de <= 151 )  ) || 

                   ( ( de >= 169 ) && ( de <= 175 )  ) || 
                   ( ( de >= 161 ) && ( de <= 167 )  ) || 
                   ( ( de >= 177 ) && ( de <= 183 )  )  ) {
                return Q.D2_EXIT_SLIP_ROAD;
            }
        
        return Q.D2_MAIN_CARRIAGEWAY;
    }
    
    /**
     * 
     * Returns driving direction for De-Nummer
     * 
     * @param de	De-Nummer
     * @return		Driving direction
     */
    public static String getDrivingDirection( int de ) {  
        return getDrivingDirectionMq( de / 8 * 8 + 7 );
    }
        
    /**
     * 
     * Returns driving direction for MQ-De-Nummer
     * 
     * @param de	De-Nummer
     * @return		Driving direction
     */
    public static String getDrivingDirectionMq( int de ) {
        if ( de == 7 || 
             de == 15 || 
             de == 23 || 
             de == 194 || 
             de == 196 || 
             de == 231 || 
             de == 135 || 
             de == 87 || 
             de == 143 ||
             de == 79 ||
             de == 151 ||
             de == 71 ) {
           return DRIVING_DIRECTION_NO;
        }
        if ( de == 39 || 
            de == 47 || 
            de == 55 || 
            de == 193 || 
            de == 195 || 
            de == 239 ||
            de == 167 ||
            de == 119 ||
            de == 175 ||
            de == 111 ||
            de == 183 ||
            de == 103 ) { 
           return DRIVING_DIRECTION_SW;
        }
        
        return "";

    }
    
    public String getDrivingDirection() {
        return getDrivingDirection( deNr );
    }

    public String getRoad() {
    	return road;
    }

    public int getFg() {
        return fg;
    }

    public void setFg(int fg) {
        this.fg = fg;
    }

	public Set<Integer> getLaneDes() {
		return laneDes;
	}

	public void setLaneDes(Set<Integer> laneDes) {
		this.laneDes = laneDes;
	}

	public int getGlanetype() {
		return glanetype;
	}

	public void setGlanetype(int glanetype) {
		this.glanetype = glanetype;
	}

	public String getTypeName() {
		return null;
	}

	public int getKnNr() {
		return knNr;
	}
}
