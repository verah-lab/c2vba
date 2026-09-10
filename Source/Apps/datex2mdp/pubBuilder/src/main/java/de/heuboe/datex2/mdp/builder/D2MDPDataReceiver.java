package de.heuboe.datex2.mdp.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.heuboe.datex2.config.persistence.Datex2MSTDataSource;
import de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils;
import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka receiver class
 */
@Component
@Slf4j
public class D2MDPDataReceiver implements MessageListener<String, byte[]>, ConsumerSeekAware {

    private String errorTopic;
    @Autowired
    private Storage storage;

    private final String processTimeName;
    private final String dataName;

    /**
     * Constructor
     *
     * @param errorTopic The name of the topic with the DeErrors
     * @param props      Properties
     */
    public D2MDPDataReceiver(@Value("${de.heuboe.datex2.mdp.deFehlerTopic}") String errorTopic, Properties props) {
        this.errorTopic = errorTopic;
        String mstId = props.getMstId();
        this.dataName = "LVE".equals(mstId) ? "dataList" : "elementsList";
        this.processTimeName = "LVE".equals(mstId) ? "processingTime" : "processTime";
    }

    @Override
    public synchronized  void onMessage(ConsumerRecord<String, byte[]> payload) {
        String topic = payload.topic();
        Map<String, Long> headers = new HashMap<>();
        payload.headers().forEach(header -> headers.put(KafkaHeaders.TIMESTAMP, payload.timestamp()));
        log.debug("received payload: " + topic + " -> " + payload.key());
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(getJson(payload, getClassPath(payload), mapper));
        } catch (IOException e) {
            log.error("failed to reade data: " + payload.key(), e);
            throw new IllegalArgumentException(e);
        }
        if (topic.equals(errorTopic)) {
            JsonNode errorNode = jsonNode.get("elementsList").get(0);
            SensorData.DeError deError = new SensorData.DeError(errorNode.get("fehlercode").asInt(),
                    Instant.parse(errorNode.get("processTime").asText()));
            storage.getData().compute(payload.key(), (k, v) -> (v == null)
                    ? newSensorData(payload.key(), deError)
                    : addDeError(v, deError));
        } else {
            jsonNode.get(dataName)  
                    .elements()
                    .forEachRemaining(i -> addNewData(i, storage.getDataSources()
                    .filter(d -> d.getDatakind().equals(topic))));                         //TODO handle Option NOSONAR
        }
    }

    private void addNewData(JsonNode i, List<Datex2MSTDataSource> dataSources) {
        SensorData sensorData = newSensorData(i, dataSources);
        storage.getData().computeIfPresent(sensorData.getId(), (k, v) -> addDeError(sensorData, v.getDeError()));
        storage.getData().put(sensorData.getId(), sensorData);
    }

    private SensorData addDeError(SensorData sensorData, SensorData.DeError deError) {
        if (deError != null) {
            Instant prozessTime = sensorData.getProzessTime();
            Instant errorTime = deError.getTime();
            if (errorTime.isAfter(prozessTime)) {
                sensorData.setDeError(deError);
            }
        }
        return sensorData;
    }

    private SensorData newSensorData(String id, SensorData.DeError deError) {
        return new SensorData(id, null,  -1, -1, null, deError);
    }

    private SensorData newSensorData(JsonNode node, List<Datex2MSTDataSource> dataSources) {
        String id = node.get(dataSources.get(0).getIdColumn()).asText();
        int interval = -1;
        if( node.has( "intervalLength" ) ) {
        	interval = node.get("intervalLength").asInt();
        }
        Instant prozessTime = Instant.parse(node.get(processTimeName).asText());
        io.vavr.collection.Map<String, Integer> data = dataSources.toMap(d -> d.getValueCol(), d -> node.get(d.getValueCol().toLowerCase()).asInt());
        return new SensorData(id, prozessTime, dataSources.get(0).getValidInterval(), interval, data, null);
    }

    private static String getClassPath(ConsumerRecord<String, byte[]> payload) {
        return new String(payload.headers().lastHeader("X-Protobuf-Type").value());
    }

    private String getJson(ConsumerRecord<String, byte[]> payload, String classPath, ObjectMapper mapper) throws IOException {
        String json;
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(classPath);
            Object o = createHbProtoBufPojo(payload.value(), ProtoPojoUtils.toJavaClass(clazz));
            json = mapper.writeValueAsString(o);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
        return json;
    }

    private static <T> T createHbProtoBufPojo(byte[] payload, Class<T> clazz) {
        try {
            return clazz.cast(clazz.getMethod("fromBytes", byte[].class).invoke(null, (Object) payload));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ClassCastException("Couldn't instantiate HbProtoBufPojo");
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.keySet().forEach(tp -> callback.seekToBeginning(tp.topic(), tp.partition()));
    }


}