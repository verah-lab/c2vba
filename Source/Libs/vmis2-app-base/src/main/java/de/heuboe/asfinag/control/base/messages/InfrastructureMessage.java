package de.heuboe.asfinag.control.base.messages;

import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import eu.vmis_ehe.vmis2.configservice.TunnelDirection;
import eu.vmis_ehe.vmis2.configservice.pojo.PCfgAq;
import eu.vmis_ehe.vmis2.configservice.pojo.PCfgMq;
import eu.vmis_ehe.vmis2.geomanager.features.pojo.PGeoFeature;

import java.util.List;
import java.util.Map;

/**
 * class holds class to attach infrastructure objects to an app infrastructure from system class.
 */

public class InfrastructureMessage {

    private InfrastructureMessage() {

    }

    /**
     * Class holds infrastructure object that should be published.
     *
     * @param infrastructureObjects Infrastructure objects.
     */
    public record InfrastructureObjectToPublish(Map<String, List<InfrastructureObject>> infrastructureObjects) {
    }

    /**
     * Class holds tunnel directions that should be published.
     *
     * @param tunnels Tunnel directions.
     */
    public record TunnelDirectionsToPublish(List<TunnelDirection> tunnels) {
    }

    /**
     * Class holds config service display panels that have no relation to an geo feature.
     *
     * @param cfgAqs Config service display panels.
     */
    public record CfgAqsWithoutGeoFeature(List<PCfgAq> cfgAqs) {
    }

    /**
     * Class holds config service display panels that have no road references.
     *
     * @param cfgAqs Config service display panels.
     */
    public record CfgAqsWithoutGeoRoadReference(List<PCfgAq> cfgAqs) {
    }

    /**
     * Class holds config service detection sites that have no relation to an geo feature.
     *
     * @param cfgMqs Config service detection sites.
     */
    public record CfgMqsWithoutGeoFeature(List<PCfgMq> cfgMqs) {
    }

    /**
     * Class holds config service detection sites that have no road references.
     *
     * @param cfgMqs Config service detection sites.
     */
    public record CfgMqsWithoutGeoRoadReference(List<PCfgMq> cfgMqs) {
    }

    /**
     * Class holds geo features that have no relation to an config service object.
     *
     * @param geoFeatures Geo features.
     */
    public record GeoFeatureWithoutCfgAqs(List<PGeoFeature> geoFeatures) {
    }

    /**
     * Exception message to throw an exception for testing.
     *
     * @param throwException Indicates if an exception should be thrown.
     */
    public record ExceptionMsg(boolean throwException) {
    }

}
