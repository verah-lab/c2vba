package de.heuboe.srb.datex2.srp;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * A class that maps external configuration properties.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.sdbby.srp")
@Data
public class Properties {
	
	public static final String ROUTE_ENCODING_OPENLR = "openLR";
	public static final String ROUTE_ENCODING_ALERTC = "AlertC";
	public static final String ROUTE_ENCODING_COORD = "coordinateSequence";

	// CyclicComplete, CyclicOnChangeComplete, OnUpdate, OnUpdateCyclicSnapshot
	private String publicationMode = "CyclicComplete";
	
	// Publicationszyklus (s)
	private int pubInterval = 300;
	
	// "Nur für pubMode=OnUpdateCyclicSnapshot verwendet: in diesem Zyklus werden Änderungen an den Strategien geprüft (ms)"
	private int checkInterval = 5000;
	
	// Timout der WLS-Verbindung
	private int connTimeout = 1800000;
	
	// Publication-Verzeichnis
	private String fileDir;
	
	// Url des WLS
	private String wlsUrl;
		
	// Url des SDBBY-Service
	private String sdbbyUrl;
	
	// DATEX-II Target
	private String datex2Target;
			
	// DATEX-II Sender
	private String datex2Sender = "DE-MDM-Bayerische Straßenbauverwaltung - Zentralstelle für Verkehrsmanagement";

	// URL eines Push-Empfaengers der Publikationen
	private String d2ReceiverURL;
		
	// LCL-Version
	private String lclVersion = "20.0";
	
	// Schaltregeln
	private String  strategyRuleFile;

	// Beschreibung der Schaltgründe
	private String strategyCauseFile;
	
	// Zustellung an MDM: Datei mit URL, Verbindungs/Authentifikations-Parameter
	private String mdmAuthenticationFile;
	
	// DATEX-II-Schema-Datei
	private String d2SchemaLocation;
	
	private String consumerGroupId; 
	private String strategyStatesTopic;
	
	// DATEX-II Location-Kodierung der Routen. Zulässige Werte: openLR, AlertC oder coordinateSequence, default: coordinateSequence
    @Value("#{'${de.heuboe.sdbby.srp.routeEncoding:coordinateSequence}'.split(',')}")
	private List<String> routeEncoding;
    
    // true: converts OpenlrExtendedLinear elements so that they are valid with the original StrategicRouting.xsd 
    //       ( 'firstDirection' sub elements are renamed: --> 'openlrLineLocationReference' )
    private boolean convert2BaseSchema = true;
	
	// Datei mit geographischen Koordinaten für Strategien 
    private String strategyRoutesFilePath;
    
    // Verzeichnis mit WLS-LinearLocation-Dateien zu Strategie-Routen 
    private String wlsLocationDir = null;
    
    public PublicationMode getPubMode() {
    	return PublicationMode.valueOf( this.publicationMode );
    }
}
