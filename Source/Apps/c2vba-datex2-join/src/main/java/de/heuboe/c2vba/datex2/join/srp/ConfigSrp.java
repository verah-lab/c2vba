package de.heuboe.c2vba.datex2.join.srp;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;

import de.heuboe.c2vba.datex2.join.Manager;
import de.heuboe.c2vba.datex2.join.Properties;
import de.heuboe.c2vba.datex2.join.ReceiverCreator;
import de.heuboe.c2vba.datex2.join.ReceiverT;
import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.kafka.listener.KafkaException;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;



/**
 * 
 * Spring configuration for strategic routing publications
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableKafka
@ComponentScan( basePackages = { "de.heuboe.c2vba.datex2.join",
        						 "de.heuboe.kafka.listener" } )   
@Profile( "SRP" )
public class ConfigSrp {

	private static final String SRP_SCHEMA_FILE = "schema/StrategicRouting_withSubscriptionLifecycle-c2vba.xsd";
	private static final String SRP_PACKAGE = "eu.datex2.schema._2._2_0.srp";
	
	@Bean 
	String generalD2SchemaFile( Properties props ) {
		String schema = SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE;
		String gd2sf = props.getGeneralD2Schema();
		
		if( gd2sf != null && !gd2sf.isEmpty() ) {
			schema = gd2sf; 
		}
		
		return schema;
	}
	
	@Bean
	ProfileConverter<D2LogicalModel,eu.datex2.schema._2._2_0.D2LogicalModel> profileConverter( String generalD2SchemaFile ) throws JAXBException {
		return new ProfileConverter<>( SRP_SCHEMA_FILE, 
				                       D2LogicalModel.class,
				                       generalD2SchemaFile, 
				                       eu.datex2.schema._2._2_0.D2LogicalModel.class );
	}
	
	@Bean 
	ReceiverCreator<D2LogicalModel> receiverCreator() {
		return new ReceiverCreator<>();
	}
	
	@Bean 
    @Qualifier("nord")
	ReceiverT<D2LogicalModel> receiverNord( Properties props, ReceiverCreator<D2LogicalModel> rc ) 
			   throws KafkaException, JAXBException {
		return rc.createRegionReceiver( props, "nord", props.getTopicNord(), D2LogicalModel.class, 
				                        SRP_SCHEMA_FILE, SRP_PACKAGE );              
	}

	@Bean 
    @Qualifier("sued")
	ReceiverT<D2LogicalModel> receiverSued( Properties props, ReceiverCreator<D2LogicalModel> rc  ) 
			   throws KafkaException, JAXBException {
		return rc.createRegionReceiver( props, "sued", props.getTopicSued(), D2LogicalModel.class, 
                                        SRP_SCHEMA_FILE, SRP_PACKAGE );              
	}
	
	@Bean
	Manager<D2LogicalModel> manager( String generalD2SchemaFile, 
			                         ReceiverT<D2LogicalModel> receiverNord, 
			                         ReceiverT<D2LogicalModel> receiverSued ) throws JAXBException {
		ResolverPubSrp resolverPub = new ResolverPubSrp();
		return new Manager<>( SRP_SCHEMA_FILE, 
							  generalD2SchemaFile,
				              SRP_PACKAGE, 
				              "/d2Template/emptyUpdateSrp.xml",
				              resolverPub, 
				              receiverNord, 
				              receiverSued );
	}
}
