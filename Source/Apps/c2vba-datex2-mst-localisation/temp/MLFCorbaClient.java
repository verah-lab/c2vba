package de.heuboe.by.c2vba.datex2.mst.localisation.server.zql;


import de.heuboe.corba.*;

/**
 * 
 * Location CORBA-Server-Client.  
 * 
 * @author peters
 *
 * @param <T> CORBA interface
 */
public class MLFCorbaClient<T> 
{
	// Corba Connection
	private T corbaService = null;
	private Class<?> ifaceClass = null;
	private String serviceName = null;
	private int maxRetries = 0;
	
	/**
	 * 
	 * Internal exception class
	 * 
	 * @author peters
	 *
	 */
	@SuppressWarnings("serial")
	public static class CORBAException extends Exception
	{
	    /**
	     * 
	     * Constructor
	     * 
	     */
	    public CORBAException() 
	    {
	        super();
	    }
	    
	    /**
	     * 
	     * Constructor
	     * 
	     * @param message      error message
	     */
	    public CORBAException(String message) 
	    {
	        super(message);
	    }
	    
	    /**
	     * 
	     * Constructor
	     * 
	     * @param message      error message
	     * @param cause        cause
	     */
	    public CORBAException(String message, Throwable cause) 
	    {
	        super(message, cause);
	    }
	}

	/**
	 * Legt einen ZSTCorbaClient an.
	 * 
	 * @param  corbaServiceNameKey
	 *            Schluessel der Konfigurationsvariablen, die den
	 *            Servicefestlegt.
	 * @param clazz
	 *            Interfaceklasse
     * @throws CORBAException
     *            CORBA exception             
	 */
	public MLFCorbaClient(final String  corbaServiceNameKey, final Class<?> clazz)  throws CORBAException
	{
		this( corbaServiceNameKey, null, 0, clazz );
	}


	/**
	 * Legt einen ZSTCorbaClient an.
	 * 
	 * @param corbaServiceNameKey
	 *            Schluessel der Konfigurationsvariablen, die den Service
	 *            festlegt.
	 * @param defaultName
	 *            Name des Service, falls die Konfigurationsvariable nicht
	 *            gefunden wird.
	 * @param maxRetries
	 *            Max. Zahl der Verbindungsversuche
	 * @param clazz
	 *            Interfaceklasse
	 * @throws CORBAException
	 *            CORBA exception             
	 */
	public MLFCorbaClient( final String corbaServiceNameKey,
			               final String defaultName, 
			               final int maxRetries, 
			               final Class<?> clazz ) throws CORBAException
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException(
					"Illegal argument 'clazz' for SpinCorbaService");
		}
		if( (corbaServiceNameKey == null ) || corbaServiceNameKey.isEmpty() ) 
		{
			if (defaultName == null || defaultName.isEmpty() )
			{
				throw new IllegalArgumentException(
						"Illegal argument 'corbaServicenameKey' for SpinCorbaService");
            } else {
                serviceName = defaultName;
            }
        } else {
            serviceName = corbaServiceNameKey;
        }
		this.maxRetries = maxRetries;

		try 
		{
			this.ifaceClass = getInterfaceClass(clazz);
        } catch ( ClassNotFoundException ex ) {
            throw new CORBAException( "Exception creating CORBA client", ex ); // NOSONAR
        }
	}

	/**
	 * Verbindet den CorbaClient mit CorbaServer
	 * 
	 * @throws CORBAException  CORBA exception 
	 */
	@SuppressWarnings("unchecked")
	public final void connect() 
			throws CORBAException
	{
		corbaService = null;
		try 
		{
			corbaService = (T) CorbaClient.getInterface( serviceName,
														 ifaceClass, maxRetries);
        } catch (Exception ex) {
            throw new CORBAException("Error initializing CORBA server access", ex);
        }
	}

	/**
	 * Gibt den CorbaService zurueck.
	 * 
	 * @return CorbaService
	 * 
	 * @throws NDWExportException NDW export exception 
	 */
	public T getService() 
			throws CORBAException 
	{
		if( corbaService == null )
		{
			connect();
		}

		return corbaService;
	}

	/**
	 * Setzt die CORBA-Verbindung zurueck.
	 */
	public final void resetConnection() 
	{
		corbaService = null;
	}

	/**
	 * Gibt das Klassenobjekt der Interfaceklasse eines CorbaServices zurueck.
	 * 
	 * @param clazz
	 *            Klassenobjekt der Holder-, Helper-, Operations-, oder
	 *            POA-KLasse.
	 * @return Klassenobjekt der Interfaceklasse
	 * @throws ClassNotFoundException
	 *             Die Interfaceklasse existiert nicht.
	 */
	public static Class<?> getInterfaceClass(Class<?> clazz)
			throws ClassNotFoundException 
	{
		String className = clazz.getName();
		String[] ends = { "Helper", "Holder", "Operations", "POA" };
		for (String end : ends) 
		{
			if (className.endsWith(end)) 
			{
				className = className.substring(0,
						className.length() - end.length());
				break;
			}
		}
		return Class.forName(className);
	}
}
