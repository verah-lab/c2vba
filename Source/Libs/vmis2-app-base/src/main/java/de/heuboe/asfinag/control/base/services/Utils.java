package de.heuboe.asfinag.control.base.services;

import de.heuboe.asfinag.control.base.config.CheckerHealthIndicator;
import eu.vmis_ehe.vmis2.paramservice.pojo.PIntRange;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;

/**
 * Utility class.
 */
@Slf4j
public class Utils {
    private static final String TXT_PARAM_NOT_FOUND_FOR_ID =
            "Parameter '{0}' for Infrastructure {1} was not found in the Kafka Paramdata '{2}'.";

    private Utils() {
    }

    /**
     * Creates a given kafka topic.
     *
     * @param kafkaAdminClient  kafka admin client
     * @param publicationTopic  topic name to create
     * @param compacted         true for cleanup policy "compact"
     * @param numPartitions     number of partitions
     * @param replicationFactor replication factor
     */
    public static void createTopicIfNeeded(AdminClient kafkaAdminClient, String publicationTopic, boolean compacted,
                                           int numPartitions, short replicationFactor) {
        ListTopicsOptions lto = new ListTopicsOptions();
        ListTopicsResult listTopicsResult = kafkaAdminClient.listTopics(lto);
        try {
            Collection<TopicListing> topicListings = listTopicsResult.listings().get();
            Optional<TopicListing> optionalTopicListing =
                    topicListings.stream().filter(tl -> tl.name().equals(publicationTopic)).findFirst();
            if (optionalTopicListing.isPresent()) {
                log.info("Topic: {} already created !", publicationTopic);
            } else {
                NewTopic nt = new NewTopic(publicationTopic, numPartitions, replicationFactor);
                if (compacted) {
                    log.info("Topic: {} is set to compacted", publicationTopic);
                    Map<String, String> configs = new HashMap<>();
                    configs.put(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT);
                    nt.configs(configs);
                }
                kafkaAdminClient.createTopics(Collections.singletonList(nt)).all().get();
                log.info("Topic: {} successfully created !", publicationTopic);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception while creating topic. What to do ?", e);
            // throw new RuntimeException(e); //ignore //find a better way in production code
            // For roads with multiple parts (e.g. S1) duplicate topics are created. We need a solution for that
            // problem.
            Thread.currentThread().interrupt();
        }
    }


    /**
     * to get the latest message we create a consumer, get the end offsets of the topics and seek to the previous
     *
     * @param consumerFactory consumer factory
     * @param callback        callback to seek to a certain position
     * @param partition       TopicPartition to work for
     */
    public static void seekToLatest(ConsumerFactory<String, byte[]> consumerFactory, ConsumerSeekCallback callback,
                                    TopicPartition partition) {
        try (Consumer<String, byte[]> consumer = consumerFactory.createConsumer(partition.toString())) {
            List<TopicPartition> topicPartitions = Collections.singletonList(partition);
            Map<TopicPartition, Long> topicPartitionLongMap = consumer.endOffsets(topicPartitions);
            Long pos = topicPartitionLongMap.get(partition);
            long position = pos == null || pos == 0 ? 0 : pos - 1;
            log.debug("Topic {} partition {} seeking to offset {}", partition.topic(), partition.partition(), position);
            callback.seek(partition.topic(), partition.partition(), position);
        }
    }

    /**
     * Strip possible quotes around strings
     *
     * @param value string with potential quotes
     * @return stirng without quotes
     */
    public static String stripQuotes(String value) {
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }


    /**
     * Creates a safe actor bean name.
     *
     * @param name name to convert
     * @return valid name
     */
    public static String createActorName(String name) {
        try {
            return URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            String failName = name.replace(" ", "+").replace("(", "**")
                    .replace(")", "**").replace("/", "*sl*")
                    .replace("ö", "oe").replace("ä", "ae")
                    .replace("ü", "ue").replace("Ö", "OE")
                    .replace("Ä", "AE").replace("Ü", "UE")
                    .replace("ß", "ss");
            log.warn("Failed encoding actor name {} - UnsupportedEncodingException: {}, use: {}", name, e.getMessage(), failName);
            return failName;
        }
    }

    /**
     * get Parameter Value is present or throw exception
     *
     * @param currentParamValues List of current parameters
     * @param paramName          name of parameter to get
     * @param laneID             id of infrastructure object
     * @param paramType          parameter that holds value
     * @return parameter value
     */
    public static List<Integer> getSuitingParamIntList(List<PParameterValue> currentParamValues, String paramName,
                                                       String laneID, String paramType) {
        for (PParameterValue param : currentParamValues) {
            if (param.getParameterId().equals(paramName)) {
                return param.getValue().getIntVals().getValuesList();
            }
        }
        String txt =
                TXT_PARAM_NOT_FOUND_FOR_ID.replace("{0}", paramName).replace("{1}", laneID).replace("{2}", paramType);
        throw new IllegalArgumentException(txt);
    }

    /**
     * get Parameter Value is present or throw exception
     *
     * @param currentParamValues List of current parameters
     * @param paramName          name of parameter to get
     * @param laneID             id of infrastructure object
     * @param paramType          parameter that holds value
     * @return parameter value
     */
    public static List<PIntRange> getSuitingParamIntRange(List<PParameterValue> currentParamValues, String paramName,
                                                          String laneID, String paramType) {
        for (PParameterValue param : currentParamValues) {
            if (param.getParameterId().equals(paramName)) {
                return param.getValue().getIntRangeVals().getValuesList();
            }
        }
        String txt =
                TXT_PARAM_NOT_FOUND_FOR_ID.replace("{0}", paramName).replace("{1}", laneID).replace("{2}", paramType);
        throw new IllegalArgumentException(txt);
    }


    /**
     * get Parameter Value is present or throw exception
     *
     * @param currentParamValues List of current parameters
     * @param paramName          name of parameter to get
     * @param laneID             id of infrastructure object
     * @param paramType          parameter that holds value
     * @return parameter value
     */
    public static int getSuitingParamInt(List<PParameterValue> currentParamValues, String paramName, String laneID,
                                         String paramType) {
        for (PParameterValue param : currentParamValues) {
            if (param.getParameterId().equals(paramName)) {
                return param.getValue().getIntVal();
            }
        }
        String txt =
                TXT_PARAM_NOT_FOUND_FOR_ID.replace("{0}", paramName).replace("{1}", laneID).replace("{2}", paramType);
        throw new IllegalArgumentException(txt);
    }

    /**
     * get Parameter Value is present or throw exception
     *
     * @param currentParamValues List of current parameters
     * @param paramName          name of parameter to get
     * @param laneID             id of infrastructure object
     * @param paramType          parameter that holds value
     * @return parameter value
     */
    public static float getSuitingParamFloat(List<PParameterValue> currentParamValues, String paramName, String laneID,
                                             String paramType) {
        for (PParameterValue param : currentParamValues) {
            if (param.getParameterId().equals(paramName)) {
                return param.getValue().getDoubleVal().floatValue();
            }
        }
        String txt =
                TXT_PARAM_NOT_FOUND_FOR_ID.replace("{0}", paramName).replace("{1}", laneID).replace("{2}", paramType);
        throw new IllegalArgumentException(txt);
    }


    /**
     * get Parameter Value is present or throw exception
     *
     * @param currentParamValues List of current parameters
     * @param paramName          name of parameter to get
     * @param laneID             id of infrastructure object
     * @param paramType          parameter that holds value
     * @return parameter value
     */
    public static boolean getSuitingParamBool(List<PParameterValue> currentParamValues, String paramName, String laneID,
                                              String paramType) {
        for (PParameterValue param : currentParamValues) {
            if (param.getParameterId().equals(paramName)) {
                return param.getValue().getBooleanVal();
            }
        }
        String txt =
                TXT_PARAM_NOT_FOUND_FOR_ID.replace("{0}", paramName).replace("{1}", laneID).replace("{2}", paramType);
        throw new IllegalArgumentException(txt);
    }

    /**
     * get Parameter Value is present or throw exception
     *
     * @param currentParamValues List of current parameters
     * @param paramName          name of parameter to get
     * @param laneID             id of infrastructure object
     * @param paramType          parameter that holds value
     * @return parameter value
     */
    public static List<String> getSuitingParamStringList(List<PParameterValue> currentParamValues, String paramName,
                                                         String laneID, String paramType) {
        for (PParameterValue param : currentParamValues) {
            if (param.getParameterId().equals(paramName)) {
                return param.getValue().getStringVals().getValuesList();
            }
        }
        String txt =
                TXT_PARAM_NOT_FOUND_FOR_ID.replace("{0}", paramName).replace("{1}", laneID).replace("{2}", paramType);
        throw new IllegalArgumentException(txt);
    }

    /**
     * blocks until healthIndicator status is "up"
     *
     * @param healthIndicator actor health indicator
     * @param timeOut         max time to wait in milliseconds
     * @throws TimeoutException thrown when time surpasses timeOut
     * @throws InterruptedException thrown when thread is interrupted
     */
    public static void waitForActors(CheckerHealthIndicator healthIndicator, int timeOut) throws TimeoutException, InterruptedException {
        while (healthIndicator.health().getStatus() != Status.UP) {
            Thread.sleep(10);
            timeOut -= 10;
            if (timeOut < 0) {
                throw new TimeoutException();
            }
        }
    }
}
