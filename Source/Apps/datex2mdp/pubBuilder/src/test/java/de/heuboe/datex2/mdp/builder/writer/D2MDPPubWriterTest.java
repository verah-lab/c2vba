package de.heuboe.datex2.mdp.builder.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.heuboe.datex2.config.persistence.Datex2MSTDataSource;
import de.heuboe.datex2.mdp.builder.Properties;
import de.heuboe.datex2.mdp.builder.SensorData;
import de.heuboe.datex2.mdp.builder.TestConfig;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.mdp.D2LogicalModel;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class D2MDPPubWriterTest {

    private String schemaFile = "schema/MeasuredData.xsd";
    private String namespace = "http://datex2.eu/schema/2/2_0";

    @Autowired
    D2MDPPubWriter writer;

//    @Test
    public void testParseMst() throws JAXBException, InterruptedException, IOException {//Mit invalid Werten testen
//        D2MDPPubWriter writer = new D2MDPPubWriter(TestConfig.storage("LVE"), TestConfig.getProps());
        Thread.sleep(10000); //NOSONAR
        Date now = Date.from(Instant.now());
        Option<D2LogicalModel> modelLve = writer.parseMst(now);
        assertTrue(modelLve.isDefined());
        JAXBUtil jaxbUtil = new JAXBUtil(schemaFile, D2LogicalModel.class.getPackageName(), namespace, true);
        assertDoesNotThrow(() -> System.out.println(jaxbUtil.getDocument(modelLve.get(), "d2LogicalModel", D2LogicalModel.class)));

        writer = new D2MDPPubWriter(TestConfig.storage("LVE"), TestConfig.getProps());
        now = Date.from(Instant.now());
        Option<D2LogicalModel> modelUde = writer.parseMst(now);
        assertTrue(modelUde.isDefined());
        assertDoesNotThrow(() -> System.out.println(jaxbUtil.getDocument(modelUde.get(), "d2LogicalModel", D2LogicalModel.class)));
    }

//    @Test
    void validateData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Datex2MSTDataSource dataSource = new Datex2MSTDataSource();
        dataSource.setValueCol("data");
        dataSource.setInvalidValues("255");
        dataSource.setIdColumn("id");
        dataSource.setValidInterval(900);
        Date now = Date.from(Instant.now());
        JsonNode node = mapper.readTree(createJsonData(now, "255"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "200"));
        assertEquals(Option.of("200"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        dataSource.setInvalidValues("-1");
        node = mapper.readTree(createJsonData(now, "-1"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));

        dataSource.setInvalidValues("(1,255)");
        node = mapper.readTree(createJsonData(now, "200"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "1"));
        assertEquals(Option.of("1"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "255"));
        assertEquals(Option.of("255"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "0"));
        assertEquals(Option.of("0"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));

        dataSource.setInvalidValues("[1,255]");
        node = mapper.readTree(createJsonData(now, "200"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "1"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "255"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "0"));
        assertEquals(Option.of("0"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));

        dataSource.setInvalidValues("[-I,0]");
        node = mapper.readTree(createJsonData(now, "-5"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "5"));
        assertEquals(Option.of("5"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));

        dataSource.setInvalidValues("[255,I]");
        node = mapper.readTree(createJsonData(now, "260"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "200"));
        assertEquals(Option.of("200"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));

        dataSource.setInvalidValues("[-I,0];(100, I)");
        node = mapper.readTree(createJsonData(now, "-2"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "150"));
        assertEquals(Option.none(), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
        node = mapper.readTree(createJsonData(now, "50"));
        assertEquals(Option.of("50"), D2MDPPubWriter.validateData(dataSource, newSensorData(node, dataSource)));
    }

    private SensorData newSensorData(JsonNode node, Datex2MSTDataSource dataSource) {
        String id = node.get(dataSource.getIdColumn()).asText();
        Instant prozessTime = Instant.parse(node.get("processingTime").asText());
        int interval = node.get("intervalLength").asInt();
        io.vavr.collection.Map<String, Integer> data = HashMap.of(dataSource.getValueCol(), node.get(dataSource.getValueCol()).asInt());
        return new SensorData(id, prozessTime, dataSource.getValidInterval(), interval, data, null);
    }

//    @Test
//    void checkAge() throws IOException {
//        Date now = Date.from(Instant.now());
//        Properties props = new Properties();
//        props.setMaxDataAge(100);
//        D2MDPPubWriter writer = new D2MDPPubWriter(props, null);
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonData = createJsonData(now);
//        List<JsonNode> dataNodes = new ArrayList<>();
//        mapper.readTree(jsonData).get("data").elements().forEachRemaining(d -> dataNodes.add(d));
//        JsonNode data = writer.getNewestData(now, dataNodes);
//        Instant resultTime = Instant.parse(data.get("processingTime").asText());
//        assertEquals(now.toInstant().plusSeconds(2), resultTime);
//    }

    private String createJsonData(Date now, String data) {
        return "{\n" +
                "   \"id\": \"ID_1\",\n" +
                "   \"data\": \"" + data + "\",\n" +
                "   \"processingTime\": \"" + now.toInstant().plusSeconds(2) + "\",\n" +
                "   \"processTime\": \"" + now.toInstant().plusSeconds(2) + "\"\n" +
                "    }";
    }

    private String createJsonData(Date now) {
        return "{\n" +
                "    \"data\": [{\n" +
                "      \"id\": \"ID_1\",\n" +
                "      \"processingTime\": \"" + now.toInstant().plusSeconds(2) + "\",\n" +
                "      \"processTime\": \"" + now.toInstant().plusSeconds(2) + "\"\n" +
                "    }, {\n" +
                "      \"id\": \"ID_2\",\n" +
                "      \"processingTime\": \"" + now.toInstant().minusSeconds(2) + "\",\n" +
                "      \"processTime\": \"" + now.toInstant().minusSeconds(2) + "\"\n" +
                "    }, {\n" +
                "      \"id\": \"ID_3\",\n" +
                "      \"processingTime\": \"" + now.toInstant().plusSeconds(4) + "\",\n" +
                "      \"processTime\": \"" + now.toInstant().plusSeconds(4) + "\"\n" +
                "    }" +
                "]}";
    }
}