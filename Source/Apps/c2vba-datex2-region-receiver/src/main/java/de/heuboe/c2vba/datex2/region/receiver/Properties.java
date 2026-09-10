package de.heuboe.c2vba.datex2.region.receiver;

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
@ConfigurationProperties("de.heuboe.c2vba.datex2.region.receiver")
@Data
public class Properties {
	
	public static final String NORD = "nord";
	public static final String SUED = "sued";
	
    @Value("${spring.kafka.consumer.group-id:sdbby-service}")
    private String consumerGroupId;
    
    
    
    private String pubFileDir;
    private int keepInterval = 6;
    private int pullInterval;

    // If for more than aliveIntervalSued seconds no data form region Süd has been received, the app is terminated
    private int aliveIntervalSued = -1;
    
    // If for more than aliveIntervalNord seconds no data form region Nord has been received, the app is terminated
    // (This applies only if Nord data has ever been received)
    private int aliveIntervalNord = -1;

    private long keyStockReadTimeout;
    private String generalD2Schema = "schema/DATEXIISchema_2_2_3-c2vba.xsd";
}
