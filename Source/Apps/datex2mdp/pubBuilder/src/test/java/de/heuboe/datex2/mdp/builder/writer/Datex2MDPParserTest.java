package de.heuboe.datex2.mdp.builder.writer;

import de.heuboe.datex2.config.persistence.Datex2MST;
import de.heuboe.datex2.config.persistence.Datex2MSTDataSource;
import de.heuboe.datex2.config.persistence.Datex2MSTIdVersion;
import de.heuboe.datex2.mdp.builder.SensorData;
import eu.datex2.schema._2._2_0.mdp.*;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Datex2MDPParserTest {

    @Test
    void getEquipmentFault() {
        SensorData.DeError error = new SensorData.DeError(1, Instant.now());
        MeasurementEquipmentFault equipmentFault = Datex2MDPParser.getEquipmentFault(error);
        assertEquals(MeasurementEquipmentFaultEnum.OTHER, equipmentFault.getMeasurementEquipmentFault());
        assertEquals("DE-Fehler", equipmentFault.getFaultDescription());
        assertEquals("1", equipmentFault.getFaultIdentifier());
    }

    @Test
    void createSiteReference() {
        SiteMeasurements reference = Datex2MDPParser.createSiteMeasurements("Loc_1", "1", Date.from(Instant.now()));
        assertEquals("MeasurementSiteRecord", reference.getMeasurementSiteReference().getTargetClass());
        assertEquals("Loc_1", reference.getMeasurementSiteReference().getId());
        assertEquals("1", reference.getMeasurementSiteReference().getVersion());
    }

    @Test
    void parseDataSource() {
        _SiteMeasurementsIndexMeasuredValue value = Datex2MDPParser.parseDataSource();
        assertEquals(0, value.getIndex());
        assertNotNull(value.getMeasuredValue());
        assertNotNull(value.getMeasuredValue().getMeasuredValueExtension());
        assertNotNull(value.getMeasuredValue().getMeasuredValueExtension().getMeasuredValues());
    }

    @Test
    void getD2LogicalModel() {
        D2LogicalModel model = Datex2MDPParser.getD2LogicalModel("2", CountryEnum.DE, "de");
        assertEquals("2", model.getModelBaseVersion());
        assertEquals(CountryEnum.DE, model.getExchange().getSupplierIdentification().getCountry());
        assertEquals("de", model.getExchange().getSupplierIdentification().getNationalIdentifier());
    }

    @Test
    void getMeasuredDataPublication() {
        Datex2MST mst = new Datex2MST();
        Datex2MSTIdVersion id = new Datex2MSTIdVersion();
        id.setVersion("1");
        id.setMstId("LVE");
        mst.setId(id);
        MeasuredDataPublication publication = Datex2MDPParser.getMeasuredDataPublication("de", mst, Date.from(Instant.now()), CountryEnum.DE, "natId");
        assertEquals("de", publication.getLang());
        assertEquals(CountryEnum.DE, publication.getPublicationCreator().getCountry());
        assertEquals("natId", publication.getPublicationCreator().getNationalIdentifier());
        assertEquals("LVE", publication.getMeasurementSiteTableReference().getId());
        assertEquals("1", publication.getMeasurementSiteTableReference().getVersion());
    }

    @Test
    void getVisibilityInformation() {
        VisibilityInformation data = Datex2MDPParser.getVisibilityInformation(Option.of("1"));
        assertEquals(1, data.getVisibility().getMinimumVisibilityDistance().getIntegerMetreDistance().intValue());
    }

    @Test
    void getPrecipitationInformation() {
        Datex2MSTDataSource source = new Datex2MSTDataSource();
        source.setMappingType("providedEnum");
        Datex2MSTDataSource.DataSourceEnumValue value = new Datex2MSTDataSource.DataSourceEnumValue();
        value.setName("unknown");
        value.setValue(0);
        Datex2MSTDataSource.DataSourceEnumValue value1 = new Datex2MSTDataSource.DataSourceEnumValue();
        value1.setName("rain");
        value1.setValue(1);
        Datex2MSTDataSource.DataSourceEnumValue value2 = new Datex2MSTDataSource.DataSourceEnumValue();
        value2.setName("solid");
        value2.setValue(2);
        source.setEnumValues(List.of(value, value1, value2));
        PrecipitationInformation info = Datex2MDPParser.getPrecipitationInformation(source, Option.of("1"));
        assertEquals("rain", info.getPrecipitationDetail().getPrecipitationDetailExtension().getPrecipitationDetailExtended().getPrecipitationType2().get(0).value());

        info = Datex2MDPParser.getPrecipitationInformation(source, Option.of("0"));
        assertEquals("unknown", info.getPrecipitationDetail().getPrecipitationDetailExtension().getPrecipitationDetailExtended().getPrecipitationType2().get(0).value());

        info = Datex2MDPParser.getPrecipitationInformation(source, Option.of("3"));
        assertEquals("unknown", info.getPrecipitationDetail().getPrecipitationDetailExtension().getPrecipitationDetailExtended().getPrecipitationType2().get(0).value());
    }
}