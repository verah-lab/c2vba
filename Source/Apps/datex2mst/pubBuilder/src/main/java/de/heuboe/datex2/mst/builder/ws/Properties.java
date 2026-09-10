package de.heuboe.datex2.mst.builder.ws;


import lombok.Data;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


/**
 * A class that maps external configuration properties.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.datex2.mst")
@Data
public class Properties {
	@NotNull
	private String pubFilePath;

	@NotNull
	private String schemaPath;

	private String schemaFile;

	private String country = "de";
	private String natId = "DE-MDM-Die Autobahn GmbH des Bundes";
	private String lang = "de";
	
	private String schemaModelBaseVersion = "2";
	private String schemaNameSpace = "http://datex2.eu/schema/2/2_0";
	private String schemaCategory = "mdm";
	private String preferredLocEncoding = "ALERTC";
	
	@NotNull
	private String mstId;
	
	@NotNull
	private String mstName = null;

	@NotNull
	private String mstVersion; 
	
	@NotNull
	private String defVersion; 

	private String d2Version; 
	
	
	@Value("${de.heuboe.datex2.mst.loc.objectIdAsD2SiteId:true}")		
	private boolean objectIdAsD2SiteId;
	
	@Value("${de.heuboe.datex2.mst.loc.locServiceUrl:}")		
	@NotNull
	private String locServiceUrl; 
}
