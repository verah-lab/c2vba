package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import java.util.Arrays;

import de.heuboe.arg.ArgParser;
import de.heuboe.arg.ArgParserError;
import de.heuboe.arg.Mand;
import de.heuboe.arg.Opt;
import de.heuboe.ddp.Connection;
import de.heuboe.ddp.DDPException;
import de.heuboe.log.Logger;

/**
 * 
 * Holds process's command line parameter  
 * 
 * 
 * @author peters
 *
 */
public class Parameter 
{
	private static final Logger LOGGER = Logger.getLogger( Parameter.class );
	
	private static Parameter instance = null;
	private ArgParser ap; 
	private String wlsUrl = null;
	
	private int servicePort;
	
	private Parameter( String[] args )
			throws SvcException
	{
		ap = new ArgParser( "d2MstLocalisation", "", Arrays.copyOf( args, args.length ) );
		
		try
		{
			ap.addArgumentDefinition( new Mand( "servicePort", "WebService-Port", "5711" ) );
			ap.addArgumentDefinition( new Mand( "wlsUrl", "WLS-Url", "http://0.0.0.0:8085/WebLocationServerSrv" ) );           // NOSONAR
			
			ap.parse();
			
			servicePort = ap.getValue( "servicePort" ).getAsInt();
			wlsUrl = ap.getValue( "wlsUrl" ).getAsString();
			
			this.connection = createConnection();
        } catch (ArgParserError ex) {
            String errMsg = "ArgParser exception: " + ex.getMessage();
            LOGGER.error(errMsg);

            throw new SvcException(SvcException.ERROR_ARG_PARSER, errMsg, ex);
        } catch (DDPException ex) {
            String errMsg = "Database exception: " + ex.getMessage();
            LOGGER.error(errMsg);

            throw new SvcException(SvcException.ERROR_ARG_PARSER, errMsg, ex);
        }
	}
	
    public int getServicePort() {
        return servicePort;
    }

    public String getWlsUrl() {
        return wlsUrl;
    }


	/**
	 * 
	 * Creates single instance of class
	 * 
	 * @param args process command line parameters
	 * @return single instance of class
	 * @exception SvcException Exception
	 */
    public static Parameter createInstance(String[] args) throws SvcException {
        if (instance == null) {
            instance = new Parameter(Arrays.copyOf(args, args.length)); // NOSONAR:
        }

        return instance;
    }

	/**
	 * 
	 * Returns single instance of class
	 * 
	 * @return single instance of class
	 */
	public static Parameter instance() {
		return instance;
	}
}
