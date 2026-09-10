package de.heuboe.datex2.mst.builder.client;
/*
 * Created on 12.12.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author peters
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import org.omg.PortableServer.*;
import org.omg.CORBA.*;



public class InitPOA 
{
	public static boolean initPOA( ORB m_orb )
	{
		try
		{
			POA rootPOA = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
			
	/*		
			Policy[] tpolicy = new Policy[4];
			
			tpolicy[0] = rootPOA.create_lifespan_policy(
				   	LifespanPolicyValue.PERSISTENT );
			tpolicy[1] = rootPOA.create_implicit_activation_policy(
			  		ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );
			 tpolicy[2] = rootPOA.create_request_processing_policy(
			   		RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY );
			 tpolicy[3] = rootPOA.create_servant_retention_policy(
			   		ServantRetentionPolicyValue.RETAIN);
			   		
			rootPOA.		   		
	*/
			rootPOA.the_POAManager().activate( );
		}
		catch( Exception ex ) 
		{
			ex.printStackTrace();
			
			return false;
		}
		
		return true;
	}
}
