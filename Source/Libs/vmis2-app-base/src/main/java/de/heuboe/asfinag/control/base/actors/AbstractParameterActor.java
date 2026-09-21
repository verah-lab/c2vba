package de.heuboe.asfinag.control.base.actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Read initial parameters and parameter changes. Check whether this instance needs them and
 * publish them on the internal eventStream
 *
 * @param <T> type of parameters to handle
 */
@Slf4j
@Component
@Scope("prototype")
public class AbstractParameterActor<T> extends AbstractActorWithTimers {
    private static final String TICK = "Tick";

    /**
     * Interface to
     *
     * @param <T> type of parameters to handle
     */
    public interface InstanceHandler<T> {
        /**
         * match a record of a parameter topic received via a Kafka to an instance of an algo
         *
         * @param r the kafka message
         * @return true if we handle that instance here
         */
        boolean matchesInstance(ConsumerRecord<String, byte[]> r);

        /**
         * map a kafka message to an internal Parameter data structure
         *
         * @param cr the kafka message
         * @return the Parameter
         */
        Optional<Parameters<T>> toParameters(ConsumerRecord<String, byte[]> cr);
    }

    private record RawParameters(List<ConsumerRecord<String, byte[]>> records) {
    }

    /**
     * Job to republish parameters on the event bus.
     *
     * @param roadId   Road id.
     * @param algo     Algorithm.
     * @param instance Instance.
     */
    public record PublishParameters(String roadId, String algo, String instance) {
    }

    /**
     * A list of all parameters of one instance of one algorithm for one road.
     *
     * @param key        Key.
     * @param roadId     Road id.
     * @param algo       Algorithm.
     * @param instance   Instance
     * @param system     System.
     * @param parameters Parameters.
     * @param <T>        type of parameters to handle
     */
    public record Parameters<T>(String key, String roadId, String algo, String instance, String system, T parameters) {
    }

    /**
     * Message of missing parameter of one instance of one algorithm for one road.
     *
     * @param roadId   Road id.
     * @param algo     Algorithm.
     * @param instance Instance
     */
    public record MissingParameter(String roadId, String algo, String instance) {
    }

    /**
     * Marker to signal, that all initial parameters are read.
     * Published once after initial Parameters are published to the akka, event stream
     *
     * @param name Request name.
     */
    public record InitialParametersRead(String name) {
    }

    /**
     * Request whether initialParameters are read or not.
     *
     * @param name Request name.
     */
    public record AskInitialParametersRead(String name) {
    }

    /**
     * Answers to AskInitialParametersRead.
     *
     * @param name                  Request name.
     * @param initialParametersRead Flag that indicates if th initial parameters have been read.
     */
    public record AnswerInitialParametersRead(String name, boolean initialParametersRead) {
    }

    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory; //NOSONAR: spring

    @Autowired
    private ApplicationContext applicationContext;

    private boolean waitForInitialParameters = true;

    private Map<TopicPartition, Long> topicPartitionEndOffsets;

    private Set<TopicPartition> successfulReadPartition = new HashSet<>();

    private Map<String, ConsumerRecord<String, byte[]>> rawParametersPerKey = new HashMap<>();

    //                     RoadId Or Key, algo, instance,  RoadId Or Key--> Key for Topic with no road id in Header but with 1 or n keys.
    private Map<Tuple3<String, String, String>, Map<String, Parameters<T>>> parametersPerKey = new HashMap<>();

    private InstanceHandler<T> instanceMatcher;
    private List<String> parameterTopics;
    private String name;
    private Marker marker;

    private ConcurrentMessageListenerContainer<String, byte[]> kafkaContainer;

    private int concurrency;

    /**
     * Constructor. Concurrency used to read topics in parallel is set to the number of topic partitions.
     *
     * @param instanceMatcher matcher if a parameter message matches the handled instance
     * @param parameterTopics a list of topics handled by this parameter actor. Must work with the same instance handler
     * @param name            used for groupId, clientId etc.
     */

    public AbstractParameterActor(
            InstanceHandler<T> instanceMatcher,
            List<String> parameterTopics,
            String name
    ) {
        this(instanceMatcher, parameterTopics, name, -1);
    }

    /**
     * Constructor.
     *
     * @param instanceMatcher matcher if a parameter message matches the handled instance
     * @param parameterTopics a list of topics handled by this parameter actor. Must work with the same instance handler
     * @param name            used for groupId, clientId etc.
     * @param concurrency     Concurrency used to read topics in parallel. The actual concurrency used is limited by the
     *                        number of topic partitions. If concurrency is less than 1 then the number of topic partitions
     *                        is used as concurrency.
     */
    public AbstractParameterActor(
            InstanceHandler<T> instanceMatcher,
            List<String> parameterTopics,
            String name,
            int concurrency
    ) {
        this.name = name;
        this.marker = new BasicMarkerFactory().getMarker(name);
        this.instanceMatcher = instanceMatcher;
        this.parameterTopics = parameterTopics;
        this.concurrency = concurrency;
    }

