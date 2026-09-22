package de.heuboe.asfinag.vmis2.infrastructure.utils;

import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;

import java.util.Comparator;

/**
 * class is to compare to infrastructure objects per log km from
 */
public class InfrastructureObjectComparator implements Comparator<InfrastructureObject> {


    @Override
    public int compare(InfrastructureObject o1, InfrastructureObject o2) {
        if (o1.getGeoReference().isPresent() && o2.getGeoReference().isPresent()) {
            return Double.compare(o1.getGeoReference().get().getValiditySection().getLogKmFrom(), o2.getGeoReference().get().getValiditySection().getLogKmFrom()); //NOSONAR check is present before
        } else {
            return -1;
        }

    }
}
