package de.heuboe.c2vba.datex2.table.update;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.exception.D2CommException;
import de.heuboe.datex2.pull.PullClient;
import de.heuboe.datex2.push.PushClient;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;

public abstract class TableJoinerImpl<T> implements TableJoiner<T>  {

	private static final Logger LOGGER = Logger.getLogger( TableJoinerImpl.class );
	
	@Value("${de.heuboe.c2vba.datex2.table.update.correctEncoding:true}") 
	private boolean correctEncoding;
	
	protected ProfileConverter<T,eu.datex2.schema._2._2_0.D2LogicalModel> profileConverter;
	protected JAXBUtil jaxbUtil;
	private JAXBUtil jaxbUtilN;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @throws JAXBException	XML/XSD error
	 */
	protected TableJoinerImpl() throws JAXBException {
		jaxbUtilN = new JAXBUtil( SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE, 
								  SchemaUtil.DATEX2_SCHEMA_PACK, 
								  SchemaUtil.DATEX2_SCHEMA_NS,
								  false );
	}
	
	/**
	 * 
	 * Reads table publication from file
	 * 
	 * @param fileName
	 * @return
	 * @throws JAXBException
	 * @throws IOException 
	 */
	@Override
	public T readTable( String region, String fileName ) throws JAXBException, IOException {
		
		LOGGER.info( "Read " + region +" table from file '" + fileName + "'" );
		
		String content = FileUtils.readFileToString( new File( fileName ) , StandardCharsets.UTF_8 ); 
		return jaxbUtil.getObject( content );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T pullTable( PullClient<?> pc ) throws JAXBException, D2CommException {
		
		LOGGER.info( "Pull Nord table from MDM address '" + pc.getRemoteConfig().getUrl() + "'" );

		PullClient<eu.datex2.schema._2._2_0.D2LogicalModel> pullClient = (PullClient<eu.datex2.schema._2._2_0.D2LogicalModel>)pc;
		pullClient.connect();
		eu.datex2.schema._2._2_0.D2LogicalModel d2lm = pullClient.pull();
		
		if( correctEncoding ) {
			d2lm = correctEncoding( d2lm );
		}
		
		return profileConverter.convertT( d2lm );
	}
	
	private D2LogicalModel correctEncoding( D2LogicalModel d2lm ) throws JAXBException {
		String document = jaxbUtilN.getDocument( d2lm, "d2LogicalModel", D2LogicalModel.class );
		byte[] content = document.getBytes( StandardCharsets.ISO_8859_1 );
		document = new String( content, StandardCharsets.UTF_8 );
		return jaxbUtilN.getObject( document );
	}
	
	
	@Override
	public void pushJoinTable( T joinTable, PushClient pushClient ) throws D2CommException, JAXBException {
		
		LOGGER.info( "Push join table to MDM address '" + pushClient.getRemoteConfig().getUrl() + "'" );

		eu.datex2.schema._2._2_0.D2LogicalModel d2lm = profileConverter.convertS( joinTable );
		pushClient.connect();
		pushClient.push( d2lm );
	}
}
