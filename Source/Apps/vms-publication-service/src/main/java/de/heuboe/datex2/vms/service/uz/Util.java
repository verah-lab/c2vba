package de.heuboe.datex2.vms.service.uz;

import java.util.List;

import de.heuboe.datex2.vms.service.Equality;



/**
 * 
 * Util class
 * 
 * @author peters
 *
 *
 */
public final class Util 
{
	public static final Long CONN_TIMEOUT = 300000L;		// ms
	public static final String TEST_MODE_FLAG = "de.heuboe.hpa.ftp.download.mode.test";
	public static final String CFG_WZG_TYPE = "ObjectType.WZG";
	public static final String CFG_AQ_TYPE = "ObjectType.AQ";
	
	private Util()
	{
	}
	
	/**
	 * 
	 * Executes exit()
	 * 
	 * @param code  exit code
	 */
	public static final void exit( int code )
	{
		// Trying to ensure that logging messages are ejected  
		pause();

		System.exit( code );
	}
	
	/**
	 * 
	 * executes Sleep
	 * 
	 * @param ms  milliseconds
	 */
	public static final void sleep( int ms )
	{
		try
		{ 
			Thread.sleep( ms ); 
		} 
		catch( Throwable ex ) // NOSONAR: sleep without exception handling
		{					  // NOSONAR: sleep without exception handling
		}  
	}
	
	/**
	 * 
	 * executes Sleep for 5000 ms
	 * 
	 */
	public static final void pause()
	{
		try
		{ 
			Thread.sleep( 5000 ); 
		} 
		catch( Throwable ex ) // NOSONAR: sleep without exception handling
		{					  // NOSONAR: sleep without exception handling
		}  
	}
	
	/**
	 * 
	 * Throwable to String
	 * 
	 * @param ex   Throwable
	 * @return	   Error text		
	 */
	public static String toString( Throwable ex )
	{
		String msg = ex.getMessage();
		if( msg == null )
		{
			msg = Util.toString(ex);
		}
		 
		return msg;
	}
	
	/**
	 * 
	 * Checks for equality allowing null values  
	 * 
	 * @param <T>	Object type	
	 * @param o1	First object
	 * @param o2	Second object
	 * @return		true/false
	 */
	public static <T extends Equality> boolean isEqual( T o1, T o2 )
	{
		if( o1 == null ) {
			return ( o2 == null );
		}
		
		if( o2 == null ) {
			return ( o1 == null );
		}
		
		return o1.isEqual( o2 );
	}
	
	
	/**
	 * 
	 * Checks for equality of objects  
	 * 
	 * @param o1	First object
	 * @param o2	Second object
	 * @return		true/false
	 */
	public static boolean isEqual( Object o1, Object o2 )
	{
		if( o1 == null ) {
			return ( o2 == null );
		}
		
		if( o2 == null ) {
			return ( o1 == null );
		}
		
		return o1.equals( o2 );
	}
	
	/**
	 * 
	 * Checks for equality of lists  
	 * 
	 * @param <T>	Object type	
	 * @param l1	First list
	 * @param l2	Second list
	 * @return		true/false
	 */
	public static <T extends Equality> boolean isEqual( List<T> l1, List<T> l2 )
	{
		if( l1 == null ) {
			return ( l2 == null );
		}
		
		if( l2 == null ) {
			return ( l1 == null );
		}
		
		for( int i = 0; i < l1.size(); i++ )
		{
			if( !l1.get(i).isEqual( l2.get(i) ) ) {
				return false;
			}
		}
		
		return true;
	}
	
}
