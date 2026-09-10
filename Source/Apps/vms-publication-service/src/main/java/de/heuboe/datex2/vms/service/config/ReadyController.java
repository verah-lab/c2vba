package de.heuboe.datex2.vms.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import de.heuboe.datex2.vms.service.kafka.WZGStellzustandReceiver;

/**
 * 
 * Spring service readiness controller
 * 
 * @author peters
 *
 */
@Controller
public class ReadyController {
    
	@Autowired
    private WZGStellzustandReceiver kafkaDataReceiver;
    
    /**
     * 
     * Check for readiness
     * 
     * @return readiness
     */
    @GetMapping("ready")
    public ResponseEntity<Void> isReady() {
        if ( kafkaDataReceiver.stateReceived() ) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status( HttpStatus.SERVICE_UNAVAILABLE ).build();
    }
}
