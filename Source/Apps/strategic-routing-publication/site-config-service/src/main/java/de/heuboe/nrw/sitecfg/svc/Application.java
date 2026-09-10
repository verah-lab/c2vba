package de.heuboe.nrw.sitecfg.svc;

import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import de.heuboe.nrw.guisvc.sitecfg.iface.SiteCfgService;
import de.heuboe.nrw.sitecfg.svc.config.CfgSvcType;
import de.heuboe.nrw.sitecfg.svc.config.CreationMode;
import de.heuboe.nrw.sitecfg.svc.model.CreatorFile;
import de.heuboe.nrw.sitecfg.svc.model.Model;
import de.heuboe.nrw.sitecfg.svc.model.Validator;
import de.heuboe.nrw.sitecfg.svc.test.CompareServices;
import de.heuboe.nrw.srv.console.CfgServiceClient;
import de.heuboe.nrw.srv.console.GeoManagerClient;
import de.heuboe.zst.model.ICfgModel;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
//import eu.vmis_ehe.vmis2.configservice.DataChanges;
//import eu.vmis_ehe.vmis2.configservice.pojo.PDataChanges;
import eu.vmis_ehe.vmis2.geomanager.GeoManagerGrpc;
import eu.vmis_ehe.vmis2.geomanager.featureservice.FeatureServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Component
@SpringBootApplication
public class Application {
	// ------------------------------------------------------------------------------------------------------------------------------
//	private @Value("${de.heuboe.nrw.config.svc.nsUri}")						String		cfgSvcNrwNsUri;
//	private @Value("${de.heuboe.nrw.config.svc.wsdl}")						String		cfgSvcNrwWsdl;
//	private @Value("${de.heuboe.nrw.config.svc.host}")						String		cfgSvcNrwHost;
//	private @Value("${de.heuboe.nrw.config.svc.port}")						String		cfgSvcNrwPort;
//	private @Value("${de.heuboe.nrw.config.svc.host:nrw-guisvc-w7v}")		String		cfgSvcNrwHost;
//	private @Value("${de.heuboe.nrw.config.svc.port:7070}")					String		cfgSvcNrwPort;
	private @Value("${de.heuboe.nrw.config.svc.model.type:NRW}") 			String		cfgSvcType;
	private @Value("${de.heuboe.nrw.config.svc.soap.url:none}")				String		cfgServiceSoapAddress;
	private @Value("${de.heuboe.nrw.sitecfg.svc.soap.url:none}")			String		siteCfgSvcSoapAddress;
	private @Value("${de.heuboe.nrw.sitecfg.svc.log.soap.in:false}")		boolean		logSoapIn;
	private @Value("${de.heuboe.nrw.sitecfg.svc.log.soap.out:false}")		boolean		logSoapOut;
	private @Value("${de.heuboe.nrw.sitecfg.svc.log.roadseg.svg:false}")	boolean		logRoadSegSvgs;
	private @Value("${de.heuboe.nrw.sitecfg.svc.save.roadseg.svg:false}")	boolean		saveRoadSegSvgs;
	private @Value("${de.heuboe.nrw.sitecfg.svc.scs.compare:false}")		boolean		compareServices;
	private @Value("${de.heuboe.nrw.sitecfg.svc.rep.root}")					String		repRoot;
	private @Value("${de.heuboe.nrw.sitecfg.svc.rep.sites}")				String		repSites;
	private @Value("${de.heuboe.nrw.sitecfg.svc.rep.creation.mode:file}")	String		repCreationMode;
	private @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.view:true}") boolean propagateInvalidView;
	private @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.site:true}") boolean propagateInvalidSite;
	private @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.missingInXml:false}") boolean missingInXml;
	private @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.missingInDb:true}") boolean missingInDb;
	private @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.likeSoap:false}") boolean likeSoap;
	private Model model = null;
	private Validator validator = null;
	// -----------------------------------------------------------------------------------------------------------------------------
	// main entry point
	// ------------------------------------------------------------------------------------------------------------------------------
	public static void main( String[] args ) {
		Thread.setDefaultUncaughtExceptionHandler(( thread, e ) -> {
			log.error("Uncaught exception from thread [{}]:", thread.getName(), e);
			System.exit(1);
		});
		SpringApplication.run(Application.class, args);
	}
	// --------------------------------------------------------------------------------------------
