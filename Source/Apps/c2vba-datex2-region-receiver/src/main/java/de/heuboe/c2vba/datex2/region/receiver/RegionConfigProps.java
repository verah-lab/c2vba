package de.heuboe.c2vba.datex2.region.receiver;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


/**
 * 
 * Region connection configuration
 * 
 * @author peters
 *
 */
@Component
@ConfigurationProperties(prefix = "regions")
public class RegionConfigProps {
    
    private List<RegionConfig> regionConfigs;
    
    @Data
    public static class RegionConfig {  // NOSONAR
        private String name;
        private String remote;
        private String consumerMode;
        private String url;
        private String context;
        private String trustStoreFile;
        private String trustStorePassword;
        private String keyStoreType;
        private String keyStoreFile;
        private String keyStorePassword;
        private String keyAlias;
        private String keyPassword;
        private String topic;
        private String serverSchema = "https";       
        private int serverPort;
        private String serverDomain;
    }

	public List<RegionConfig> getRegionConfigs() {
		return regionConfigs;
	}

	public void setRegionConfigs(List<RegionConfig> regionConfigs) {
		this.regionConfigs = regionConfigs;
	}
}
