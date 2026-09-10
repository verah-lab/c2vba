package de.heuboe.datex2.mst.builder.client;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

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
public class CorbaServer {
  static public String initNamedServerClass(
    ORB orb,
    String corbaClassName,
    org.omg.PortableServer.Servant impl)
    throws Exception {
    if (corbaClassName == null || corbaClassName.equals(""))
      return initServerClass(orb, impl);
    if (orb == null)
      throw new Exception("ORB not initialized");
    if (impl == null)
      throw new Exception("Servant may not be 'null'");

    // get reference to rootpoa & activate the POAManager
    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    rootpoa.the_POAManager().activate();

    // get object reference from the servant
    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(impl);

    // get the root naming context
    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

    // Use NamingContextExt which is part of the Interoperable
    // Naming Service (INS) specification.
    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

    // bind the Object Reference in Naming
    NameComponent path[] = ncRef.to_name(corbaClassName);
    ncRef.rebind(path, ref);

    return orb.object_to_string(ref);
  }

  static public String initServerClass(
    ORB orb,
    org.omg.PortableServer.Servant impl)
    throws Exception {
    if (orb == null)
      throw new Exception("ORB not initialized");
    if (impl == null)
      throw new Exception("Servant may not be 'null'");

    // get reference to rootpoa & activate the POAManager
    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    rootpoa.the_POAManager().activate();

    // get object reference from the servant
    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(impl);
    return orb.object_to_string(ref);
  }
}
