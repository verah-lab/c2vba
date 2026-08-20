package de.heuboe.asfinag.vmis2.synchronize.vd;

import de.heuboe.asfinag.vmis2.synchronize.vd.actors.AlgoActor;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for System.exit(status)
 *
 *
 */
@Slf4j
public class SystemExit {
    /**
     * Wrapper method for system exit. Now you can mock the system exit for tests
     * 
     * @param status exit status
     */
    public void exit(int status) {

        log.error("FATAL ERROR - EXIT APPLICATION");
        System.exit(status);
    }
}
