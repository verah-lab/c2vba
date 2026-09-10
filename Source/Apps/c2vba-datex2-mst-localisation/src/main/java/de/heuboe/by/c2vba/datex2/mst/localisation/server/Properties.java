package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * A class that maps external configuration properties.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.by.c2vba.datex2.mst.localisation")
@Data
public class Properties {

	private int servicePort = 5711;
	
	@NotNull 
	private String wlsUrl = null;
	
	boolean addOpenLRLocation = false;
	
	@NotNull 
	private String lveBetriebsparameterTopic;

	@NotNull 
	private String ufdBetriebsparameterTopic;
	
	// Set to ABDS in C2VBA: used to mark the Süd-Bayern Measurementsites
	private String system;
}
