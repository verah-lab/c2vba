package de.heuboe.c2vba.datex2.kafka;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import de.heuboe.util.JAXBUtil;


/**
 * 
 * Implements Resolver for D2LogicalModel
 * 
 * @author peters
 *
 * @param <T>
 */
public class PublicationResolver<T> implements Resolver<T>  {
	
	private Class<T> clasT;
	private String schema;
	private JAXBUtil jaxbUtil;
	
	// "getPayloadPublication"
	private Method methodPP;
	
	// "getPublicationTime"
	private Method methodPT;
	
	// "getExchange"
	private Method methodE;

	// "getSubscription"
	private Method methodS;
	
	// "getUpdateMethod"
	private Method methodUM;

	
	public PublicationResolver( Class<T> clasT, String schema, JAXBUtil jaxbUtil ) {
		this.clasT = clasT;
		this.schema = schema;
		this.jaxbUtil = jaxbUtil;
		
		try {
			methodPP = clasT.getDeclaredMethod("getPayloadPublication" );
			Class<?> classPP = methodPP.getReturnType();
			methodPT = classPP.getDeclaredMethod("getPublicationTime" );
			
			methodE = clasT.getDeclaredMethod("getExchange" );
			Class<?> classE = methodE.getReturnType();
			methodS = classE.getDeclaredMethod("getSubscription" );
			Class<?> classS = methodS.getReturnType();
			methodUM = classS.getDeclaredMethod("getUpdateMethod" );
			
		} catch (NoSuchMethodException | SecurityException e) {
		}

	}

	/**
	 * 
	 * In case of non snapshot publications the ID shall be extended by publication time
	 * 
	 */
	@Override
	public String getId(T object) throws C2VbaException {
		if( isSnapshot( object ) ) {
			return "PUB";
		}
		return "PUB_" + getTime( object ).getTime();
	}

	@Override
	public String getVersion(T object) {
		return "";
	}

	@Override
	public Date getTime(T object) throws C2VbaException {
		
		try {
			Object ppObject = methodPP.invoke( object );
			return (Date)methodPT.invoke( ppObject );
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
			throw new C2VbaException( C2VbaException.ERROR_CONVERSION, "Cannot get publication time",  ex );
		}
	}

	@Override
	public String getContent(T object) throws C2VbaException {
		synchronized( JAXBUtil.class ) {
			try {
				return jaxbUtil.getDocument( object, "d2LogicalModel", clasT );
			} catch (JAXBException ex ) {
				throw new C2VbaException( C2VbaException.ERROR_CONVERSION, "Error converting to string content",  ex );
			}
		}
	}

	@Override
	public T getObject(String content) throws C2VbaException {
		synchronized( JAXBUtil.class ) {
			try {
				return jaxbUtil.getObject( content );
			} catch (JAXBException ex ) {
				throw new C2VbaException( C2VbaException.ERROR_CONVERSION, "Error string content to object",  ex );
			}
		}
	}

	@Override
	public String getSchema(T object) {
		return schema;
	}

	@Override
	public boolean isSnapshot( T object ) throws C2VbaException { 
		try {
			Object eObject = methodE.invoke( object );
			Object sObject = methodS.invoke( eObject );
			if( sObject != null ) {
				Object umObject = methodUM.invoke( sObject );
				if( umObject != null ) {
					String ums = umObject.toString();
					return ums.equals( "SNAPSHOT" );
				}
			}
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
			throw new C2VbaException( C2VbaException.ERROR_CONVERSION, "Cannot get publication time",  ex );
		}
		
		return true; 
	}; 
	
	private int compareByPubTime( T o1, T o2 ) {
		try {
			return getTime(o1).compareTo( getTime(o2) );
		} catch (C2VbaException e) {
		}
		
		return 0;
	}

	public List<T> sortByTime( List<T> objects ) {
		return objects.stream().sorted( (o1,o2) -> compareByPubTime( o1, o2 ) ).collect( Collectors.toList() ); 
	}

	
	public int getElementCount( T object ) throws C2VbaException {
		return -1;
	}

}
