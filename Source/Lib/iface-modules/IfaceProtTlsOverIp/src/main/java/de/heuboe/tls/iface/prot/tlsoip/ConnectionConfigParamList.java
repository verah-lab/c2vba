package de.heuboe.tls.iface.prot.tlsoip;

import de.heuboe.tls.iface.lib.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Objects of this class carry connection parameters for the TLS over IP protocal
 *
 * the key to an entry may be one of the paramNames disregarding case
 * this list of possibilities is mapped to a single string which is used as final key.
 * a map using these final keys constitutes a set of allowed parameters    
 */
@Slf4j
public class ConnectionConfigParamList {
    private static final String BAD_VALUE = "Bad value {} for {}";
    private static List< Pair<String, String> > paramNames;
    private static Map<String, String>        defaultParams;
    static {
        initDefaultList();
    }

    // K_ Stands for key
    public static final String         K_CLIENT           = "client";
    public static final String         K_SECURECONNECTION = "secureConnection";
    public static final String         K_CLIENTAUTH       = "clientAuth";

    public static final String         K_CONNECTDELAY     = "connectDelay";
    public static final String         K_CONNECTDURATION  = "connectDuration";

    public static final String         K_HELLODELAY       = "helloDelay";
    public static final String         K_HELLOTIMEOUT     = "helloTimeout";

    public static final String         K_RECEIPTCOUNT     = "receiptCount";
    public static final String         K_RECEIPTDELAY     = "receiptDelay";
    public static final String         K_RECEIPTTIMEOUT   = "receiptTimeout";
    public static final String         K_RECEIPTGRACE     = "receiptGrace";

    public static final String         K_RECONNECTDELAY   = "reconnectDelay";
    public static final String         K_LOGFILESIZE      = "logfileSize";
    public static final String         K_LOGFILEROTATE    = "logfileRotate";
    public static final String         K_LOGFILENAME      = "logfileName";

    public static final String         K_ID               = "id";

    public static final String         K_TCPPORT          = "tcpPort";
    public static final String         K_TCPPORTB         = "tcpPortB";
    public static final String         K_SERVERHOST       = "serverHost";
    public static final String         K_SERVERHOSTB      = "serverHostB";

    public static final String         K_OSI2PORT         = "osi2Port";
    public static final String         K_OSI2ADDRESS      = "osi2Address";
    
    public static final String         K_ACTIVE           = "active";
    
    // values for a certain object
    private final Map<String, String> paramList;
    
