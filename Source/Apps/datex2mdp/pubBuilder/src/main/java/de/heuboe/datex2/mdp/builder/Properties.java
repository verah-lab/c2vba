package de.heuboe.datex2.mdp.builder;

import eu.datex2.schema._2._2_0.mdp.CountryEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Property class
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.datex2.mdp")
@Data
public class Properties {

    private String mstId;
    private String mstVersion;

    private long maxDataAge;
    private boolean onlyActive;
    private boolean checkValid;
    private boolean writeVehClass;
    private boolean ejectEmptySiteMeasurements;
    private boolean invalidValsAsDataError;

    private CountryEnum country;
    private String nationalId;
    private String language;

    private String checkValidity;
    private String schemaModelBaseVersion;
}
