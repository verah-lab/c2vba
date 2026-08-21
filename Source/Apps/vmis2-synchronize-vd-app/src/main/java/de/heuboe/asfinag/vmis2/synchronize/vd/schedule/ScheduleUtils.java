package de.heuboe.asfinag.vmis2.synchronize.vd.schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Utils to handle quartz scheduling.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 08.08.2019
 *
 */
public class ScheduleUtils {
    
    private ScheduleUtils() {
        //hide implicit public constructor
    }

    /**
     * Calculate parts of cron expression by given seconds.
     * 
     * @param seconds   given seconds to calculate parts of cron expression.
     * @return  List of cron exp parts.
     */
    public static List<Integer> getCronExpParts(Integer seconds) {
        List<Integer> expParts = new ArrayList<>();
        
        int cronPart = 1;  //seconds = 1, minutes = 2, hours = 3
        boolean end = false;
        int rest = seconds;
        
        while(!end && cronPart <= 3) {
            Integer divider = null; 
            if(cronPart == 1) {
                divider = 60;
            } else if(cronPart == 2) {
                divider = 60;
                rest = rest / 60;
            } else if(cronPart == 3) {
                divider = 24;
                rest = rest / 60;
            }
            if(rest/divider > 0) {
                int mod = rest%divider;
                expParts.add(mod);
                rest = rest - mod;
                cronPart++;
            } else {
                expParts.add(rest);
                end = true;
            }
        }
        return expParts;
    }
    
    /**
     * Returns a loggable String for a given list of Integers.
     * 
     * @param ils   List of interval lengths
     * @return      a String
     */
    public static String getListString(List<Integer> ils) {
        StringBuilder sb = new StringBuilder();
        for(Integer il : ils) {
            sb.append(il + ", ");
        }
        return sb.toString();
    }
}
