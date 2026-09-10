package de.heuboe.datex2.mdp.builder.writer;

import de.heuboe.datex2.config.persistence.Datex2MST;
import de.heuboe.datex2.config.persistence.Datex2MSTDataSource;
import de.heuboe.datex2.mdp.builder.SensorData;
import eu.datex2.schema._2._2_0.mdp.*;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * abstract class to parse data into datex2 model
 */
@Slf4j
public abstract class Datex2MDPParser {

    private Datex2MDPParser() {
    }

    static MeasurementEquipmentFault getEquipmentFault(SensorData.DeError deError) {
        MeasurementEquipmentFault fault = new MeasurementEquipmentFault();
        fault.setMeasurementEquipmentFault(MeasurementEquipmentFaultEnum.OTHER);
        fault.setFaultDescription("DE-Fehler");
        fault.setFaultLastUpdateTime(Date.from(deError.getTime()));
        fault.setFaultIdentifier(deError.getErrorCode() + "");
        return fault;
    }

    static SiteMeasurements createSiteMeasurements(String locationId, String version, Date pubTime) {
        SiteMeasurements sm = new SiteMeasurements();
        sm.setMeasurementSiteReference(getRecordRef(locationId, version));
        sm.setMeasurementTimeDefault(pubTime);
        return sm;
    }


    static _SiteMeasurementsIndexMeasuredValue parseDataSource() {
        _SiteMeasurementsIndexMeasuredValue value = new _SiteMeasurementsIndexMeasuredValue();
        value.setMeasuredValue(getMeasuredValue());
        return value;
    }

    private static MeasuredValue getMeasuredValue() {
        MeasuredValue measuredValue = new MeasuredValue();
        measuredValue.setMeasuredValueExtension(getMeasuredValueExtension());
        return measuredValue;
    }

    private static _MeasuredValueExtensionType getMeasuredValueExtension() {
        _MeasuredValueExtensionType extension = new _MeasuredValueExtensionType();
        extension.setMeasuredValues(new MeasuredValues());
        return extension;
    }

    static D2LogicalModel getD2LogicalModel(String modleBaseVersion, CountryEnum country, String nationalId) {
        D2LogicalModel model = new D2LogicalModel();
        model.setModelBaseVersion(modleBaseVersion);
        Exchange exchange = new Exchange();
        exchange.setSupplierIdentification(getInternationalId(country, nationalId));
        model.setExchange(exchange);
        return model;
    }

    static MeasuredDataPublication getMeasuredDataPublication(String language, Datex2MST mst, Date pubTime, CountryEnum country, String nationalId) {
        final MeasuredDataPublication payload = new MeasuredDataPublication();
        payload.setLang(language);
        payload.setPublicationTime(pubTime);
        payload.setPublicationCreator(getInternationalId(country, nationalId));
        payload.setHeaderInformation(getHeaderInformation());
        payload.setMeasurementSiteTableReference(getMstRef(mst.getId().getVersion(), mst.getId().getMstId()));
        return payload;
    }

    static VisibilityInformation getVisibilityInformation(Option<String> data) {
        VisibilityInformation info = new VisibilityInformation();
        Visibility value = new Visibility();
        value.setMinimumVisibilityDistance(getIntegerMetreDistanceValue(data));
        info.setVisibility(value);
        return info;
    }

    private static IntegerMetreDistanceValue getIntegerMetreDistanceValue(Option<String> data) {
        IntegerMetreDistanceValue value = new IntegerMetreDistanceValue();
        String v = data.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setIntegerMetreDistance(new BigInteger(v));
        return value;
    }

    static PrecipitationInformation getPrecipitationInformation(Datex2MSTDataSource dataSource, Option<String> data) {
        PrecipitationInformation info = new PrecipitationInformation();
        PrecipitationDetail value = new PrecipitationDetail();
        if (dataSource.getMappingType().equals("providedEnum")) {
            _PrecipitationDetailExtensionType extensionType = new _PrecipitationDetailExtensionType();
            PrecipitationDetailExtended extended = new PrecipitationDetailExtended();
            extended.getPrecipitationType2().add(getPrecipitationIntensityEnum(dataSource.getEnumValues(), data));
            extensionType.setPrecipitationDetailExtended(extended);
            value.setPrecipitationDetailExtension(extensionType);
        }
        value.setPrecipitationIntensity(getPrecipitationIntensityValue(data));
        info.setPrecipitationDetail(value);
        return info;
    }

