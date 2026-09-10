package de.heuboe.srb.datex2.srp;

import java.util.ArrayList;
import java.util.List;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import de.heuboe.log.Logger;
import de.heuboe.wls.data.wls.GeometryFeature;
import de.heuboe.wls.data.wls.Location;

/**
 * 
 * Location-Builder
 * 
 * @author peters
 *
 */
public class LocationBuilder
{
	/**
	 * 
	 * WGS84 coordinates
	 * 
	 * @author peters
	 *
	 */
	public static class WGS84Point
	{
		private double x;
		private double y;
		
		public WGS84Point( double x, double y )    // NOSONAR:
		{                                          // NOSONAR:   
			this.x = x;
			this.y = y;
		}
		
		public double x()							// NOSONAR:
		{                                           // NOSONAR:    
			return x;
		}
		
		public double y()							// NOSONAR:
		{                                           // NOSONAR:  
			return y;
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger( LocationBuilder.class );
	private static final int SRID_DEFAULT = 31463;
	private MathTransform transform = null;

	private int getSRID( Integer srid )
	{
		if( srid == null ) 
		{
			return SRID_DEFAULT;
		}
		
		return srid;
	}
	
	private void createTransform( int refSysKey )
	{
		try
		{
			int key = refSysKey;
			if( key == 0 ) 
			{
				key = SRID_DEFAULT;
			}
			
			// reference system is WGS84
			CoordinateReferenceSystem wgs84CRS = DefaultGeographicCRS.WGS84;
			
			CoordinateReferenceSystem gkCRS = CRS.decode("EPSG:" + key );
			
			// Retrieve transform operation
			DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();
			CoordinateOperation operation = trFactory.createOperation(gkCRS, wgs84CRS);
			
			transform = operation.getMathTransform();   // NOSONAR:
		} catch( FactoryException ex ) 
		{
			LOGGER.error( "Unable to init GeoTools !" );
			LOGGER.error( ex.toString(), ex );
			Util.exit( -1 );
		}
	}
	
	
	private MathTransform getTransform( int refSysKey )
	{
		if( transform == null )
		{
			createTransform( refSysKey );
		}
		
		return transform;
	}
	
	/////////////////////////////////////////////////////////////////
	
	public List<WGS84Point> getCoordinates( Geometry geom, int srid )
			throws SRPException
	{
		if(
				( geom == null ) 
				|| 
				( geom.getCoordinates() == null ) 
				||
				( geom.getCoordinates().length == 0 )
			  )
			{
				return null;
			}

			int refSysKey = getSRID( srid );
			
			List<WGS84Point> points = new ArrayList<>();
			
			for( Coordinate coor : geom.getCoordinates() )
			{
				try 
				{
					double[] sourceCoor = new double[3];
					sourceCoor[0] = coor.x;
					sourceCoor[1] = coor.y;
					sourceCoor[2] = 0; 			// z-value is 0
					
					double[] targetCoor = new double[3];
					getTransform( refSysKey ).transform(sourceCoor, 0, targetCoor, 0, 1);
					
					points.add( new WGS84Point( targetCoor[0], targetCoor[1] ) );
				} catch (TransformException ex) 
				{
					throw new SRPException( SRPException.ERROR_LOC, "Error converting coordinates", ex);
				}
			}
			
			return points;
	}	
	/**
	 * 
	 * Returns coordinates of location
	 * 
	 * @param loc
	 * @return
	 * @throws SRPException
	 */
	public List<WGS84Point> getCoordinates( Location loc )
			throws SRPException
	{
		if( loc instanceof GeometryFeature )
		{
			GeometryFeature gf = (GeometryFeature)loc;
			
			return getCoordinates( gf.getGeometry(), gf.getSrid() );

		} else
		{
			return null;
		}
	}

}
