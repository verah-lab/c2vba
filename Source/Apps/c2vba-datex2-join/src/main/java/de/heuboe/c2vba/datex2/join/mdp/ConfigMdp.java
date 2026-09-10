package de.heuboe.c2vba.datex2.join.mdp;

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
import eu.datex2.schema._2._2_0.mdp.D2LogicalModel;


/**
 * 
 * Spring configuration for MeasuredData publications
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableKafka
@ComponentScan( basePackages = { "de.heuboe.c2vba.datex2.join",
        						 "de.heuboe.kafka.listener" } )   
@Profile( "MDP" )
public class ConfigMdp {

	private static final String MDP_SCHEMA_FILE = "schema/MeasuredData_2017_01-00-00.xsd";
	private static final String MDP_PACKAGE = "eu.datex2.schema._2._2_0.mdp";
	
	@Bean
	ProfileConverter<D2LogicalModel,eu.datex2.schema._2._2_0.D2LogicalModel> profileConverter() throws JAXBException {
		return new ProfileConverter<>( MDP_SCHEMA_FILE, 
				                       D2LogicalModel.class,
				                       SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE,
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
				                        MDP_SCHEMA_FILE, MDP_PACKAGE );              
	}

	@Bean 
    @Qualifier("sued")
	ReceiverT<D2LogicalModel> receiverSued( Properties props, ReceiverCreator<D2LogicalModel> rc  ) 
			   throws KafkaException, JAXBException {
		return rc.createRegionReceiver( props, "sued", props.getTopicSued(), D2LogicalModel.class, 
                                        MDP_SCHEMA_FILE, MDP_PACKAGE );              
	}
	
	@Bean
	Manager<D2LogicalModel> manager( ReceiverT<D2LogicalModel> receiverNord, 
			                         ReceiverT<D2LogicalModel> receiverSued ) throws JAXBException {
		ResolverPubMdp resolverPub = new ResolverPubMdp();
		return new Manager<>( MDP_SCHEMA_FILE, 
				              SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE,
				              MDP_PACKAGE, 
				              "/d2Template/emptyUpdateMdp.xml",
				              resolverPub, 
				              receiverNord, 
				              receiverSued );
	}
}
