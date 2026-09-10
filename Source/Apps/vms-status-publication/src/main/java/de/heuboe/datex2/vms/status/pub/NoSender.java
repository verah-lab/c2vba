package de.heuboe.datex2.vms.status.pub;

import java.util.Date;

import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.D2LogicalModel;


/**
 * 
 * Dummy-Versender von Publikationen
 *   
 * @author peters
 *
 */
public class NoSender extends Sender
{
	private static final Logger LOGGER = Logger.getLogger( NoSender.class );

	@Override
	public void send( Date pubTime, 
			          int pubCycle, 
			  		  String errMsg,	
			  		  D2LogicalModel d2lm,
			  		  String pubFile )
		throws D2Exception
	{
		LOGGER.info( "NoReceiver: ok" );
	}
}
