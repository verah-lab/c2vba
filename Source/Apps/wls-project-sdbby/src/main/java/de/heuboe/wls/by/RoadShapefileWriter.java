package de.heuboe.wls.by;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Geometry;

import de.heuboe.data.DataStore;
import de.heuboe.data.DataWriter;
import de.heuboe.data.Factory;
import de.heuboe.data.Type;
import de.heuboe.data.shp.ShpFactory;
import de.heuboe.data.shp.ShpType;
import de.heuboe.geo.data.GeoData;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.roadmap.RoadComponent;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.roadmap.RoadComponentBuilderStd;
import de.heuboe.wls.utils.CoordinateConverter;


/**
 * 
 * Road map shapefile writer
 * 
 * @author peters
 *
 */
public class RoadShapefileWriter {
    
    private static final Logger LOGGER = Logger.getLogger( RoadShapefileWriter.class );
    public static final String MAIN_ROAD_SHAPE_FILE_NAME = "Road.shp";
    
    private String roadShapeFileDir;
    private RoadMapPlugin roadMapPlugin;
    private RoadComponentBuilderStd roadManager;
    
    /**
     * 
     * Constructor
     * 
     * @param roadShapeFileDir			Shapefile directory
     * @param roadMapPlugin 			RoadMapPlugin
     * @param roadManager 				RoadComponentBuilderStd 	
     */
    public RoadShapefileWriter( String roadShapeFileDir, RoadMapPlugin roadMapPlugin, RoadComponentBuilderStd roadManager ) {
    	this.roadShapeFileDir = roadShapeFileDir;
    	this.roadMapPlugin = roadMapPlugin;
    	this.roadManager = roadManager;
    }
    
    /**
     * 
     * Writes a shapefile of roads
     * 
     * @param shapefileDir   File path of shapefile
     * @param roadMapPlugin  RoadMapPlugin
     * @param roadManager    RoadManager
     */
    public void writeShapefile(  ) {  // NOSONAR
    	
    	if( ( roadShapeFileDir == null ) || roadShapeFileDir.isEmpty() ) {
    		LOGGER.info( "No shapefile directory defined" );
    	}
    	
    	
        writeShapefile( this.roadShapeFileDir, MAIN_ROAD_SHAPE_FILE_NAME, this.roadMapPlugin, roadManager, true );
    }
    
    private void writeShapefile( String shapefileDir, 
                                 String shapefileName,
                                 RoadMapPlugin roadMapPlugin, 
                                 RoadComponentBuilderStd roadManager, 
                                 boolean mainRoad ) {  // NOSONAR
        
        int wlsSrid = roadMapPlugin.getRoadMap().getSrid();
        
        Map<String, RoadComponent>  rcs = roadManager.getRoadComponents(); 
        
        // Create files in temporary directory
        
        String filePath = shapefileDir + File.separator + "temp";
        File dir = new File(filePath);
        if( !dir.exists() && !dir.mkdirs() ) {
            LOGGER.error( "Cannot create shapefile temp directory + <" + filePath + ">!"); 
            return;
        }
        
        filePath += File.separator + shapefileName;
        
        new ShpFactory();
        Factory factory = Factory.Singleton.getInstance();
        Map<String, Type> members = new LinkedHashMap<>();
        
        members.put("Id", factory.getStringType(30));
        
        String tn = ShpType.getTypeName( filePath );
        Type type = factory.getType("shp", tn, members, "", "");
        Properties props = new Properties();
        props.put("de.heuboe.data.shp.geotype", "POLYLINE");
        props.put("de.heuboe.data.shp.srid", wlsSrid );
        DataStore store = factory.createNewDataStore(type, filePath, props);
        DataWriter writer = store.getWriter();

        for( RoadComponent rc : rcs.values()) {
            GeoData record = (GeoData) type.createData();
            
            String id = rc.getId();
            record.getMember("Id").setFromString(id);
            
            Geometry geom;
            try {
            	Location loc = roadMapPlugin.convertLocation( GeoLocation.class.getName(), rc.getLocation(), null );
            	if( loc instanceof GeoLocation ) {
	            	geom = ((GeoLocation)loc).getGeometry();
	                de.heuboe.geo.Geometry geogeo = CoordinateConverter.toGeoGeometry( geom );
	                record.setGeometry(geogeo);
	                writer.add(record);
            	}
            } catch (Fault e) {
                LOGGER.error( "No geometry for RoadComponent <" + id + ">" );
            } 
        }
        writer.close();
        
        
        // Move files to parent directory
        
        try( Stream<Path> walk = Files.walk( Paths.get( shapefileDir + File.separator + "temp" ) ) ) {
            
            walk.forEach( f -> 
            
                {
                    if( !f.toFile().isDirectory() ) {
                        String fn = f.getFileName().toString(); 
                        if( !fn.equals( shapefileName ) ) {
                            moveFile( shapefileDir, f );
                        }
                    }
                }
            );
            
         } catch (IOException e) {
             LOGGER.error( "Cannot move shapefiles to parent directory"); 
         }
        
        Path gipShp = Paths.get( shapefileDir + File.separator + "temp" + 
                                 File.separator + shapefileName );
        moveFile( shapefileDir, gipShp );
    }

    private void moveFile( String shapefileDir, Path f ) {
        String fn = f.getFileName().toString(); 
        Path temp = null;
        try {
            temp = Files.move( f, 
                               Paths.get( shapefileDir + File.separator + fn ),
                               StandardCopyOption.REPLACE_EXISTING );
        } catch( IOException ex ) {
            LOGGER.error( ex.toString() );
            temp = null;
        } 
          
        if(temp == null) { 
            LOGGER.error( "Cannot move <" + fn + "> to parent directory"); 
        }                     
        
    }
}
