package de.heuboe.by.config.reader;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Property class
 */
@Configuration
@ConfigurationProperties("de.heuboe.by.config.import")
@Data
public class Properties {

    private String vrzId;
    private int vrzKnotenNr;
    private String uzId;
    private int uzKnotenNr;
    private String kriFile;
    private String wwwDir;
    private String aufbereitetDir;
}
