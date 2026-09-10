package de.heuboe.wls.by.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 
 * Spring service readiness controller
 * 
 * @author peters
 *
 */
@Controller
public class ReadyController {
    
    private SystemStateProvider systemRunningProvider;
    
    /**
     * 
     * Constructor
     * 
     * @param systemRunningProvider   Knows if the system is running    
     */
    public ReadyController( SystemStateProvider systemRunningProvider ) {
        this.systemRunningProvider = systemRunningProvider;
    }

    /**
     * 
     * Check for readiness
     * 
     * @return readiness
     */
    @GetMapping("ready")
    public ResponseEntity<Void> isReady() {
        if ( systemRunningProvider.isRunning() ) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status( HttpStatus.SERVICE_UNAVAILABLE ).build();
    }
}
