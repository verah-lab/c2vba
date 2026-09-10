package de.heuboe.sdbby.strategy.matching;

/**
 * A simple bean for a www prism. It consists only of Getters and Setters.
 * 
 * @author ralfz
 *
 */
public class WWWPrisma {

	private String permId;
	private String name;
	private int stellcode = -1;
	private String displayedText = null;
	private int errorcode;
	private int kanalsteuerung;
	private int betriebsart;
	private int funktionsbyte;
	
	public WWWPrisma() {
	}
	
	/**
	 * 
	 * Constructor 
	 * 
	 * @param permId		ID
	 * @param name			Name
	 */
	public WWWPrisma(String permId, String name ) {
		super();
		this.permId = permId;
		this.name = name;
		this.stellcode = -1;
		this.errorcode = 0;
		this.kanalsteuerung = 0;
		this.betriebsart = 3;
	}

	public int getStellcode() {
		return stellcode;
	}
	
	// @@@@ (betriebsart == 3) kontrollieren
	//
	public boolean validState() {
		return (errorcode == 0) && (kanalsteuerung == 0) && (betriebsart == 3) && (funktionsbyte != 0);
	}

	public void setStellcode(int stellcode) {
		this.stellcode = stellcode;
	}

	public String getPermId() {
		return permId;
	}

	public String getName() {
		return name;
	}

	public void setErrorCode(int errorcode) {
		this.errorcode = errorcode;
	}

	public void setKanalSteuerung(int kanalsteuerung) {
		this.kanalsteuerung = kanalsteuerung;		
	}

	public void setBetriebsArt(int betriebsart) {
		this.betriebsart = betriebsart;
	}

	public String getDisplayedText() {
		return displayedText;
	}

	public void setDisplayedText(String displayedText) {
		this.displayedText = displayedText;
	}

	public int getFunktionsbyte() {
		return funktionsbyte;
	}

	public void setFunktionsbyte(int funktionsbyte) {
		this.funktionsbyte = funktionsbyte;
	}
		
}
