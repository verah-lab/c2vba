package de.heuboe.asfinag.control.base.actors;

import com.google.protobuf.InvalidProtocolBufferException;
import de.heuboe.asfinag.control.base.services.Utils;
import de.heuboe.asfinag.vmis2.constants.KafkaConstants;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.helpers.BasicMarkerFactory;
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
public class ParameterActor extends AbstractParameterActor<PParameterSetList> {
    private static final String UNMARSHALL_ERROR = "Could not unmarshall parameters for key {}";
    private static final String PARAMETER_HEADER_WARNING = "Parameter-Record with key: {} doesn't have the necessary headers.";
    /**
     * Default instance handler for common parameter types
     */
    public static class DefaultInstanceHandler implements InstanceHandler<PParameterSetList> {
          
        private Pattern systemPattern;
        private Pattern roadPattern;
        private Set<Tuple2<String, String>> handledAlgoInstances;

        /**
         * Constructor
         *
         * @param systemPattern               systemPattern to work for
         * @param roadPattern          roads to work for
         * @param handledAlgoInstances algos to work for or empty to work for all algos
         */
        public DefaultInstanceHandler(
            String systemPattern,
            String roadPattern,
            Set<Tuple2<String, String>> handledAlgoInstances
        ) {
            this.systemPattern = Pattern.compile(systemPattern);
            this.roadPattern = Pattern.compile(roadPattern);
            this.handledAlgoInstances = handledAlgoInstances;
        }

