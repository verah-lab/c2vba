package de.heuboe.datex2.mdp.builder.writer;

import de.heuboe.datex2.config.persistence.Datex2MST;
import de.heuboe.datex2.config.persistence.Datex2MSTDataSource;
import de.heuboe.datex2.config.persistence.Datex2MSTItem;
import de.heuboe.datex2.config.persistence.Datex2MSTLocation;
import de.heuboe.datex2.mdp.builder.Properties;
import de.heuboe.datex2.mdp.builder.SensorData;
import de.heuboe.datex2.mdp.builder.Storage;
import eu.datex2.schema._2._2_0.mdp.*;
import eu.datex2.schema._2._2_0.mdp.BasicData;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Pattern;

import static de.heuboe.datex2.mdp.builder.writer.Datex2MDPParser.*;

/**
 * Uses the {@link Storage} to write the data into a {@link D2LogicalModel}
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Component
public class D2MDPPubWriter {

    private static final String VALIDATE_REGEX = "([\\[\\(]{1}-?(I|[0-9]*),-?(I|[0-9]*)[\\]\\)]{1});?";

    @Autowired
    private Storage storage;

    @Autowired
    private Properties props;

    /**
     * parses the data into a D2LogicalModel
     *
     * @param pubTime time of the publication
     * @return an D2LogicalModel if any data is present.
     */
    synchronized public Option<D2LogicalModel> parseMst(Date pubTime) {
        Datex2MST mst = storage.getMst();
        D2LogicalModel model = getD2LogicalModel(props.getSchemaModelBaseVersion(), props.getCountry(), props.getNationalId());
        final MeasuredDataPublication publication = getMeasuredDataPublication(props.getLanguage(), mst, pubTime, props.getCountry(), props.getNationalId());
        boolean hasData = false;
        for (Datex2MSTLocation location : storage.getLocations()) {
            final String locationId = location.getD2Id();
            final String version = location.getId().getVersion();
            List<Datex2MSTItem> items = storage.getItems().get(location.getId().getId()).get();
            SiteMeasurements siteMeasurements = createSiteMeasurements(locationId, version, pubTime);
            publication.getSiteMeasurements().add(siteMeasurements);
            java.util.Set<Datex2MSTDataSource> dataSources = storage.getDataSourceMap().get(locationId);
            for (Datex2MSTDataSource dataSource : dataSources) {
                List<Datex2MSTItem> filter = items.filter(i -> i.getDkRef() == dataSource.getId().getId());
                for (Datex2MSTItem item : filter) {
                    _SiteMeasurementsIndexMeasuredValue measuredValue = parseDataSource();
                    siteMeasurements.getMeasuredValue().add(measuredValue);
                    measuredValue.setIndex(item.getIndex());
                    SensorData data = storage.getData().get(item.getDkKey());
                    if (data == null) {
                        log.warn("Missing data for: {}", item.getDkKey());
                        continue;
                    }
                    hasData = true;
                    if (data.getDeError() != null) {
                        SensorData.DeError deError = data.getDeError();
                        if (data.getProzessTime() == null || deError.getTime().isAfter(data.getProzessTime())) {
                            measuredValue.getMeasuredValue().getMeasurementEquipmentFault().add(getEquipmentFault(deError));
                            continue;
                        }
                    }
                    addValue(measuredValue, getBasicData(data, dataSource));
                }
            }
        }
        model.setPayloadPublication(publication);
        return hasData ? Option.of(model) : Option.none();
    }

    private void addValue(_SiteMeasurementsIndexMeasuredValue measuredValue, BasicData data) {
        measuredValue.getMeasuredValue().getMeasuredValueExtension().getMeasuredValues().getBasicData().add(data);
    }

    static Option<String> validateData(Datex2MSTDataSource dataSource, SensorData data) {
        String invalidValues = dataSource.getInvalidValues();
        int value = data.getData().get(dataSource.getValueCol()).get();
        Pattern pattern = Pattern.compile(VALIDATE_REGEX);
        if (pattern.matcher(invalidValues).find()) {
            String[] invalidAreas = invalidValues.split(";");
            for (String invalidArea : invalidAreas) {
                String[] limits = invalidArea.split(",");
                int min, max;
                min = getMin(limits[0]);
                max = getMax(limits[1]);
                if (min < value && value < max) {
                    return Option.none();
                }
            }
            return Option.of(value + "");
        } else if (invalidValues.matches("-?[0-9]*")) {
            int iv = Integer.parseInt(invalidValues);
            return value != iv ? Option.of(value + "") : Option.none();
        }//TODO handle other cases
        return null;
    }

    private static int getMax(String limit) {
        if (limit.contains("I")) {
            return Integer.MAX_VALUE;
        }
        return getLimit(limit, ")", "]", 1);
    }

    private static int getMin(String limit) {
        if (limit.contains("I")) {
            return Integer.MIN_VALUE;
        }
        return getLimit(limit, "(", "[", -1);
    }

    private static int getLimit(String limit, String s, String s1, int add) {
        if (limit.contains(s)) {
            return Integer.parseInt(limit.replace(s, ""));
        } else if (limit.contains(s1)) {
            return Integer.parseInt(limit.replace(s1, "")) + add;
        }//TODO prüfen auf andere Werte
        return 0;
    }

    private BasicData getBasicData(SensorData sensorData, Datex2MSTDataSource dataSource) {
        Option<String> data = validateData(dataSource, sensorData);
        BasicData baseData;
        switch (dataSource.getD2BasicDataType()) {
            case "TrafficFlow":
                baseData = getTrafficFlow(data, sensorData.getInterval());
                break;
            case "TrafficSpeed":
                baseData = getTrafficSpeed(data);
                break;
            case "TrafficConcentration":
                baseData = getTrafficConcentration(data);
                break;
            case "RoadSurfaceConditionInformation":
                baseData = getRoadSurfaceConditionInformation(dataSource, data);
                break;
            case "HumidityInformation":
                baseData = getHumidityInformation( data );
                break;
            case "WindInformation":
                baseData = getWindInformation(dataSource, data);
                break;
            case "TemperatureInformation":
                baseData = getTemperatureInformation(dataSource, data);
                break;
            case "PrecipitationInformation":
                baseData = getPrecipitationInformation(dataSource, data);
                break;
            case "VisibilityInformation":
                baseData = getVisibilityInformation(data);
                break;
            default:
                //TODO handel defaut
                return null;
        }
        baseData.setMeasurementOrCalculationTime(Date.from(sensorData.getProzessTime()));
        return baseData;
    }
}