/*	@GrpcClient("CfgServiceNrw") private ConfigServiceGrpc.ConfigServiceBlockingStub	cfgNrwStub	= null;
	@GrpcClient("CfgServiceAsf") private ConfigServiceGrpc.ConfigServiceBlockingStub	cfgAsfStub	= null;
	@GrpcClient("CfgServiceVzh") private ConfigServiceGrpc.ConfigServiceBlockingStub	cfgVzhStub	= null;
	@Bean
	CfgServiceClient cfgServiceClient() {
		return new CfgServiceClient(cfgNrwStub, cfgAsfStub, cfgVzhStub);
	}
	// --------------------------------------------------------------------------------------------
	@GrpcClient("GeoManagerNrw") private GeoManagerGrpc.GeoManagerBlockingStub			geoNrwStub	= null;
	@GrpcClient("FeatureSvcNrw") private FeatureServiceGrpc.FeatureServiceBlockingStub	feaNrwStub	= null;
	@GrpcClient("GeoManagerAsf") private GeoManagerGrpc.GeoManagerBlockingStub			geoAsfStub	= null;
	@GrpcClient("FeatureSvcAsf") private FeatureServiceGrpc.FeatureServiceBlockingStub	feaAsfStub	= null;
	@Bean
	GeoManagerClient geoManagerClient() {
		return new GeoManagerClient(geoNrwStub, geoAsfStub, feaNrwStub, feaAsfStub);
	}*/
//	@Bean
//	GeoModel geoModel() {
//		return new GeoModel(geoNrwStub, feaNrwStub);
//	}
	// --------------------------------------------------------------------------------------------
    @Bean(name = "Model")
	Model getModel() {
		if( model == null ) {
			model = createModel();
		}
		return model;
	}
    // the Validator will tie together the configurations given in the jar files and the corresponding information from the cfgService
    // further more a lot of checks concerning the validity of the jar files will be performed
//    @Bean(name = "Validator")
/*    public Validator getValidator(
    	            @Qualifier("Model") Model model,
    	   	@Value( "${grpc.server.port:9090}") int grpcPort,
            @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.view:true}") boolean propagateInvalidView,
            @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.site:true}") boolean propagateInvalidSite,
            @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.missingInXml:false}") boolean missingInXml,
            @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.missingInDb:true}") boolean missingInDb,
            @Value( "${de.heuboe.nrw.guisvc.sitecfgsvc.propagateInvalid.likeSoap:false}") boolean likeSoap
            ) {
        validator = new Validator(model, getCfgModel(), getCompareServices(grpcPort), propagateInvalidView, propagateInvalidSite, missingInXml, missingInDb, likeSoap);
        return validator;
    }*/
	// -----------------------------------------------------------------------------------------------------------------------------
    private Model createModel() {
		switch(CreationMode.create(repCreationMode)) {
			default  : return new Model(repRoot);
			case FILE: return new CreatorFile(repRoot, repSites, logRoadSegSvgs, saveRoadSegSvgs).create();
//			case SOAP: return new CreatorSoap(repRoot, siteCfgServiceProxy()).create();
		}
	}
    private CompareServices getCompareServices( int grpcPort ) {
		return compareServices ? new CompareServices(siteCfgServiceProxy(), "localhost", grpcPort) : null;
	}
/*	private ICfgModel getCfgModel() {
		switch(CfgSvcType.create(cfgSvcType, CfgSvcType.NRW)) {
			default:
			//case ZST: return cfgModelZst();
			//case NRW: return cfgModelNrw();
			//case CHB: return cfgModelChb();
		}
	}*/
/*	private ICfgModel cfgModelZst() {
		return new de.heuboe.zst.model.CfgModel((de.heuboe.zst.CfgService)makeWsProxy(de.heuboe.zst.CfgService.class, cfgServiceSoapAddress));
	}
	private ICfgModel cfgModelNrw() {
		return new de.heuboe.nrw.srv.model.CfgModelNrw((de.heuboe.nrw.guisvc.cfginterface.CfgService)makeWsProxy(de.heuboe.nrw.guisvc.cfginterface.CfgService.class, cfgServiceSoapAddress));
	}
	private ICfgModel cfgModelChb() {
    	return new de.heuboe.nrw.srv.model.ConfigModel(cfgNrwStub);
    }*/