    @Override
    public void postStop() {
        log.info("Stop listener {} of actor {}", kafkaContainer.getContainerProperties().getClientId(), name);
        kafkaContainer.stop();
        log.info("Stop actor {}", name);
    }

    protected AbstractParameterActor() {
        //Hide empty Constructor
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.debug("Start actor {}", name);
        context().self().tell(TICK, context().self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(TICK, t -> checkForParameters())
                .match(RawParameters.class, this::handleRawParameters)
                .match(AskInitialParametersRead.class, this::answerInitialParametersRead)
                .match(PublishParameters.class, p -> publishParameters(p, getContext().getSender()))
                .matchAny(o -> log.error(marker, "received unknown message of type {}. Couldn't handle ... discarding!",
                        o.getClass().getName()))
                .build();
    }

    private void answerInitialParametersRead(AskInitialParametersRead question) {
        AnswerInitialParametersRead answerInitialParametersRead =
                new AnswerInitialParametersRead(question.name(), !waitForInitialParameters);
        log.debug("Actor: {} Answer: {}", this.name, answerInitialParametersRead);
        if (waitForInitialParameters) {
            if (topicPartitionEndOffsets == null) {
                log.debug("Actor: {} waiting for ALL parameter topics in categorie: {}", this.name, question.name());
            } else {
                log.debug("Actor: {} waiting for parameter topics: {}", this.name,
                        topicPartitionEndOffsets.keySet().stream()
                                .filter(element -> !successfulReadPartition.contains(element))
                                .map(TopicPartition::topic)
                                .toList());
            }
        }
        context().sender().tell(answerInitialParametersRead, self());
    }

    private void publishParameters(PublishParameters pp, ActorRef sender) {
        log.debug("publishParameters: Request to publish the parameters {} received!", pp);
        if (waitForInitialParameters) {
            log.debug("publish Parameters: {} received before parameters are initially read. Ignore!", pp);
        } else {
            Tuple3<String, String, String> t3 = Tuple.of(pp.roadId(), pp.algo(), pp.instance());
            if (parametersPerKey.containsKey(t3)) {
                if (sender == null) {
                    // sender not set -> publish on bus
                    parametersPerKey.get(t3).values().forEach(para -> context().system().eventStream().publish(para));
                } else {
                    // answer the sender
                    parametersPerKey.get(t3).values().forEach(para -> sender.tell(para, self()));
                }
            } else {
                log.debug(marker, "No Parameters for {} found!", t3);
                MissingParameter ep = new MissingParameter(pp.roadId(), pp.algo(), pp.instance());
                if (sender == null) {
                    // sender not set -> publish on bus
                    context().system().eventStream().publish(ep);
                } else {
                    // answer the sender
                    sender.tell(ep, self());
                }
            }
            context().system().eventStream().publish(new InitialParametersRead(name));
        }
    }

    private void checkForParameters() {
        startReadParameters();
    }

    private void startReadParameters() {
        String clientId = consumerFactory.getConfigurationProperties().get("client.id") == null ?
                "" :
                consumerFactory.getConfigurationProperties().get("client.id").toString();
        try (Consumer<String, byte[]> consumer = consumerFactory.createConsumer(
                clientId + "_" + name + "-startReader")) {

            List<TopicPartition> topicPartitions = parameterTopics.stream()
                    .map(parameterTopic -> getPartitionInfoFor(parameterTopic, consumer))
                    .flatMap(Collection::stream)
                    .map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
                    .toList();

            if (!topicPartitions.isEmpty()) {

                Map<TopicPartition, Long> topicPartitionBeginOffsets = consumer.beginningOffsets(topicPartitions);

                topicPartitionEndOffsets =
                        consumer.endOffsets(topicPartitions).entrySet().stream()  //remove emtpy topics
                                .filter(topicPartitionOffsetEntry -> topicPartitionBeginOffsets
                                        .getOrDefault(topicPartitionOffsetEntry.getKey(),
                                                topicPartitionOffsetEntry.getValue()) < topicPartitionOffsetEntry
                                        .getValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                ContainerProperties cp = new ContainerProperties(topicPartitions.stream()
                        .map(tp -> new TopicPartitionOffset(tp.topic(), tp.partition(),
                                TopicPartitionOffset.SeekPosition.BEGINNING)).toArray(TopicPartitionOffset[]::new));
                cp.setMessageListener((BatchMessageListener<String, byte[]>) this::newParameters);
                cp.setClientId(clientId + "_" + name + "-container");
                cp.setGroupId(consumerFactory.getConfigurationProperties().get("group.id").toString() + "_parameter");
                kafkaContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, cp);
                //Test: read all partitions parallel.
                kafkaContainer.setConcurrency(calculateConcurrency(topicPartitions.size()));
                kafkaContainer.setApplicationContext(applicationContext);
                kafkaContainer.start();
                waitForInitialParameters =
                        !topicPartitionEndOffsets.isEmpty(); //Don't wait for initial Parameters if all topics are emtpy
                if (waitForInitialParameters) {
                    log.info(marker, "Started reading of parameters of topics {} !", String.join(",", parameterTopics));
                } else {
                    log.warn(marker, "All parameter topics: {} are empty. Is that ok?",
                            String.join(",", parameterTopics));
                    context().system().eventStream().publish(new InitialParametersRead(name));
                }
            }
        }
    }

    /**
     * Returns all partition information for the given topic. The returned list is empty if the topic does not exist.
     * A log entry is made for a non-existing topic (warn).
     *
     * @param topic    Topic which partition information should be returned.
     * @param consumer Consumer used to request partition information.
     * @return Partition information for the given topic.
     */
    private List<PartitionInfo> getPartitionInfoFor(String topic, Consumer<String, byte[]> consumer) {
        List<PartitionInfo> partitionInfoList = consumer.partitionsFor(topic);

        if (partitionInfoList.isEmpty()) {
            log.warn("The parameter topic '{}' does not exist.", topic);
        }

        return partitionInfoList;
    }

    /**
     * Calculates the concurrency used to read topics in parallel. If the concurrency is less than 1 then
     * numberOfTopicPartitions is returned. Otherwise, the minimum of concurrency and numberOfTopicPartitions is
     * returned.
     *
     * @param numberOfTopicPartitions Number of topic partitions.
     * @return Concurrency used to read topics in parallel
     */
    private int calculateConcurrency(int numberOfTopicPartitions) {
        if (concurrency < 1) {
            return numberOfTopicPartitions;
        }
        return Math.min(concurrency, numberOfTopicPartitions);
    }

    private void newParameters(List<ConsumerRecord<String, byte[]>> records) {
        //Bring new parameters into the actors thread context.
        self().tell(new RawParameters(records), self());
    }

    private void handleRawParameters(RawParameters rp) {
        rp.records.forEach(this::handleRecord);
    }

    private void handleRecord(ConsumerRecord<String, byte[]> r) {
        log.trace(marker, "New ParameterMessage Topic {} Partition {} Key {} Offset {}", r.topic(), r.partition(),
                r.key(), r.offset());
        if (waitForInitialParameters) {
            if (instanceMatcher.matchesInstance(r)) {
                rawParametersPerKey.put(r.key(), r);
            }
            TopicPartition tp = new TopicPartition(r.topic(), r.partition());
            Long offset = topicPartitionEndOffsets.get(tp);
            log.debug(marker, "Handle Topic {} Partition {} offset {} endOffset {}", r.topic(), r.partition(),
                    r.offset(), offset);
            if (offset == null) {
                log.warn(marker, "Got offset of unwanted partition: {}/{}. Ignoring !", r.topic(), r.partition());
            } else {
                if (r.offset() == offset - 1) {
                    successfulReadPartition.add(new TopicPartition(r.topic(), r.partition()));
                    if (successfulReadPartition.size() == topicPartitionEndOffsets.size()) {
                        log.info(marker, "Initial parameters read !");
                        //got all data of all partitions
                        waitForInitialParameters = false;
                        handleInitialParameters();

                    }
                }
            }
        } else {
            if (instanceMatcher.matchesInstance(r)) {
                handleNewParameters(r);
            }
        }
    }

    private void handleNewParameters(ConsumerRecord<String, byte[]> r) {
        Optional<Parameters<T>> op = instanceMatcher.toParameters(r);
        if (op.isEmpty()) {
            log.warn(marker, "Couldn't convert parameters of key: {}. Ignoring!", r.key());
        } else {
            Parameters<T> parameters = op.get();
            Map<String, Parameters<T>> parametersPerKeyMap =
                    parametersPerKey.get(Tuple.of(parameters.roadId(), parameters.algo(), parameters.instance()));
            if (parametersPerKeyMap != null) {
                parametersPerKeyMap.put(r.key(), parameters);
            } else {
                parametersPerKeyMap = new HashMap<>();
                parametersPerKeyMap.put(r.key(), parameters);
            }

            parametersPerKey.put(Tuple.of(parameters.roadId(), parameters.algo(), parameters.instance()),
                    parametersPerKeyMap);

            log.debug(marker,
                    "Publishing new Parameter (system: {}, roadId: {}, algo: {}, instance: {} to the event stream!",
                    parameters.system(), parameters.roadId(), parameters.algo(), parameters.instance());
            context().system().eventStream().publish(parameters);
        }
    }

    private void handleInitialParameters() {
        rawParametersPerKey.forEach((k, v) -> handleNewParameters(v));
        rawParametersPerKey = null; //release intial data
        context().system().eventStream().publish(new InitialParametersRead(name));
    }

}
