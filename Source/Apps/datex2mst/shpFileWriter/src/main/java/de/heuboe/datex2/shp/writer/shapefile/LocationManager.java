/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.heuboe.datex2.shp.writer.shapefile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;

import de.heuboe.base.geometry.base2d.GeometryLineOperations;
import de.heuboe.base.geometry.base2d.Polyline;
import de.heuboe.corba.CorbaClient;
import de.heuboe.geo.Coordinate;
import de.heuboe.geo.impl.CoordinateImpl;
import de.heuboe.locStatelessServer.locCoordinate;
import de.heuboe.locStatelessServer.locError;
import de.heuboe.locStatelessServer.locErrorSequence;
import de.heuboe.locStatelessServer.locXY;
import de.heuboe.locStatelessServer.manager;
import de.heuboe.locStatelessServer.managerHelper;
import de.heuboe.log.Logger;
import de.heuboe.util.CallStack;

/**
 *
 * @author peters
 */
public class LocationManager 
{
    private static final Logger LOGGER = Logger.getLogger( LocationManager.class );
    private manager locManager = null; 

    private MathTransform transform = null;
    
    private void init()
            throws FactoryException
    {
        // reference system is WGS84
        CoordinateReferenceSystem wgs84CRS = DefaultGeographicCRS.WGS84;
        // reference system Gauss Krueger
        CoordinateReferenceSystem gkCRS = CRS.decode("epsg:25832");

        // Retrieve transform operation
        DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();
        CoordinateOperation operation = trFactory.createOperation( gkCRS,
                                                                   wgs84CRS);
        transform = operation.getMathTransform();
    }
    
    public LocationManager()
            throws Exception
    {
        try
        {
            init();
        }
        catch( FactoryException ex ) 
        {
            throw new Exception( "Fehler bei Intialisierung der GeoTools" , ex);
        }
    }
    
    public void connect( String locSrvUrl )
            throws Exception
    {        if( locManager != null )
            return;
        
        try
        {
            locManager = managerHelper.narrow( CorbaClient.getNamedObject( locSrvUrl ) );
        }
        catch( Exception ex )
        {
        	LOGGER.error( CallStack.getStackTraceAsString( ex ) );
            throw new Exception( "Fehler bei der Verbindungsaufnahme zum LocationServer !", 
                                 ex );
        }
    }
    
    private boolean locationNotExists( locErrorSequence  ex ) 
    {
        locError[] errors = ex.errSequence; 
        if( errors != null )
        {
            for( locError error : errors )
            {
                if( error.code == 122005 )
                    return true;
            }
        }
        return false;
    }

    public Map<Integer, List<Coordinate> > localizeAll( Set<Integer> locIds )
            throws Exception
    {
        try
        {
            return localize( locIds );
        }
        catch( locErrorSequence  ex )
        {
            if( !locationNotExists(ex) )
            {
                locError[] errors = ex.errSequence; 
                String lError = "";
                if( errors != null )
                {
                    for( locError error : errors )
                    {
                        if( !lError.isEmpty() )
                            lError += ", ";
                        lError += error.message;
                    }
                }
                throw new Exception( "Fehler bei der Bestimmung der Koordinaten ! " +
                                            "[" + lError + "]" );
            }
            else
            {
                Map<Integer, List<Coordinate> > result = new HashMap<>();
                for( Integer locId : locIds )
                {
                    try
                    {
                        Set<Integer> sLocIds = new HashSet<>( Arrays.asList( locId ) );
                        Map<Integer, List<Coordinate> > sResult = localize( sLocIds );
                        result.putAll( sResult );
                    }
                    catch( locErrorSequence  sex )
                    {
                        LOGGER.error( sex.toString(), sex );
                    }
                }
                
                return result;
            }
        }
    }    
    
    private Map<Integer, List<Coordinate> > localize( Set<Integer> locIds )
            throws locErrorSequence
    {
        int[] locIdArray = new int[locIds.size() ];
        int i = 0;
        for( Integer locId : locIds )
        {
            locIdArray[i] = locId;
            i++;
        }

        Map<Integer, List<Coordinate> > result = new HashMap<>();

        locCoordinate[] coors = locManager.getCoordinates( locIdArray );
        
        GeometryLineOperations geometry = new GeometryLineOperations();
        
        for( locCoordinate coor : coors )
        {
            List<Coordinate> vCoors = new ArrayList<>();
            boolean validTransform = true;
            
            
            List<de.heuboe.base.geometry.base2d.Coordinate> d2Coors = new ArrayList<>();
            for( locXY xy : coor.coords )
            {
            	d2Coors.add( new de.heuboe.base.geometry.base2d.Coordinate( xy.x_coor, xy.y_coor ) );
            }
            Polyline moved = geometry.shiftRight( new Polyline( d2Coors ) , 20.0, true );
            
            List<de.heuboe.base.geometry.base2d.Coordinate> cos = moved.getPoints();
            for( de.heuboe.base.geometry.base2d.Coordinate co : cos )
            {
            	if( co == null )
            	{
            		co = null;
            	}
            	else
            	{
	                Coordinate vco = toWGS84( co );
	
	                if( vco == null )
	                {
	                    validTransform = false;
	                    break;
	                }
	
	                vCoors.add( vco );
            	}
            }

            if( validTransform )
                result.put( coor.id, vCoors);
        }

        return result;
    }
    
    public Coordinate toWGS84( locXY gdCoor ) 
    {
    	return toWGS84( gdCoor.x_coor, gdCoor.y_coor );
    }
    
    public Coordinate toWGS84( de.heuboe.base.geometry.base2d.Coordinate co ) 
    {
    	return toWGS84( co.getX(), co.getY() );
    }

    public Coordinate toWGS84( double x, double y ) 
    {
        try 
        {
            // Convert coordinate
            double[] sourceCoor = new double[3];
            sourceCoor[0] = x;
            sourceCoor[1] = y;
            sourceCoor[2] = 0; // z-value is 0
            double[] targetCoor = new double[3];
            // This is the transformation
            transform.transform(sourceCoor, 0, targetCoor, 0, 1);

            Coordinate coor = new CoordinateImpl();
            coor.setX( targetCoor[0] );
            coor.setY( targetCoor[1] );
            
            return coor;
        } 
        catch (Exception ex) 
        {
            LOGGER.error( "Error converting coordinates to WGS84:" );
            LOGGER.error( ex.getMessage(), ex );
            return null;
        }
    }
    
}
