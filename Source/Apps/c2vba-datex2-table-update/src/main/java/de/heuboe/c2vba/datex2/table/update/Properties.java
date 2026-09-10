package de.heuboe.c2vba.datex2.table.update;

import java.nio.charset.StandardCharsets;

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
@ConfigurationProperties("de.heuboe.c2vba.datex2.table.update")
@Data
public class Properties {

    private String type;
    
    // Sued table
	private String tableSFile = "";

    // Nord table (empty if retrieved from MDM)
	private String tableNFile = "";
	
    // Sued table 
	private String resultFile;
	
	// Value "ID": version of join table includes also ID of Sued table 
	private String versionModeS = "";

	// Value "ID": version of join table includes also ID of Nord table 
	private String versionModeN = "";

    
    // Content of table publications received from MDM may be ISO_8859_1 encoded
    // For correctEncoding = true a conversion to UTF_8 is executed
    private boolean correctEncoding = true;
}