//	private ICfgModel cfgModelNrw() {
//		String nsUri = "http://cfginterface.guisvc.nrw.heuboe.de/";
//		String wsdl = String.format("http://%s:%s/CfgService?wsdl", cfgSvcNrwHost, cfgSvcNrwPort);
//		String host = "CfgServiceService";
//		String port = "CfgServicePort";
//		try {
//			Service service = Service.create(new URL(wsdl), new QName(nsUri, host));
//			de.heuboe.nrw.guisvc.cfginterface.CfgService cfgService = service.getPort(new QName(nsUri, port), de.heuboe.nrw.guisvc.cfginterface.CfgService.class);
//	    	return new de.heuboe.nrw.srv.model.CfgModelNrw(cfgService);
//		} catch( Exception e ) {
//			log.error(String.format("CfgLookup: error connecting to cfgService(wsdl='%s', nsUri='%s', host='%s', port='%s': '%s'", wsdl, nsUri, host, port, e.toString()));
//		}
//		return null;
//    }
	// -----------------------------------------------------------------------------------------------------------------------------
	private SiteCfgService siteCfgServiceProxy() {
		return (SiteCfgService)makeWsProxy(SiteCfgService.class, siteCfgSvcSoapAddress);
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	Object makeWsProxy( Class<?> clazz, String url ) {
		log.info("Creating ws proxy [{}] url [{}]", clazz.getSimpleName(), url);
		return makeWsProxyFactory(clazz, url).create();
	}
	JaxWsProxyFactoryBean makeWsProxyFactory( Class<?> clazz, String url ) {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(clazz);
		factory.setAddress(url);
		addLoggingInterceptors(factory);
		return factory;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	Object makeWsServer( Class<?> clazz, String url, Object impl ) {
		log.info("Creating ws server [{}] url [{}] impl [{}]", clazz.getSimpleName(), url, impl.getClass().getSimpleName());
		return makeWsServerFactory(clazz, url, impl).create();
	}
	JaxWsServerFactoryBean makeWsServerFactory( Class<?> clazz, String url, Object impl ) {
		JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
		factory.setServiceBean(impl);
		factory.setServiceClass(clazz);
		factory.setAddress(url);
		addLoggingInterceptors(factory);
		return factory;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	void addLoggingInterceptors( AbstractBasicInterceptorProvider factory ) {
		if( logSoapIn ) {
			factory.getInInterceptors().add(new LoggingInInterceptor());
		}
		if( logSoapOut ) {
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
		}
	}
	// ------------------------------------------------------------------------------------------------------------------------------
//	@KafkaListener(topics = "#{'${spring.kafka.topics.LosDetSite}'.split(',')}")
//	public void readLosDataDetSites( ConsumerRecord<?, byte[]> message ) {
//		try {
//			String topic = message.topic();
//			byte[] payload = message.value();
//			log.info("Received messages on topic {}: {}", topic, payload);
//			log.info("msg: {}", message);
//			LosDetSites data = PLosDetSites.to(PLosDetSites.fromBytes(payload));
//			log.info(data.toString());
////			data.getDataList().forEach(o -> {
////				log.info(o.toString());
////			});
//			new Validator(model, getCfgModel(), null, propagateInvalidView, propagateInvalidSite, missingInXml, missingInDb, likeSoap);
//		} catch( Exception e ) {
//			log.error(e.getMessage(), e);
//		}
//	}
	@KafkaListener(topics = "#{'${spring.kafka.topics.DataChange}'.split(',')}")
	public void readDataChange( ConsumerRecord<?, byte[]> message ) {
		try {
			String topic = message.topic();
			byte[] payload = message.value();
			log.info("Received messages on topic {}: {}", topic, payload);
			log.info("msg: {}", message);
//			DataChanges data = PDataChanges.to(PDataChanges.fromBytes(payload));
//			log.info(data.toString());
		} catch( Exception e ) {
			log.error(String.format("readDataChange failed: '%s'", e.getMessage()), e);
		} finally {
	        validator.loadConfig("DataChange");
		}
	}
	// ------------------------------------------------------------------------------------------------------------------------------
}
