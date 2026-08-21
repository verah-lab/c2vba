package de.heuboe.asfinag.vmis2.tls.rcv.data.converter.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.heuboe.tls.rcv.data.cvtinterface.ConversionRegistryIf;

public class PseudoTlsRcvCvt {
    private static Logger LOGGER = LoggerFactory.getLogger( PseudoTlsRcvCvt.class );

    public static void init( ConversionRegistryIf registry ) {
        LOGGER.info( "init PseudoCvt" );
        LOGGER.info( "init PseudoCvt done" );
    }
}
