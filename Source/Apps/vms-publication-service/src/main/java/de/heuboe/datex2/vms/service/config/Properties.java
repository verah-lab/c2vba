package  de.heuboe.datex2.vms.service.config;


import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import eu.vmis_ehe.vmis2.configservice.CfgAq.AqType;


/**
 * A class that maps external configuration properties.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.datex2.vms.service")
public class Properties {
  
	// Timout of web service connection
	private int connTimeout = 1800000;
	
	// Port of this web service
	private int servicePort = 22763;
	
	// Host of this web service
	private String serviceHost;

	// Complete VMS data is retrieved each <fullUpdateInterval> seconds (-1: no complete update)
	private int fullUpdateInterval = 120;
	
	// VMS data is retrieved each <updateInterval> millis
	private int updateInterval = 5000;
	
	// Zoom factor of images, the only valid values are 1.0 and 2.0
	private double zoomFactor = 1.0;
	
	// Valid values: NRW
	@NotNull
	private String cfgServiceType;
	
	// Url of (SOAP-)CfgService
	private String cfgServiceUrl;
	
	// Url of DataService 
	private String dataServiceUrl;
	
	// MQTT address
	private String messageBrokerAddresses;
	
	// SDBBY or NRW
	private String imageServiceType = "NRW";
	
	// SOAP or GRPC
	private String siteCfgSvcType = "SOAP";
	
	// Url of WLS
	@NotNull
	private String wlsUrl;
	
	// SRID of WLS coordinates
	private int wlsSRID = 31468;
	
	// Valid values: AQ
	private String aqLocationType = "AQ";
	
	private boolean useAggregatedAqs = true;

	// Use carriageway attribute in DATEX-II
	private boolean useCarriagewayAttr = true;
	
	// Host name in image url prefix
	private String imageUrlPrefixHost;
	
	// Port number in image url prefix
	private int imageUrlPrefixPort;
	
	// Complete image url prefix: may be used insted of imageUrlPrefixHost/imageUrlPrefixPort
	private String imageUrlPrefix;
	
	// Pfad-Präfix der Image-Dateien (nur im SDBBY-Kontext verwendet!)
	private String extFilePathPrefix = "";
	
	// Relevant AQ IDs
    @Value("#{'${de.heuboe.datex2.vms.service.aqIds:}'.split(',')}")
    private List<String> aqIds;

    // SBA AQ types
    @Value("#{'${de.heuboe.datex2.vms.service.sbaAqTypes:AQ}'.split(',')}")
    private List<AqType> sbaAqTypes;
    
	// SBA sites
    @Value("#{'${de.heuboe.datex2.vms.service.sbaSites:}'.split(',')}")
    private List<String> sbaSites;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;
    
	// Kafka topic switching data
    private String topicStellzustand;
    
    // Kafka topic error data
    private String topicFehler;
    
    // GeoDataMsgSvc host
    private String geoDataMsgSvcHost;

	// GeoDataMsgSvc port
    private int geoDataMsgSvcPort;
    
	// GeoDataMsg: zoom factor of images
    private double geoDataMsgImageZoomFactor= -1.0;

	public int getConnTimeout() {
		return connTimeout;
	}

	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}

	public int getServicePort() {
		return servicePort;
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	public String getServiceHost() {
		return serviceHost;
	}

	public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}

	public int getFullUpdateInterval() {
		return fullUpdateInterval;
	}

	public void setFullUpdateInterval(int fullUpdateInterval) {
		this.fullUpdateInterval = fullUpdateInterval;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public double getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(double zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	public String getCfgServiceType() {
		return cfgServiceType;
	}

	public void setCfgServiceType(String cfgServiceType) {
		this.cfgServiceType = cfgServiceType;
	}

	public String getCfgServiceUrl() {
		return cfgServiceUrl;
	}

	public void setCfgServiceUrl(String cfgServiceUrl) {
		this.cfgServiceUrl = cfgServiceUrl;
	}

	public String getDataServiceUrl() {
		return dataServiceUrl;
	}

	public void setDataServiceUrl(String dataServiceUrl) {
		this.dataServiceUrl = dataServiceUrl;
	}

	public String getImageServiceType() {
		return imageServiceType;
	}

	public void setImageServiceType(String imageServiceType) {
		this.imageServiceType = imageServiceType;
	}

	public String getWlsUrl() {
		return wlsUrl;
	}

	public void setWlsUrl(String wlsUrl) {
		this.wlsUrl = wlsUrl;
	}

	public int getWlsSRID() {
		return wlsSRID;
	}

	public void setWlsSRID(int wlsSRID) {
		this.wlsSRID = wlsSRID;
	}

	public String getAqLocationType() {
		return aqLocationType;
	}

	public void setAqLocationType(String aqLocationType) {
		this.aqLocationType = aqLocationType;
	}

	public boolean isUseCarriagewayAttr() {
		return useCarriagewayAttr;
	}

	public void setUseCarriagewayAttr(boolean useCarriagewayAttr) {
		this.useCarriagewayAttr = useCarriagewayAttr;
	}

	public String getImageUrlPrefixHost() {
		return imageUrlPrefixHost;
	}

	public void setImageUrlPrefixHost(String imageUrlPrefixHost) {
		this.imageUrlPrefixHost = imageUrlPrefixHost;
	}

	public int getImageUrlPrefixPort() {
		return imageUrlPrefixPort;
	}

	public void setImageUrlPrefixPort(int imageUrlPrefixPort) {
		this.imageUrlPrefixPort = imageUrlPrefixPort;
	}

	public String getMessageBrokerAddresses() {
		return messageBrokerAddresses;
	}

	public void setMessageBrokerAddresses(String messageBrokerAddresses) {
		this.messageBrokerAddresses = messageBrokerAddresses;
	}
	
	public List<String> getAqIds() {
		return aqIds;
	}

	public void setAqIds(List<String> aqIds) {
		this.aqIds = aqIds;
	}

	public String getExtFilePathPrefix() {
		return extFilePathPrefix;
	}

	public void setExtFilePathPrefix(String extFilePathPrefix) {
		this.extFilePathPrefix = extFilePathPrefix;
	}
	
	public String getSiteCfgSvcType() {
		return siteCfgSvcType;
	}

	public void setSiteCfgSvcType(String siteCfgSvcType) {
		this.siteCfgSvcType = siteCfgSvcType;
	}

	public String getTopicStellzustand() {
		return topicStellzustand;
	}

	public void setTopicStellzustand(String topicStellzustand) {
		this.topicStellzustand = topicStellzustand;
	}

	public String getTopicFehler() {
		return topicFehler;
	}

	public void setTopicFehler(String topicFehler) {
		this.topicFehler = topicFehler;
	}
    public String getGeoDataMsgSvcHost() {
		return geoDataMsgSvcHost;
	}

	public void setGeoDataMsgSvcHost(String geoDataMsgSvcHost) {
		this.geoDataMsgSvcHost = geoDataMsgSvcHost;
	}

	public int getGeoDataMsgSvcPort() {
		return geoDataMsgSvcPort;
	}

	public void setGeoDataMsgSvcPort(int geoDataMsgSvcPort) {
		this.geoDataMsgSvcPort = geoDataMsgSvcPort;
	}

	public double getGeoDataMsgImageZoomFactor() {
		return geoDataMsgImageZoomFactor;
	}

	public void setGeoDataMsgImageZoomFactor(double geoDataMsgImageZoomFactor) {
		this.geoDataMsgImageZoomFactor = geoDataMsgImageZoomFactor;
	}

	public String getImageUrlPrefix() {
		return imageUrlPrefix;
	}

	public void setImageUrlPrefix(String imageUrlPrefix) {
		this.imageUrlPrefix = imageUrlPrefix;
	}

    public List<String> getSbaSites() {
    	if( ( sbaSites == null ) || sbaSites.isEmpty() || 
    		( ( sbaSites.size() == 1 ) && sbaSites.get(0).isBlank() ) ) {
    		return Arrays.asList();
    	}
		return sbaSites;
	}

	public void setSbaSites(List<String> sbaSites) {
		this.sbaSites = sbaSites;
	}

	public List<AqType> getSbaAqTypes() {
		return sbaAqTypes;
	}

	public void setSbaAqTypes(List<AqType> sbaAqTypes) {
		this.sbaAqTypes = sbaAqTypes;
	}
	
	public boolean isUseAggregatedAqs() {
		return useAggregatedAqs;
	}

	public void setUseAggregatedAqs(boolean useAggregatedAqs) {
		this.useAggregatedAqs = useAggregatedAqs;
	}

	public String getConsumerGroupId() {
		return consumerGroupId;
	}

	public void setConsumerGroupId(String consumerGroupId) {
		this.consumerGroupId = consumerGroupId;
	}

}
