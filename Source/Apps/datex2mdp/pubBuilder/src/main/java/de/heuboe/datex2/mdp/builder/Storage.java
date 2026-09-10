package de.heuboe.datex2.mdp.builder;

import java.util.HashMap;
import java.util.HashSet;

import de.heuboe.datex2.config.persistence.Datex2MST;
import de.heuboe.datex2.config.persistence.Datex2MSTDataSource;
import de.heuboe.datex2.config.persistence.Datex2MSTItem;
import de.heuboe.datex2.config.persistence.Datex2MSTLocation;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import lombok.Data;

/**
 * Class to store all relevant data
 */
@Data
public class Storage {

    private List<Datex2MSTLocation> locations;
    private Map<Integer, List<Datex2MSTItem>> items;
    private List<Datex2MSTDataSource> dataSources;
    private java.util.Map<String, java.util.HashSet<Datex2MSTDataSource>> dataSourceMap = new HashMap<>();// ItemId, list von DataSources. Die Liste da LVEs mehrere haben können
    private java.util.Map<String, SensorData> data = new HashMap<>(); // ItemId, data
    private Datex2MST mst;

    /**
     * Constructor
     *
     * @param mst Datex2MST
     * @param dataSources list of Datex2MSTDataSource
     * @param locations list of Datex2MSTLocation
     * @param items list of Datex2MSTItem
     */
    public Storage(Datex2MST mst, List<Datex2MSTDataSource> dataSources, List<Datex2MSTLocation> locations, List<Datex2MSTItem> items) {
        this.mst = mst;
        this.locations = locations;
        this.dataSources = dataSources;
        this.items = items.groupBy(i -> i.getLocId());

        Map<String, List<Datex2MSTItem>> itemMap = items.groupBy(i -> i.getDkKey());
        Map<Integer, Datex2MSTLocation> locationsMap = locations.toMap(l -> l.getId().getId(), l -> l);
        Map<Integer, Datex2MSTDataSource> dsMap = dataSources.toMap(i -> i.getId().getId(), i -> i);

        for (List<Datex2MSTItem> itemList : itemMap.values()) {
            for (Datex2MSTItem item : itemList) {
                dataSourceMap.computeIfAbsent(locationsMap.get(item.getLocId()).get().getD2Id(), k -> new HashSet<>()).add(dsMap.get(item.getDkRef()).get());
            }
        }
    }
}
