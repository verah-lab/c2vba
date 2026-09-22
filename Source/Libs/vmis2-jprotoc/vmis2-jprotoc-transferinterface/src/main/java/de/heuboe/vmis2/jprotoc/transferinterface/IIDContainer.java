package de.heuboe.vmis2.jprotoc.transferinterface;

/**
 * Marker for classes (usually pojos) that have a iid field.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public interface IIDContainer {

    /**
     * Gets the IID for this object. All elements that are send via Kafka must have a non-empty IID.
     *
     * @return The IID for this object.
     */
    String getIid();

}
