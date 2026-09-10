package de.heuboe.c2vba.datex2.region.receiver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.heuboe.c2vba.datex2.kafka.Producer;
import de.heuboe.c2vba.datex2.region.receiver.RegionConfigProps.RegionConfig;
import de.heuboe.datex2.base.ProfileConverter.DefaultProfileConverter;
import de.heuboe.datex2.base.comm.Datex2ConsumerServer;
import de.heuboe.datex2.base.comm.Server;
import de.heuboe.datex2.base.config.ConnectionParameter;
import de.heuboe.datex2.base.config.RemoteConfig;
import de.heuboe.datex2.base.config.RemoteConfig.AccessType;
import de.heuboe.datex2.base.config.RemoteConfig.ConsumerMode;
import de.heuboe.datex2.base.config.SchemaConfig;
import de.heuboe.datex2.consumer.Datex2ConsumerServerBase;
import de.heuboe.datex2.consumer.Datex2PushConsumer;
import de.heuboe.datex2.consumer.Datex2SoapConnectorFactory;
import de.heuboe.datex2.consumer.PublicationWorker;
import de.heuboe.datex2.exception.D2CommException;
import de.heuboe.datex2.exception.DefaultErrorHandler;
import de.heuboe.datex2.pull.PullClient;
import de.heuboe.datex2.pull.PullClientAutoConfiguration;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;


/**
 * 
 * Builds 
 * 
 * @author peters
 *
 */
public class Manager {
	
	private static final Logger LOGGER = Logger.getLogger( Manager.class );
	
	@Value("${de.heuboe.c2vba.datex2.region.receiver.pullInterval}") 
	private int pullInterval;
	
	@Value("${de.heuboe.c2vba.datex2.region.receiver.pubFileDir}") 
	private String pubFileDir;

	@Value("${de.heuboe.c2vba.datex2.region.receiver.keepInterval:6}") 
	private int keepInterval;
	
	
	@Value("${de.heuboe.c2vba.datex2.region.receiver.aliveIntervalSued:-1}") 
	private int aliveIntervalSued;

	@Value("${de.heuboe.c2vba.datex2.region.receiver.aliveIntervalNord:-1}") 
	private int aliveIntervalNord;
	
	@Value("${de.heuboe.datex2.connection.disableCNCheck:false}") 
	private boolean disableCNCheck;

	
	@Autowired
	private String generalD2Schema;
	
	@Autowired
	private JAXBUtil jaxbUtil;
	
	@Autowired
	Datex2SoapConnectorFactory datex2SoapConnectorFactory;
	
	@Autowired
	List< Producer<D2LogicalModel> > producerList;
	
	private List<Datex2ConsumerServer> servers = new ArrayList<>();
	
	@Autowired
	private RegionConfigProps regionConfigProps;
	
	
	/**
	 * 
	 * Starts all receivers
	 * 
	 * @throws D2CommException   Communication error
	 */
	public void start() throws D2CommException {
		
		ConnectionAliveObserver connectionAliveObserver = null;
		
		if( ( aliveIntervalSued > 0 ) && ( aliveIntervalNord > 0 ) ) {
			Map<String,Integer> region2RefreshInterval = new HashMap<>();
			region2RefreshInterval.put( Properties.SUED, aliveIntervalSued );
			region2RefreshInterval.put( Properties.NORD, aliveIntervalNord );
			
			connectionAliveObserver =new ConnectionAliveObserver( region2RefreshInterval );
		}
		
		Map<String,Producer<D2LogicalModel> > producers = producerList.stream()
				                                               .collect( Collectors.toMap( Producer::getName, Function.identity() ) );
		
		SchemaConfig sc = new SchemaConfig();
		PubResolver resolver = new PubResolver( D2LogicalModel.class, sc.getDatex2SchemaFile(), jaxbUtil ); 
		
		List<RegionConfig> regionConfigs = regionConfigProps.getRegionConfigs();
		
		for( RegionConfig regionConfig : regionConfigs ) {
			ConnectionParameter cp = new ConnectionParameter( regionConfig.getUrl(), 
															  regionConfig.getTrustStoreFile(), regionConfig.getTrustStorePassword(),
															  regionConfig.getKeyStoreFile(), regionConfig.getKeyStorePassword() );
			
			cp.setContext( regionConfig.getContext() );
			cp.setServerSchema( regionConfig.getServerSchema() );
			cp.setServerPort( regionConfig.getServerPort() );
			cp.setServerDomain( regionConfig.getServerDomain() );
			cp.setUseGzip( true );
			if( disableCNCheck ) {
				cp.setDisableCNCheck( true );
			}
			
			RemoteConfig rc = new RemoteConfig( AccessType.valueOf( regionConfig.getRemote()), sc.getDatex2SchemaFile(), cp );
			rc.setConsumerMode( ConsumerMode.valueOf( regionConfig.getConsumerMode() ) );
			Producer<D2LogicalModel> producer = producers.get( regionConfig.getName() );
			
			String pfd = Config.startDirectoryCleaner( pubFileDir, regionConfig.getName(), keepInterval );
			
			Datex2Producer worker = new Datex2Producer( producer, resolver, pfd, generalD2Schema );
			if( regionConfig.getName().toLowerCase().contains( Properties.SUED ) ) {
				worker.setRegion( Properties.SUED );
			} else {
				worker.setRegion( Properties.NORD );
			}
			worker.setObserver( connectionAliveObserver );
			
			if( rc.getConsumerMode() == ConsumerMode.PULL ) {
				rc.setContentEncoding( StandardCharsets.UTF_8.name() );
				PullClient<D2LogicalModel> pc = PullClientAutoConfiguration.createPullClient( rc, sc );
				pc.setName( regionConfig.getName() );
				pc.connect();
				Server server = new Server( pullInterval, pc );
				server.setWorkers( Arrays.asList( worker ) );
	
				servers.add( server );
			} else {
				DefaultProfileConverter<D2LogicalModel> pc = new DefaultProfileConverter<>();
				Datex2PushConsumer<D2LogicalModel> dpc = new Datex2PushConsumer<>( rc, pc, pullInterval, 
						                                                           Arrays.asList( new PublicationWorker(worker ) ), 
						                                                           new DefaultErrorHandler() );
				
				Datex2ConsumerServerBase<D2LogicalModel> dcsb = new Datex2ConsumerServerBase<>( dpc );
				
				datex2SoapConnectorFactory.startServer( rc );

				dcsb.connect();
				servers.add( dcsb );
			}
		}
		
		for( Datex2ConsumerServer dcs : servers ) {
			dcs.start();
		}
		
		LOGGER.info( "Manager started" );
	}
}
