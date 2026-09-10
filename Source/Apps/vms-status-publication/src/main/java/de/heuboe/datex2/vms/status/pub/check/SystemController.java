package de.heuboe.datex2.vms.status.pub.check;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import de.heuboe.log.Logger;

/**
 * 
 * Spring service readiness controller
 * 
 * @author peters
 *
 */
@Controller
public class SystemController {
    
    private static final Logger LOGGER = Logger.getLogger(SystemController.class);

    /**
     * 
     * Constructor
     * 
     */
    public SystemController() {
    	//
    }
	
    
    /**
     * 
     * Activates data check
     * 
     * @return activated/de-activated-Message
     */
    @GetMapping("activateDataCheck")
    public ResponseEntity< String > activateDataCheck() {
        
    	LOGGER.info( "'activateDataCheck' called" );
    	
    	boolean isEnabled = ContentCheck.isEnabled();
   		ContentCheck.setEnabled( !isEnabled );
    	
        return new ResponseEntity<>( isEnabled ? "Data check de-activated" :  "Data check activated", HttpStatus.OK );
    }
}
