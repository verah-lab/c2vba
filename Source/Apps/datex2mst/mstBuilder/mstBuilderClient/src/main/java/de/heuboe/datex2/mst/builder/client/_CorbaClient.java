package de.heuboe.datex2.mst.builder.client;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

/*
 * Created on 25.11.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author michaels
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class _CorbaClient {
	public static org.omg.CORBA.Object getNamedObject(ORB orb, String corbaClassName) throws Exception {
		if (orb == null)  throw new Exception("ORB not initialized");

		// get the root naming context
		org.omg.CORBA.Object objRef =
				orb.resolve_initial_references("NameService");

		// Use NamingContextExt instead of NamingContext. This is
		// part of the Interoperable naming Service.
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

		// resolve the Object Reference in Naming
		return ncRef.resolve_str(corbaClassName);
	}

	public org.omg.CORBA.Object getObject(ORB orb, String ior) throws Exception {
		if (orb == null)  throw new Exception("ORB not initialized");

		// resolve the Object Reference in Naming
		return orb.string_to_object(ior);
	}
}
