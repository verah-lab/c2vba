package de.heuboe.datex2.location.service.server;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * Provides ID generators
 * 
 * @author peters
 *
 */
public class Util
{
	private Util() {
	}
	
	/**
	 * 
	 * ID generator
	 * 
	 * @author peters
	 *
	 */
	public static class IdGenerator
	{
		private int nextId = 1;
		
		/**
		 * 
		 * Returns next ID
		 * 
		 * @return ID
		 */
		public int getNextId()
		{
			return nextId++;
		}
	}
	
	/**
	 * 
	 * ID generator on object ID base
	 * 
	 * @author peters
	 *
	 */
	public static class SubIdGenerator
	{
		private Map<Integer,Integer> nextId = new TreeMap<>();
		
		/**
		 * 
		 * Returns next ID for object with ID masterId
		 * 
		 * @param  masterId  Object ID
		 * @return ID
		 */
		public int getNextId( int masterId )
		{
			Integer subId = nextId.get( masterId );
			if( subId == null ) {
				subId = 1;
			} else {
				subId++;
			}
			
			nextId.put( masterId, subId );
			return subId;
		}
	}
}
