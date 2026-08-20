package de.heuboe.asfinag.vmis2.synchronize.vd.data;

import java.util.List;

import java.util.Map;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLane;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedTrafficCategoriesLane;
import lombok.Value;

/**
 * ShortTerm data.
 */
@Value
public class ShortTermData {
    
    /**
     * map roadId to shortTerm collected data.
     */
    public Map<String, List<PShortTermCollectedDataLane>> road2ShortTermData;
    
    /**
     * map roadId to shortTerm traffic categories.
     */
    public Map<String, List<PShortTermCollectedTrafficCategoriesLane>> road2ShortTermTrafficCategories;
}