    private static PrecipitationType2Enum getPrecipitationIntensityEnum(List<Datex2MSTDataSource.DataSourceEnumValue> enumValues, Option<String> valueData) {
        int value = Integer.parseInt(valueData.getOrElse(() -> "-1"));
        Datex2MSTDataSource.DataSourceEnumValue enumValue = enumValues.stream().filter(x -> x.getValue() == value).findFirst().orElseGet(() -> enumValues.stream().filter(x -> x.getName().equals("unknown")).findFirst().get());
        return PrecipitationType2Enum.fromValue(enumValue.getName());
    }

    private static PrecipitationIntensityValue getPrecipitationIntensityValue(Option<String> data) {
        PrecipitationIntensityValue value = new PrecipitationIntensityValue();
        String v = data.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setMillimetresPerHourIntensity(Float.parseFloat(v));
        return value;
    }

    static TemperatureInformation getTemperatureInformation(Datex2MSTDataSource dataSource, Option<String> data) {
        TemperatureInformation info = new TemperatureInformation();
        Temperature value = new Temperature();
        switch (dataSource.getD2Value()) {
            case "airTemperature":
                value.setAirTemperature(getTemperatureValue(data));
                break;
            case "dewPointTemperature":
                value.setDewPointTemperature(getTemperatureValue(data));
                break;
            case "maximumTemperature":
                value.setMaximumTemperature(getTemperatureValue(data));
                break;
            case "minimumTemperature":
                value.setMinimumTemperature(getTemperatureValue(data));
                break;
            default:
                log.warn("Unknown temperature value: " + dataSource.getD2Value());
        }
        info.setTemperature(value);
        return info;
    }

    static WindInformation getWindInformation(Datex2MSTDataSource dataSource, Option<String> data) {
        WindInformation info = new WindInformation();
        Wind value = new Wind();
        switch (dataSource.getD2Value()) {
            case "maximumWindSpeed":
                value.setMaximumWindSpeed(getSpeedValue(data));
                break;
            case "windSpeed":
                value.setWindSpeed(getSpeedValue(data));
                break;
            case "windDirectionBearing":
                value.setWindDirectionBearing(getWindDirectionBearing(data));
                break;
            default:
                log.warn("Unknown wind value: " + dataSource.getD2Value());
        }
        info.setWind(value);
        return info;
    }

    private static SpeedValue getSpeedValue(Option<String> data) {
        SpeedValue value = new SpeedValue();
        String v = data.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setSpeed(Float.parseFloat(v));
        return value;
    }

    private static DirectionBearingValue getWindDirectionBearing(Option<String> data) {
        DirectionBearingValue value = new DirectionBearingValue();
        String v = data.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setDirectionBearing(new BigInteger(v));
        return value;
    }

    static HumidityInformation getHumidityInformation(Option<String> data) {
        HumidityInformation info = new HumidityInformation();
        Humidity humidity = new Humidity();
        humidity.setRelativeHumidity(getPercentageValue(data));
        info.setHumidity(humidity);
        return info;
    }

    static RoadSurfaceConditionInformation getRoadSurfaceConditionInformation(Datex2MSTDataSource dataSource, Option<String> data) {
        RoadSurfaceConditionInformation info = new RoadSurfaceConditionInformation();
        RoadSurfaceConditionMeasurements value = new RoadSurfaceConditionMeasurements();
        info.setRoadSurfaceConditionMeasurements(value);
        if (dataSource.getMappingType().equals("providedEnum")) {
            RoadSurfaceConditionInformationExtended extension = new RoadSurfaceConditionInformationExtended();
            extension.setWeatherRelatedRoadConditionType2(getWeatherRelatedRoadConditionType(dataSource.getEnumValues(), data));
            _RoadSurfaceConditionInformationExtensionType x = new _RoadSurfaceConditionInformationExtensionType();
            x.setRoadSurfaceConditionInformationExtended(extension);
            info.setRoadSurfaceConditionInformationExtension(x);
            return info;
        } else {
            switch (dataSource.getD2Value()) {
                case "roadSurfaceTemperature":
                    value.setRoadSurfaceTemperature(getTemperatureValue(data));
                    break;
                case "protectionTemperature":
                    value.setProtectionTemperature(getTemperatureValue(data));
                    break;
                case "deIcingApplicationRate":
                    value.setDeIcingApplicationRate(getApplicationRateValue(data));
                    break;
                case "deIcingConcentration":
                    value.setDeIcingConcentration(getKilogramsConcentrationValue(data));
                    break;
                case "depthOfSnow":
                    value.setDepthOfSnow(getFloatingPointMetreDistanceValue(data));
                    break;
                case "waterFilmThickness":
                    value.setWaterFilmThickness(getFloatingPointMetreDistanceValue(data));
                    break;
                default:
                    log.warn("Unknown road surface value: " + dataSource.getD2Value());
            }
        }
        return info;
    }

