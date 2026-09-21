package de.heuboe.asfinag.control.base.actors;

import de.heuboe.asfinag.control.base.services.Utils;
import de.heuboe.asfinag.vmis2.constants.KafkaConstants;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Read initial parameters and parameter changes. Check whether this instance needs them and
 * publish them on the internal eventStream
 */
@Slf4j
@Component
@Scope("prototype")
public class RawParameterActor extends AbstractParameterActor<ConsumerRecord<String, byte[]>> {

    /**
     * InstanceHandler to check for topic names
     */
    public static class SystemDefaultInstanceHandler implements InstanceHandler<ConsumerRecord<String, byte[]>> {

        private List<String> topicNames;
        private Set<Tuple2<String, String>> handledAlgoInstances;


        public static final String KAFKA_PARAMETER_TYPE = "X-Protobuf-Type";

        /**
         * constructor
         * @param topicNames names of topics to handle
         * @param handledAlgoInstances instances to handel
         */
        public SystemDefaultInstanceHandler(List<String> topicNames, Set<Tuple2<String, String>> handledAlgoInstances) {
            this.topicNames = topicNames;
            this.handledAlgoInstances = handledAlgoInstances;
        }

        @Override
        public boolean matchesInstance(ConsumerRecord<String, byte[]> r) {
            Optional<Map<String, byte[]>> oHeaders = getHeaders(r);
            if (oHeaders.isEmpty()) {
                log.warn("Parameter-Record with key: {} doesn't have the necessary headers.", r.key());
                return false;
            }

            String topic = r.topic();
            if (!this.topicNames.contains(topic)) {
                return false;
            }

            return handledAlgoInstances.contains(Tuple.of("", topic));
        }

        private Optional<Map<String, byte[]>> getHeaders(ConsumerRecord<String, byte[]> cr) {
            Map<String, byte[]> headers = StreamSupport.stream(cr.headers().spliterator(), false)
                                                       .filter(this::isQualifyingHeader).collect(Collectors.toMap(Header::key, Header::value));

            if (headers.containsKey(KAFKA_PARAMETER_TYPE)) {
                return Optional.of(headers);
            } else {
                return Optional.empty();
            }

        }

        private boolean isQualifyingHeader(Header h) {
            return h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_ROAD_ID)
                || h.key().equalsIgnoreCase(KAFKA_PARAMETER_TYPE)
                || h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID)
                || h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_INSTANCE)
                || h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_SYSTEM);
        }

        @Override
        public Optional<Parameters<ConsumerRecord<String, byte[]>>> toParameters(ConsumerRecord<String, byte[]> cr) {
            Optional<Map<String, byte[]>> oHeaders = getHeaders(cr);
            if (oHeaders.isPresent()) {
                Map<String, byte[]> headers = oHeaders.get();
                String roadId = "";
                String parameterAlgo =
                    Utils.stripQuotes(new String(headers.get(KAFKA_PARAMETER_TYPE), StandardCharsets.UTF_8));
                String algoInstance = "";
                String sys = "";

                return Optional.of(new Parameters<>(cr.key(), roadId, parameterAlgo, algoInstance, sys, cr));
            }

            return Optional.empty();

        }
    }

    /**
     * Default instance handler for common parameter types
     */
    public static class DefaultInstanceHandler implements InstanceHandler<ConsumerRecord<String, byte[]>> {

        private String system;
        private Pattern roadPattern;
        private Set<Tuple2<String, String>> handledAlgoInstances;

        /**
         * Constructor
         *
         * @param system               system to work for
         * @param roadPattern          roads to work for
         * @param handledAlgoInstances algos to work for
         */
        public DefaultInstanceHandler(
            String system,
            String roadPattern,
            Set<Tuple2<String, String>> handledAlgoInstances
        ) {
            this.system = system;
            this.roadPattern = Pattern.compile(roadPattern);
            this.handledAlgoInstances = handledAlgoInstances;
        }

        @Override
        public boolean matchesInstance(ConsumerRecord<String, byte[]> r) {
            Optional<Map<String, byte[]>> oHeaders = getHeaders(r);
            if (oHeaders.isEmpty()) {
                log.warn("Parameter-Record with key: {} doesn't have the necessary headers.", r.key());
                return false;
            }

            Map<String, byte[]> headers = oHeaders.get();

            String sys = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_SYSTEM), StandardCharsets.UTF_8));

            if (!this.system.equals(sys)) {
                return false;
            }

            String roadId = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_ROAD_ID), StandardCharsets.UTF_8));
            boolean handleRoad = roadPattern.matcher(roadId).matches();
            if (log.isTraceEnabled()) {
                if (!handleRoad) {
                    log.trace("Road {} is NOT handled by this application", roadId);
                } else {
                    log.trace("Road {} is handled by this application", roadId);
                }
            }
            if (!handleRoad) {
                return handleRoad;
            }

            String algoName = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID), StandardCharsets.UTF_8));
            String instanceName = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_INSTANCE), StandardCharsets.UTF_8));

            return handledAlgoInstances.contains(Tuple.of(algoName, instanceName));

        }

        @Override
        public Optional<Parameters<ConsumerRecord<String, byte[]>>> toParameters(ConsumerRecord<String, byte[]> cr) {
            //Assume no duplicate header entries !
            Optional<Map<String, byte[]>> oHeaders = getHeaders(cr);

            if (oHeaders.isPresent()) {
                Map<String, byte[]> headers = oHeaders.get();
                String roadId = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_ROAD_ID), StandardCharsets.UTF_8));
                String parameterAlgo = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID), StandardCharsets.UTF_8));
                String algoInstance = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_INSTANCE), StandardCharsets.UTF_8));
                String sys = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_SYSTEM), StandardCharsets.UTF_8));
                return Optional.of(new Parameters<>(cr.key(), roadId, parameterAlgo, algoInstance, sys, cr));
            }

            return Optional.empty();
        }


        private boolean isQualifyingHeader(Header h) {
            return h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_ROAD_ID) ||
                h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID) ||
                h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_INSTANCE) ||
                h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_SYSTEM);
        }

        private Optional<Map<String, byte[]>> getHeaders(ConsumerRecord<String, byte[]> cr) {
            Map<String, byte[]> headers = StreamSupport.stream(cr.headers().spliterator(), false)
                                                       .filter(this::isQualifyingHeader)
                                                       .collect(Collectors.toMap(Header::key, Header::value));

            if (headers.containsKey(KafkaConstants.KAFKA_HEADER_ROAD_ID) &&
                headers.containsKey(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID) &&
                headers.containsKey(KafkaConstants.KAFKA_HEADER_INSTANCE) &&
                headers.containsKey(KafkaConstants.KAFKA_HEADER_SYSTEM)
            ) {
                return Optional.of(headers);
            } else {
                return Optional.empty();
            }

        }

    }


    /**
     * Constructor
     *
     * @param instanceMatcher matcher if a parameter message matches the handled instance
     * @param parameterTopics a list of topics handled by this parameter actor. Must work with the same instance handler
     * @param name used for groupId, clientId etc.
     */
    public RawParameterActor(
        InstanceHandler<ConsumerRecord<String, byte[]>> instanceMatcher,
        List<String> parameterTopics,
        String name
    ) {
        super(instanceMatcher, parameterTopics, name);
    }

    protected RawParameterActor() {
        //Hide empty Constructor
    }
}
