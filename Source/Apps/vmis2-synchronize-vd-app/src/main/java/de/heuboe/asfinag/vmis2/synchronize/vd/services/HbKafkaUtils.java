package de.heuboe.asfinag.vmis2.synchronize.vd.services;

/**
 * Util methods for common handling.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 08.08.2019
 *
 */
public class HbKafkaUtils {
    
    private static final String REPLACE_PARANTHESIS_LEFT    = "_--";
    private static final String REPLACE_PARANTHESIS_RIGHT   = "--_";
    private static final String REPLACE_SPACE               = "_._";

    private HbKafkaUtils() {
        //Hide the implicit public constructor.
    }
    
    /**
     * Encode topic name.
     * 
     * @param topic     given decoded topic name
     * @return          encoded topic name string.
     */
    public static String encodeTopicName(String topic) {
        return topic
                .replace(" ", REPLACE_SPACE)
                .replace("(", REPLACE_PARANTHESIS_LEFT)
                .replace(")", REPLACE_PARANTHESIS_RIGHT);
    }
    
    /**
     * Decode topic name.
     * 
     * @param topic given encoded topic name.
     * @return      decoded topic name string.
     */
    public static String decodeTopicName(String topic) {
        return topic
                .replace(REPLACE_SPACE, " ")
                .replace(REPLACE_PARANTHESIS_LEFT, "(")
                .replace(REPLACE_PARANTHESIS_RIGHT, ")");
    }

}