    @SuppressWarnings( "SpellCheckingInspection" )
    private static void initDefaultList() {
        paramNames = new LinkedList<>();
        defaultParams = new LinkedHashMap<>();

        // param name alternatives, may be mixed upper or lower case
        // @formatter:off
        paramNames.add( new Pair<>("CLIENT"                                                                   , K_CLIENT           ) );
        paramNames.add( new Pair<>(":SECURE_CONNECTION:SECURECONNECTION"                                      , K_SECURECONNECTION ) );
        paramNames.add( new Pair<>(":CLIENT_AUTHENTICATION:CLIENTAUTHENTICATION"                              , K_CLIENTAUTH       ) );
        
        paramNames.add( new Pair<>(":CONNECT_DELAY:CONNECTDELAY"                                              , K_CONNECTDELAY     ) );
        paramNames.add( new Pair<>(":CONNECT_DURATION:CONNECTDURATION"                                        , K_CONNECTDURATION  ) );
        
        paramNames.add( new Pair<>(":HELLO_DELAY:HELLODELAY"                                                  , K_HELLODELAY       ) );
        paramNames.add( new Pair<>(":HELLO_TIMEOUT:HELLOTIMEOUT"                                              , K_HELLOTIMEOUT     ) );
        
        paramNames.add( new Pair<>(":RECEIPT_COUNT:RECEIPTCOUNT"                                              , K_RECEIPTCOUNT     ) );
        paramNames.add( new Pair<>(":RECEIPT_DELAY:RECEIPTDELAY"                                              , K_RECEIPTDELAY     ) );
        paramNames.add( new Pair<>(":RECEIPT_TIMEOUT:RECEIPTTIMEOUT"                                          , K_RECEIPTTIMEOUT   ) );
        paramNames.add( new Pair<>(":RECEIPT_GRACE_TIME:RECEIPT_GRACETIME:RECEIPT_GRACETIME:RECEIPTGRACETIME" , K_RECEIPTGRACE     ) );
        
        paramNames.add( new Pair<>(":RECONNECT_DELAY:RECONNECTDELAY"                                          , K_RECONNECTDELAY   ) );
        paramNames.add( new Pair<>(":LOGFILE_SIZE:LOGFILESIZE"                                                , K_LOGFILESIZE      ) );
        paramNames.add( new Pair<>(":LOGFILE_ROTATE:LOGFILEROTATE"                                            , K_LOGFILEROTATE    ) );     
        paramNames.add( new Pair<>(":LOGFILENAME:LOGFILE_NAME"                                                , K_LOGFILENAME    ) );     

        paramNames.add( new Pair<>("ID"                                                                       , K_ID               ) );

        paramNames.add( new Pair<>(":TPCPORT:TCP_PORT"                                                        , K_TCPPORT          ) );
        paramNames.add( new Pair<>(":TCPPORTB:TCP_PORT_B:TCP_PORTB:TCPPORT_B"                                 , K_TCPPORTB         ) );
        paramNames.add( new Pair<>(":SERVERHOST:SERVER_HOST"                                                  , K_SERVERHOST       ) );
        paramNames.add( new Pair<>(":SERVERHOSTB:SERVER_HOST_B:SERVERHOST_B:SERVER_HOSTB"                     , K_SERVERHOSTB      ) );

        paramNames.add( new Pair<>(":OSI2PORT:OSI2_PORT"                                                      , K_OSI2PORT         ) );
        paramNames.add( new Pair<>(":OSI2ADDRESS:OSI2_ADDRESS"                                                , K_OSI2ADDRESS      ) );

        //
        defaultParams.put(K_CLIENT          , "true"   ); // if true this code tries to connect
        defaultParams.put(K_SECURECONNECTION, "false"  );
        defaultParams.put(K_CLIENTAUTH      , "false"  );
                                            
        defaultParams.put(K_CONNECTDELAY    , "0"      );
        defaultParams.put(K_CONNECTDURATION , "0"      );
                                            
        defaultParams.put(K_HELLODELAY      , "10"     );
        defaultParams.put(K_HELLOTIMEOUT    , "30"     );
                                            
        defaultParams.put(K_RECEIPTCOUNT    , "10"     );
        defaultParams.put(K_RECEIPTDELAY    , "10"     );
        defaultParams.put(K_RECEIPTTIMEOUT  , "30"     );
        defaultParams.put(K_RECEIPTGRACE    , "0"      );
                                            
        defaultParams.put(K_RECONNECTDELAY  , "60"     );
        defaultParams.put(K_LOGFILESIZE     , "100000" ); // size is measured in count of lines!
        defaultParams.put(K_LOGFILEROTATE   , "3"      ); // number of files to keep
        // @formatter:on
    }
    
    /**
     * Empty Constructor. Simply initialize a map.
     */
    public ConnectionConfigParamList() {
        paramList = new LinkedHashMap<>();
    }

    private ConnectionConfigParamList( Map<String, String> params ) {
        this.paramList = params;
    }

    public ConnectionConfigParamList getDefault() {
        return new ConnectionConfigParamList( ConnectionConfigParamList.defaultParams );
    }
    
    /**
     * Put a value under a given key into a map
     * @param keyIn The key of the value
     * @param value The value to be stored
     * @return Returns whether setting was successful
     */
    public boolean setParam( String keyIn, String value ) {
        String key = getRealParamName( keyIn );
        if ( null == key ) {
            return false;
        }
        this.paramList.put( key, value );
        return true;
    }
    
    public boolean setParam( Pair<String, String> nvParam ) {
        return setParam( nvParam.getFirst(), nvParam.getSecond() );
    }
    
    public Map<String, String> getParams() {
        return paramList;
    }
    
    /**
     * get the value of a certain key from a map of parameters
     * @param keyIn the key to retrieve
     * @return The value stored or null
     */
    public String getValue( String keyIn ) {
        String paramName = getRealParamName( keyIn );

        if ( null == paramName ) {
            // in case keyIn matches lm.second
            return null;
        }
        
        return this.paramList.get( paramName );
    }

