package de.heuboe.datex2.mdp.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.heuboe.datex2.config.persistence.*;
import eu.datex2.schema._2._2_0.mdp.CountryEnum;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import java.io.IOException;
import java.time.Instant;

public class TestConfig {

    public static Properties getProps() {
        Properties props = new Properties();
        props.setSchemaModelBaseVersion("2");
        props.setCountry(CountryEnum.DE);
        props.setLanguage("de");
        props.setNationalId("DE-MDM-Bayerische Straßenbauverwaltung - Zentralstelle für Verkehrsmanagement");
        return props;
    }

    public static Storage storage(String mstId) throws IOException {
        Datex2MST mst = getMst(mstId);
            List<Datex2MSTDataSource> dataSource = getDataSourcesLve();
            Storage storage = new Storage(mst, dataSource, getLocation(), getItems());
            storage.setData(getDataLve(dataSource).toJavaMap());
            return storage;
    }

    private static List<Datex2MSTItem> getItems() {
        Datex2MSTItem item1 = new Datex2MSTItem();
        item1.setId(getId(1));
        item1.setLocId(1);
        item1.setDkId(1);
        item1.setDkRef(1);
        item1.setDkKey("item.item1.m.de");

        Datex2MSTItem item2 = new Datex2MSTItem();
        item2.setId(getId(2));
        item2.setLocId(1);
        item2.setDkId(2);
        item2.setDkRef(2);
        item2.setDkKey("item.item1.m.de");

        return List.of(item1);
    }

    private static List<Datex2MSTLocation> getLocation() {
        Datex2MSTLocation location1 = new Datex2MSTLocation();
        location1.setId(getId(1));
        location1.setD2Id("loc.loc1.m.de");

        Datex2MSTLocation location2 = new Datex2MSTLocation();
        location2.setId(getId(2));
        location2.setD2Id("loc.loc2.m.de");

        return List.of(location1);
    }

    private static Map<String, SensorData> getDataLve(List<Datex2MSTDataSource> dataSource) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(JSON_DATA);
        return HashMap.of("item.item1.m.de", newSensorData(jsonNode.get("data").get(0), dataSource.get(0)),
                "item.item2.m.de", newSensorData(jsonNode.get("data").get(1), dataSource.get(0)));
    }

    private static SensorData newSensorData(JsonNode node, Datex2MSTDataSource dataSource) {
        String id = node.get(dataSource.getIdColumn()).asText();
        Instant prozessTime = Instant.parse(node.get("processingTime").asText());
        int interval = node.get("intervalLength").asInt();
        io.vavr.collection.Map<String, Integer> data = HashMap.of(dataSource.getValueCol(), node.get(dataSource.getValueCol()).asInt());
        return new SensorData(id, prozessTime, dataSource.getValidInterval(), interval, data, null);
    }

    private static List<Datex2MSTDataSource> getDataSourcesLve() {
        Datex2MSTDataSource ds1 = new Datex2MSTDataSource();
        ds1.setId(getId(1));
        ds1.setDatakind("C2VBA-tlsin-LVEIntervalldatenKurzzeitInterpoliertOpt0");
        ds1.setValueCol("qKFZ");
        ds1.setIdColumn("id");
        ds1.setInvalidValues("-1");
        ds1.setD2BasicDataType("TrafficFlow");
        ds1.setValidInterval(900);

        Datex2MSTDataSource ds2 = new Datex2MSTDataSource();
        ds2.setId(getId(2));
        ds2.setDatakind("C2VBA-tlsin-LVEIntervalldatenKurzzeitInterpoliertOpt0");
        ds2.setValueCol("vPKW");
        ds2.setIdColumn("id");
        ds2.setInvalidValues("255");
        ds2.setD2BasicDataType("TrafficSpeed");
        ds2.setValidInterval(900);

        return List.of(ds1, ds2);
    }

    private static Datex2MST getMst(String mstId) {
        Datex2MST mst1 = new Datex2MST();
        Datex2MSTIdVersion id1 = new Datex2MSTIdVersion();
        id1.setMstId(mstId);
        id1.setVersion("1");
        mst1.setId(id1);
        return mst1;
    }

    private static Datex2MSTObjId getId(int id) {
        Datex2MSTObjId id1 = new Datex2MSTObjId();
        id1.setId(id);
        id1.setMstId("LVE");
        id1.setVersion("1");
        return id1;
    }

    private static final String JSON_DATA = "{\n" +
            "    \"data\": [{\n" +
            "      \"eventTime\": \"2022-09-30T12:36:00Z\",\n" +
            "      \"processingTime\": \"2022-09-30T12:37:40.002020Z\",\n" +
            "      \"id\": \"DET_B17_780_0520_AUG_H3_1_2\",\n" +
            "      \"qKFZ\": 10,\n" +
            "      \"qLKW\": 10,\n" +
            "      \"vPKW\": 10,\n" +
            "      \"vLKW\": 10,\n" +
            "      \"tNetto\": -99999.0,\n" +
            "      \"b\": -1,\n" +
            "      \"s\": -1,\n" +
            "      \"vKFZSmoothed\": -1,\n" +
            "      \"intervalLength\": 60,\n" +
            "      \"version\": \"VERSION_3\",\n" +
            "      \"vArithmetically\": true,\n" +
            "      \"noMeasuredData\": true,\n" +
            "      \"passivated\": false\n" +
            "    }, {\n" +
            "      \"eventTime\": \"2022-09-30T12:36:00Z\",\n" +
            "      \"processingTime\": \"2022-09-30T12:37:40.002020Z\",\n" +
            "      \"id\": \"DET_B17_780_0960_AUG_H1_1_81\",\n" +
            "      \"qKFZ\": -1,\n" +
            "      \"qLKW\": -1,\n" +
            "      \"vPKW\": 255,\n" +
            "      \"vLKW\": -1,\n" +
            "      \"tNetto\": -99999.0,\n" +
            "      \"b\": -1,\n" +
            "      \"s\": -1,\n" +
            "      \"vKFZSmoothed\": -1,\n" +
            "      \"intervalLength\": 60,\n" +
            "      \"version\": \"VERSION_3\",\n" +
            "      \"vArithmetically\": true,\n" +
            "      \"noMeasuredData\": true,\n" +
            "      \"passivated\": false\n" +
            "    }" +
            "]}";

}
