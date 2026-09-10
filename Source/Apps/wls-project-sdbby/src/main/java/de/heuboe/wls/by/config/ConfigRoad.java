package de.heuboe.wls.by.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.by.RoadShapefileWriter;
import de.heuboe.wls.by.TmcFilterSdbby;
import de.heuboe.wls.data.roadmap.RoadClass;
import de.heuboe.wls.plugin.roadmap.RoadComponentBuilderConnect;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.roadmap.RoadComponentBuilderStd;
import de.heuboe.wls.roadmap.RoadNet;
import de.heuboe.wls.roadmap.RoadComponentBuilderStd.IdCreatorShortOrientation;
import de.heuboe.wls.util.WlsException;

/**
 * 
 * Road configuration
 * 
 * @author peters
 *
 */
@Configuration
public class ConfigRoad {
    private static final Logger LOGGER = Logger.getLogger(ConfigRoad.class.getName());
    private static final String STD_PATTERN = "[A] *[0-9]+[A-Za-z]?";
    
    private static List<String> addRouteNums = Arrays.asList( 
            "ST2584",
            "ST2580",
            "ST2082",
            "B505",
            "B471",
            "B304",
            "B2",
            "B17",
            "B15n",
            "B22",
            "ST2088",
            "ST2007",
            "B310",
            "B2r",
            "B13" );

    @Bean
    TmcFilterSdbby tmcFilter() {
        return new TmcFilterSdbby( addRouteNums );
    }

    /**
     * This bean creates an instance of the RoadComponentBuilderConnect
     * 
     */
    @Bean
    RoadComponentBuilderConnect roadComponentBuilder(RoadMapPlugin roadMapPlugin, RoadNet roadNet) throws WlsException {
        try {
            RoadComponentBuilderConnect rcb = new RoadComponentBuilderConnect( roadMapPlugin, 
                                                                               roadNet, 
                                                                               new IdCreatorShortOrientation(),
                                                                               1, 
                                                                               STD_PATTERN, 
                                                                               RoadClass.ROAD_CLASS_3, 
                                                                               true, 
                                                                               true,
                                                                               addRouteNums.stream().collect( Collectors.toSet() ) );
            
            rcb.setRoadIdsIgnore(new ArrayList<>() );
            rcb.connectRoadComponents();
            rcb.initGetterCache();
            
            return rcb;
        } catch (Exception ex) {
            LOGGER.error(CallStack.getStackTraceAsString(ex));
            throw new WlsException("Error(s) in road creation " + ex.getMessage());
        }
        
    }
    
    @Bean
	RoadShapefileWriter roadShapefileWriter( @Value("${road.shapeFileDir:}") String roadShapeFileDir,
			                                RoadMapPlugin roadMapPlugin, 
			                                RoadComponentBuilderStd roadManager) {
    	return new RoadShapefileWriter( roadShapeFileDir, roadMapPlugin, roadManager );
    }
	

}