    private String getRealParamName( String keyIn ) {
        String paramName = null;
        for ( Pair<String, String> lm : paramNames ) {
            String key = lm.getFirst();
            String realKey = lm.getSecond();
            
            if (keyIn.equalsIgnoreCase( realKey )) {
                return realKey;
            }
            
            if ( key.startsWith( ":" ) ) { // multiple strings possible
                String[] arr = key.substring( 1 ).split( ":" );
                for ( String str : arr ) {
                    if ( str.equalsIgnoreCase( keyIn ) ) {
                        paramName = realKey;
                        break;
                    }
                }
            } else {
                if ( key.equalsIgnoreCase( keyIn ) ) {
                    paramName = realKey;
                    break;
                }
            }
        }
        return paramName;
    }

    public ConnectionConfig getAsConnectionConfig() { // NOSONAR refactoring would be artificial. Very uniform code
        ConnectionConfig res = new ConnectionConfig();
        String s1;
        
        s1 = getValue( K_CLIENT );
        if ( null != s1) {
            try {
                res.setClient( Boolean.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_CLIENT );
            }
        }

        
        s1 = getValue( K_TCPPORT );
        if ( null != s1) {
            try {
                res.setTcpPort( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_TCPPORT );
            }
        }
        
        s1 = getValue( K_TCPPORTB );
        if ( null != s1) {
            try {
                res.setTcpPortB( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_TCPPORTB );
            }
        }
        
        s1 = getValue( K_SERVERHOST );
        if ( null != s1) {
            res.setServerHost( s1 );
        }
        
        s1 = getValue( K_SERVERHOSTB );
        if ( null != s1) {
            res.setServerHostB( s1 );
        }

        
        s1 = getValue( K_OSI2PORT);
        if ( null != s1) {
            try {
                res.setOsi2Port( Short.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_OSI2PORT );
            }
        }
        
        s1 = getValue( K_OSI2ADDRESS );
        if ( null != s1) {
            try {
                res.setOsi2Address( Short.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_OSI2ADDRESS );
            }
        }
        
        
        s1 = getValue( K_SECURECONNECTION );
        if ( null != s1) {
            res.setSecureConnection( Boolean.valueOf( s1 ) );
        }
        
        s1 = getValue( K_CLIENTAUTH );
        if ( null != s1) {
            res.setClientAuthentication( Boolean.valueOf( s1 ) );
        }


        s1 = getValue( K_CONNECTDELAY );
        if ( null != s1) {
            try {
                res.setConnectDelay( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_CONNECTDELAY );
            }
        }
        
        s1 = getValue( K_CONNECTDURATION );
        if ( null != s1) {
            try {
                res.setConnectDuration( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_CONNECTDURATION );
            }
        }
       

        s1 = getValue( K_HELLODELAY );
        if ( null != s1) {
            try {
                res.setHelloDelay( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_HELLODELAY );
            }
        }
        
        s1 = getValue( K_HELLOTIMEOUT );
        if ( null != s1) {
            try {
                res.setHelloTimeout( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_HELLOTIMEOUT );
            }
        }
        
        
        s1 = getValue( K_RECEIPTCOUNT );
        if ( null != s1) {
            try {
                res.setReceiptCount( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_RECEIPTCOUNT );
            }
        }
        
        s1 = getValue( K_RECEIPTDELAY );
        if ( null != s1) {
            try {
                res.setReceiptCount( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_RECEIPTDELAY );
            }
        }
        
        s1 = getValue( K_RECEIPTTIMEOUT );
        if ( null != s1) {
            try {
                res.setReceiptTimeout( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_RECEIPTTIMEOUT );
            }
        }
        
        s1 = getValue( K_RECEIPTGRACE );
        if ( null != s1) {
            try {
                res.setReceiptGraceTime( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_RECEIPTGRACE );
            }
        }
        
        
        s1 = getValue( K_RECONNECTDELAY );
        if ( null != s1) {
            try {
                res.setReconnectDelay( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_RECONNECTDELAY );
            }
        }
        
        s1 = getValue( K_LOGFILESIZE );
        if ( null != s1) {
            try {
                res.setLogFileSize( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_LOGFILESIZE );
            }
        }
        
        s1 = getValue( K_LOGFILEROTATE );
        if ( null != s1) {
            try {
                res.setHelloTimeout( Integer.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_LOGFILEROTATE );
            }
        }
        
        s1 = getValue( K_ACTIVE );
        if( null != s1 ) {
            try {
                res.setActive( Boolean.valueOf( s1 ) );
            } catch( NumberFormatException e ) {
                log.error( BAD_VALUE, s1, K_ACTIVE );
            }
        }
        
        return res;
    }

}