        @Override
        public boolean matchesInstance(ConsumerRecord<String, byte[]> r) {
            Optional<Map<String, byte[]>> oHeaders = getHeaders(r);
            if (oHeaders.isEmpty()) {
                log.warn(PARAMETER_HEADER_WARNING, r.key());
                return false;
            }

            Map<String, byte[]> headers = oHeaders.get();

            String sys = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_SYSTEM), StandardCharsets.UTF_8));

            if (!systemPattern.matcher(sys).matches()) {
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
                return false;
            }

            if (handledAlgoInstances==null || handledAlgoInstances.isEmpty()) {
                return true;
            }

            String algoName = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID), StandardCharsets.UTF_8));
            String instanceName = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_INSTANCE), StandardCharsets.UTF_8));

            return handledAlgoInstances.contains(Tuple.of(algoName, instanceName));

        }

        @Override
        public Optional<Parameters<PParameterSetList>> toParameters(ConsumerRecord<String, byte[]> cr) {
            //Assume no duplicate header entries !
            Optional<Map<String, byte[]>> oHeaders = getHeaders(cr);

            if (oHeaders.isPresent()) {
                Map<String, byte[]> headers = oHeaders.get();
                String roadId = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_ROAD_ID), StandardCharsets.UTF_8));
                String parameterAlgo = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID), StandardCharsets.UTF_8));
                String algoInstance = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_INSTANCE), StandardCharsets.UTF_8));
                String sys = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_SYSTEM), StandardCharsets.UTF_8));

                try {
                    PParameterSetList pParameterSetList = PParameterSetList.fromBytes(cr.value());
                    return Optional.of(new Parameters<>(cr.key(), roadId, parameterAlgo, algoInstance, sys, pParameterSetList));
                } catch (InvalidProtocolBufferException e) {
                    log.error(UNMARSHALL_ERROR, cr.key());
                }
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
     * Default instance handler for common parameter types
     */
    public static class UZDefaultInstanceHandler implements InstanceHandler<PParameterSetList> {

        private Pattern systemPattern;
        private Set<Tuple2<String, String>> handledAlgoInstances;

        /**
         * Constructor
         *
         * @param system               systemPattern to work for
         * @param handledAlgoInstances algos to work for or empty to work for all
         */
        public UZDefaultInstanceHandler(
            String system,
            Set<Tuple2<String, String>> handledAlgoInstances
        ) {
            this.systemPattern = Pattern.compile(system);
            this.handledAlgoInstances = handledAlgoInstances;
        }

        @Override
        public boolean matchesInstance(ConsumerRecord<String, byte[]> r) {
            if (handledAlgoInstances==null || handledAlgoInstances.isEmpty()) {
                // handle all instances
                return true;
            }

            Optional<Map<String, byte[]>> oHeaders = getHeaders(r);
            if (oHeaders.isEmpty()) {
                log.warn(PARAMETER_HEADER_WARNING, r.key());
                return false;
            }

            Map<String, byte[]> headers = oHeaders.get();

            String sys = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_SYSTEM), StandardCharsets.UTF_8));

            if (!systemPattern.matcher(sys).matches()) {
                return false;
            }

            String algoName = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID), StandardCharsets.UTF_8));
            String instanceName = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_INSTANCE), StandardCharsets.UTF_8));

            return handledAlgoInstances.contains(Tuple.of(algoName, instanceName));

        }

        @Override
        public Optional<Parameters<PParameterSetList>> toParameters(ConsumerRecord<String, byte[]> cr) {
            //Assume no duplicate header entries !
            Optional<Map<String, byte[]>> oHeaders = getHeaders(cr);

            if (oHeaders.isPresent()) {
                Map<String, byte[]> headers = oHeaders.get();
                String parameterAlgo = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID), StandardCharsets.UTF_8));
                String algoInstance = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_INSTANCE), StandardCharsets.UTF_8));
                String sys = Utils.stripQuotes(new String(headers.get(KafkaConstants.KAFKA_HEADER_SYSTEM), StandardCharsets.UTF_8));

                try {
                    PParameterSetList pParameterSetList = PParameterSetList.fromBytes(cr.value());
                    return Optional.of(new Parameters<>(cr.key(), "", parameterAlgo, algoInstance, sys, pParameterSetList));
                } catch (InvalidProtocolBufferException e) {
                    log.error(UNMARSHALL_ERROR, cr.key());
                }
            }

            return Optional.empty();
        }


        private boolean isQualifyingHeader(Header h) {
            return h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID) ||
                h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_INSTANCE) ||
                h.key().equalsIgnoreCase(KafkaConstants.KAFKA_HEADER_SYSTEM);
        }

        private Optional<Map<String, byte[]>> getHeaders(ConsumerRecord<String, byte[]> cr) {
            Map<String, byte[]> headers = StreamSupport.stream(cr.headers().spliterator(), false)
                                                       .filter(this::isQualifyingHeader)
                                                       .collect(Collectors.toMap(Header::key, Header::value));

            if (headers.containsKey(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID) &&
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
     * Constructor. Concurrency used to read topics in parallel is set to the number of topic partitions.
     *
     * @param instanceMatcher matcher if a parameter message matches the handled instance
     * @param parameterTopics a list of topics handled by this parameter actor. Must work with the same instance handler
     * @param name used for groupId, clientId etc.
     */
    public ParameterActor(
        InstanceHandler<PParameterSetList> instanceMatcher,
        List<String> parameterTopics,
        String name
    ) {
        super(instanceMatcher, parameterTopics, name);
    }

    /**
     * Constructor.
     *
     * @param instanceMatcher matcher if a parameter message matches the handled instance
     * @param parameterTopics a list of topics handled by this parameter actor. Must work with the same instance handler
     * @param name            used for groupId, clientId etc.
     * @param concurrency Concurrency used to read topics in parallel. The actual concurrency used is limited by the
     *                    number of topic partitions. If concurrency is less than 1 then the number of topic partitions
     *                    is used as concurrency.
     */
    public ParameterActor(
            InstanceHandler<PParameterSetList> instanceMatcher,
            List<String> parameterTopics,
            String name,
            int concurrency
    ) {
        super(instanceMatcher, parameterTopics, name, concurrency);
    }

    protected ParameterActor() {
        //Hide empty Constructor
    }
}
