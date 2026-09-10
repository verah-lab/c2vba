package de.heuboe.srb.datex2.srp;

import java.util.Date;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.push.D2PubSenderMDM;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;

/**
 * 
 * Sender
 * 
 * @author peters
 *
 */
public class Sender
{
	@Autowired
	private D2PubSenderMDM mdmSender = null;

	@Autowired
	private ProfileConverter<D2LogicalModel,eu.datex2.schema._2._2_0.D2LogicalModel> profileConverter;
	
	/**
	 * 
	 * Sends publication
	 * 
	 * @param pubTime    	publication time
	 * @param pubCycle   	publication cycle
	 * @param failed     	success ?
	 * @param errMsg     	error message
	 * @param content    	publication content
	 * @param pubFile    	publication file
	 * @throws D2Exception	Exception
	 */
	public void send( Date pubTime, int pubCycle, 
					  boolean failed,
					  String errMsg,	
					  D2LogicalModel d2lm,
					  String pubFile )
			throws D2Exception
	{
		try {
			eu.datex2.schema._2._2_0.D2LogicalModel d2lmBase = profileConverter.convertS( d2lm );
			mdmSender.publish( d2lmBase );
		} catch (JAXBException ex ) {
			throw new D2Exception( D2Exception.D2_ERR_SEND_SOAP, "Error sending publication", ex );
		}
	}
}
