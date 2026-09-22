package de.heuboe.asfinag.vmis2.infrastructure.types;


import lombok.Getter;

/**
 * Class for infrastructure object reference types
 *
 */
public final class ReferenceTypes {

    /**
     * Road (Straße).
     */
    public static final String ROAD                         = "Road";
    /**
     * Route station (Streckenstation).
     */
    public static final String ROUTE_STATION                = "RouteStation";
    /**
     * Display panel (Anzeigetafel).
     */
    public static final String DISPLAY_PANEL                = "DisplayPanel";
    /**
     * Detection site (Messquerschnitt / MQ).
     */
    public static final String DETECTION_SITE               = "DetectionSite";
    /**
     * Lane (Fahrstreifen).
     */
    public static final String LANE                         = "Lane";
    /**
     * Environmental data (Umfelddaten / UFD).
     */
    public static final String ED_SENSOR                    = "EdSensor";
    /**
     * Environmental data (Umfelddaten / UFD).
     */
    public static final String ED_SENSOR_TYPE               = "UdeType";
    /**
     * Traffic control technology (Verkehrsleittechnik / VLT).
     */
    public static final String TCT_SENSOR                   = "TctSensor";
    /**
     * Traffic control technology (Verkehrsleittechnik / VLT).
     */
    public static final String TCT_SENSOR_TYPE              = "VltType";
    /**
     * Variable sign generator (Wechselzeichengeber / WZG).
     */
    public static final String VSG_SENSOR                   = "VsgSensor";

    /**
     * Communication controller (Kommunikationsrechner Inselbus / KRI)
     */
    public static final String COMMUNICATION_CONTROLLER = "CommunicationController";

    /**
     * Tunnel direction (Tunnelröhre / )
     */
    public static final String TUNNEL_DIRECTION = "TunnelDirection";

    /**
     * Junction (Anschlussstelle / )
     */
    public static final String JUNCTION = "Junction";


    /**
     * SBA AQ (AQ or EFQ)
     */
    @Getter
    public static final String SBA_AQ = "SBA_AQ";

    /**
     * WTA AQ (WTA or WTV)
     */
    @Getter
    public static final String WTA_AQ = "WTA_AQ";

    /**
     * Other AQ (not AQ, EFQ, WTA or WTV)
     */
    @Getter
    public static final String OTHER_AQ = "OTHER_AQ";

    /**
     * AQ from geo manager could not find in the config service
     */
    @Getter
    public static final String GEO_AQ_WITHOUT_CFG_AQ = "GEO_AQ_WITHOUT_CFG_AQ";


    /**
     * AQ from geo manager could not find in the config service
     */
    @Getter
    public static final String GEO_AQ_WITHOUT_ROAD_REF = "GEO_AQ_WITHOUT_ROAD_REF";

    /**
     * AQ from config service could not find in the geo manager
     */
    @Getter
    public static final String CFG_AQ_WITHOUT_GEO_AQ = "CFG_AQ_WITHOUT_GEO_AQ";
    /**
     * AQ from config service, has geo feature but no road reference in geo manager
     */
    @Getter
    public static final String CFG_AQ_WITHOUT_GEO_ROAD_REF = "CFG_AQ_WITHOUT_GEO_ROAD_REF";

    /**
     * Tunnel from geo manager could not find in the config service
     */
    @Getter
    public static final String GEO_TUNNEL_WITHOUT_CFG_TUNNEL = "GEO_TUNNEL_WITHOUT_CFG_TUNNEL";

    /**
     * Tunnel from config service could not find in the geo manager
     */
    @Getter
    public static final String CFG_TUNNEL_WITHOUT_GEO_TUNNEL = "CFG_TUNNEL_WITHOUT_GEO_TUNNEL";


    /**
     *  InfrastructureObjects to publish to broker.
     */
    @Getter
    public static final String INFRASTRUCTURE_TO_PUBLISH = "INFRASTRUCTURE_TO_PUBLISH";

    /**
     * Infrastructure holds other infrastructure objects, e.x. config service or Geo Manager objects, to publish to broker.
     */
    @Getter
    public static final String OTHER_INFRASTRUCTURE_TO_PUBLISH = "OTHER_INFRASTRUCTURE_TO_PUBLISH";

    /**
     * Tunnel from geo manager without road ref
     */
    @Getter
    public static final String GEO_TUNNEL_WITHOUT_ROAD_REF = "GEO_TUNNEL_WITHOUT_ROAD_REF";

    /**
     * junction from geo manager has no road ref
     */
    @Getter
    public static final String GEO_JUNCTION_WITHOUT_ROAD_REF = "GEO_JUNCTION_WITHOUT_ROAD_REF";


    private ReferenceTypes() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Return reference string of roads.
     * @return reference string of roads
     */
    public static String getRoadReferenceType() {
        return ROAD;
    }

    /**
     * Return reference string of route stations.
     * @return reference string of route stations
     */
    public static String getRouteStationReferenceType() {
        return ROUTE_STATION;
    }

    /**
     * Return reference string of display panel.
     * @return reference string of display panel.
     */
    public static String getDisplayPanelReferenceType() {
        return DISPLAY_PANEL;
    }

    /**
     * Return reference string of detection sites.
     * @return reference string of detection sites
     */
    public static String getDetSiteReferenceType() {
        return DETECTION_SITE;
    }

    /**
     * Return reference string of lanes.
     * @return reference string of lanes
     */
    public static String getLaneType() {
        return LANE;
    }

    /**
     * Return reference string of tunnel directions.
     * @return reference string of tunnel directions
     */
    public static String getTunnelDirectionType() {
        return TUNNEL_DIRECTION;
    }

    /**
     * Return reference string of junction.
     * @return reference string of junction
     */
    public static String getJunctionType() {
        return JUNCTION;
    }

}