    private static WeatherRelatedRoadConditionType2Enum getWeatherRelatedRoadConditionType(List<Datex2MSTDataSource.DataSourceEnumValue> enumValues, Option<String> valueData) {
        int value = Integer.parseInt(valueData.getOrElse(() -> "65"));
        Datex2MSTDataSource.DataSourceEnumValue enumValue = enumValues.stream().filter(x -> x.getValue() == value).findFirst().orElseGet(() -> enumValues.stream().filter(x -> x.getName().equals("unknown")).findFirst().get());
        return WeatherRelatedRoadConditionType2Enum.fromValue(enumValue.getName());
    }

    private static ApplicationRateValue getApplicationRateValue(Option<String> valueData) {
        ApplicationRateValue value = new ApplicationRateValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setApplicationRate(Float.parseFloat(v));
        return value;
    }

    private static FloatingPointMetreDistanceValue getFloatingPointMetreDistanceValue(Option<String> valueData) {
        FloatingPointMetreDistanceValue value = new FloatingPointMetreDistanceValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setFloatingPointMetreDistance(Float.parseFloat(v));
        return value;
    }

    private static PercentageValue getPercentageValue(Option<String> valueData) {
        PercentageValue value = new PercentageValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setPercentage(Float.parseFloat(v));
        return value;
    }


    private static TemperatureValue getTemperatureValue(Option<String> valueData) {
        TemperatureValue value = new TemperatureValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setTemperature(Float.parseFloat(v));
        return value;
    }

    private static KilogramsConcentrationValue getKilogramsConcentrationValue(Option<String> valueData) {
        KilogramsConcentrationValue value = new KilogramsConcentrationValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setKilogramsConcentration(Float.parseFloat(v));
        return value;
    }

    static TrafficConcentration getTrafficConcentration(Option<String> valueData) {
        TrafficConcentration conc = new TrafficConcentration();
        PercentageValue value = new PercentageValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setPercentage(Float.parseFloat(v));
        conc.setOccupancy(value);
        return conc;
    }

    static TrafficSpeed getTrafficSpeed(Option<String> valueData) {
        TrafficSpeed speedData = new TrafficSpeed();
        SpeedValue value = new SpeedValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setSpeed(Float.parseFloat(v));
        speedData.setAverageVehicleSpeed(value);
        return speedData;
    }

    static TrafficFlow getTrafficFlow(Option<String> valueData, int interval) {
        TrafficFlow data = new TrafficFlow();
        VehicleFlowValue value = new VehicleFlowValue();
        String v = valueData.getOrElse(() -> {
            value.setDataError(true);
            return "0";
        });
        value.setVehicleFlowRate(parseTrafficData(new BigInteger(v), interval));
        data.setVehicleFlow(value);
        return data;
    }

    private static BigInteger parseTrafficData(BigInteger v, int interval) {
        if (interval == -1) {
            return v;
        }
        int intervalPerH = 3600 / interval;
        return v.multiply(new BigInteger(intervalPerH + ""));
    }

    static _MeasurementSiteRecordVersionedReference getRecordRef(String itemId, String version) {
        _MeasurementSiteRecordVersionedReference ref = new _MeasurementSiteRecordVersionedReference();
        ref.setTargetClass("MeasurementSiteRecord");
        ref.setId(itemId);
        ref.setVersion(version);
        return ref;
    }

    static _MeasurementSiteTableVersionedReference getMstRef(String version, String mstId) {
        _MeasurementSiteTableVersionedReference mstRef = new _MeasurementSiteTableVersionedReference();
        mstRef.setTargetClass("MeasurementSiteTable");
        mstRef.setId(mstId);
        mstRef.setVersion(version);
        return mstRef;
    }

    static HeaderInformation getHeaderInformation() {
        HeaderInformation headerInfo = new HeaderInformation();
        headerInfo.setConfidentiality(ConfidentialityValueEnum.NO_RESTRICTION);
        headerInfo.setInformationStatus(InformationStatusEnum.REAL);
        return headerInfo;
    }

    static InternationalIdentifier getInternationalId(CountryEnum country, String nationalId) {
        InternationalIdentifier internationalId = new InternationalIdentifier();
        internationalId.setCountry(country);
        internationalId.setNationalIdentifier(nationalId);
        return internationalId;
    }

}
