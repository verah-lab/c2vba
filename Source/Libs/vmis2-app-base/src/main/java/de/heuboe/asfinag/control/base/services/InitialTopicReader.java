package de.heuboe.asfinag.control.base.services;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Util methods for initialized reading of a compacted topic. Each key has to be send only the latest message.
 * Or for initializing normal topics where reading starts at a given timestamp supplied via constructor.
 * Attention!!! Not thread safe: Data structure should not be accessed simultaneously by different threads.
 *
 * @author cathleen
 *
 * @param <O> type of message content
 */
@Slf4j
public class InitialTopicReader<O> {
    /**
     * Message containing the key, a topic name and the message itself.
     */
    @AllArgsConstructor
    @Data
    public class Message {
        private String messageKey;
        private String topicName;
        private O protoObj;
        private Map<String, String> headerValues;
    }

    private boolean initialAssignment = true;
    private boolean initialReadFinished = false;
    private Map<TopicPartition, Long> beginningOffsets = new HashMap<>();
    private Map<TopicPartition, Long> topicsEndOffsets = new HashMap<>();
    private Set<TopicPartition> emptyPartitions = new HashSet<>();

    // Map<topic, Map<message key, Tuple<proto object, header values>>>
    Map<String, Map<String, Tuple2<O, Map<String, String>> >> startupCache = new HashMap<>();

    private final Set<TopicPartition> initialPartitionsRead = new HashSet<>();

    private final Set<String> inputTopics;

    private Long timestamp = null;
    private final ConsumerFactory<String, byte[]> consumerFactory;

    /**
     * Constructor for compacted topics. Reads every topic from the start.
     *
     * @param inputTopics     Input topics to be read.
     * @param consumerFactory consumer factory
     */
    public InitialTopicReader(Set<String> inputTopics, ConsumerFactory<String, byte[]> consumerFactory) {
        this.consumerFactory = consumerFactory;
        this.inputTopics = inputTopics;
    }

    /**
     * Constructor for normal topics. Reads every topic from a certain timestamp.
     *
     * @param inputTopics     Input topics to be read.
     * @param timestamp       timestamp (Instant) to read from
     * @param consumerFactory consumer factory
     */
    public InitialTopicReader(Set<String> inputTopics, Instant timestamp,
            ConsumerFactory<String, byte[]> consumerFactory) {
        this(inputTopics, consumerFactory);
        this.timestamp = timestamp.toEpochMilli();
    }

    /**
     * Constructor for compacted topics. Reads every topic from the start. Be aware that using a pattern is an
     * expensive operation because all topics have to be requested and then filtered according to the pattern.
     * If you know the topics in advance then use {@link #InitialTopicReader(Set, ConsumerFactory)}.
     *
     * @param topicPattern topic pattern to check
     * @param consumerFactory consumer factory
     */
    public InitialTopicReader(String topicPattern, ConsumerFactory<String, byte[]> consumerFactory) {
        this.consumerFactory = consumerFactory;
        this.inputTopics = getInputTopicsForPattern(topicPattern, consumerFactory);

        if(this.inputTopics.isEmpty()) {
            log.warn("There are no topics matching the topic pattern: {}. Check your pattern!", topicPattern);
        }
    }

    /**
     * Constructor for normal topics. Reads every topic from a certain timestamp. Be aware that using a pattern is an
     * expensive operation because all topics have to be requested and then filtered according to the pattern.
     * If you know the topics in advance then use {@link #InitialTopicReader(Set, Instant, ConsumerFactory)}.
     * 
     * @param topicPattern topic pattern to check
     * @param timestamp timestamp (Instant) to read from
     * @param consumerFactory consumer factory
     */
    public InitialTopicReader(String topicPattern, Instant timestamp, ConsumerFactory<String, byte[]> consumerFactory) {
        this(topicPattern, consumerFactory);
        this.timestamp = timestamp.toEpochMilli();
    }

    /**
     * Returns all input topics that matches the given topic pattern.
     *
     * @param topicPattern    Topic pattern used to match topic.
     * @param consumerFactory consumer factory
     * @return All input topics that matches the given topic pattern.
     */
    private Set<String> getInputTopicsForPattern(String topicPattern, ConsumerFactory<String, byte[]> consumerFactory) {
        Pattern pattern = Pattern.compile(topicPattern);

        try (org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer =
                consumerFactory.createConsumer(UUID.randomUUID().toString())) {

            return consumer.listTopics().keySet()
                    .stream()
                    .filter(topic -> pattern.matcher(topic).matches())
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private Marker getLogMarker(String topic) {
        return MarkerFactory.getMarker("Init-" + topic);
    }

    /**
     * Checks whether initialization of a topic is already done.
     *
     * @param pObject message content
     * @param topic topic name
     * @param partition partition
     * @param key message key
     * @param offset offset on partition
     * @param consumer consumer to inform about the last message for given key
     * @return true if last message of the key was already send to consumer
     */
    public boolean isInitialized(O pObject, String topic, int partition, String key, long offset,
            Consumer<Message> consumer) {
        return isInitialized(pObject, topic, partition, key, offset, consumer, true);
    }

    /**
     * Checks whether initialization of a topic is already done.
     *
     * @param pObject message content
     * @param topic topic name
     * @param partition partition
     * @param key message key
     * @param offset offset on partition
     * @param consumer consumer to inform about the last message for given key
     * @param systemExit flag to use function with system exit or not
     * @return true if last message of the key was already send to consumer
     */
    public boolean isInitialized(O pObject, String topic, int partition, String key, long offset, //NOSONAR: Is Ok
            Consumer<Message> consumer, boolean systemExit) {
        return isInitialized(pObject, topic, partition, key, offset, Collections.emptyMap(), consumer, systemExit);
    }

    /**
     * Checks whether initialization of a topic is already done. Use it if header values are necessary.
     *
     * @param pObject message content
     * @param topic topic name
     * @param partition partition
     * @param key message key
     * @param offset offset on partition
     * @param headerValues key value pairs of header values (optional)
     * @param consumer consumer to inform about the last message for given key
     * @param systemExit flag to use function with system exit or not
     * @return true if last message of the key was already send to consumer
     */
    public boolean isInitialized(O pObject, String topic, int partition, String key, long offset, //NOSONAR: Is Ok
            Map<String, String> headerValues, Consumer<Message> consumer, boolean systemExit) {
        if (initialReadFinished) {
            return true;
        }
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        Long endOffset = topicsEndOffsets.get(topicPartition);
        if (endOffset == null) {
            if (!emptyPartitions.contains(topicPartition)) {
                log.error(getLogMarker(topic), "Got data of unwanted partition: {}. Something went wrong -> exiting!",
                        topicPartition);

                //TODO: Diese Funktion scheint niemals jemand mit systemExit = false aufzurufen?
                //könnte also theoretischer weise angepasst werden
                if(systemExit) { //flag to avoid app crash with null payload messages
                    System.exit(-1);
                } else {
                    return false;
                }
            }
        } else {
            if(log.isTraceEnabled(getLogMarker(topic))) {
                log.trace(getLogMarker(topic), "Got data of {} offset {} endOffset {}", topicPartition, offset, topicsEndOffsets.get(topicPartition));
            }
            startupCache.computeIfAbsent(topic, t -> new HashMap<>()).put(key, Tuple.of(pObject, headerValues));

            if (offset >= endOffset - 1) {
                initialPartitionsRead.add(topicPartition);
                log.trace(getLogMarker(topic), "Initial read of partition: {} finished!", topicPartition);
                if (initialPartitionsRead.size() == topicsEndOffsets.size()) {
                    for (Map.Entry<String, Map<String, Tuple2<O, Map<String, String>>>> topicEntry : startupCache
                            .entrySet()) {
                        topicEntry.getValue().forEach((messageKey, tupleData) -> {
                            if(Objects.nonNull(tupleData._1())) {
                                consumer.accept(new Message(messageKey, topicEntry.getKey(), tupleData._1(), tupleData._2()));
                            }
                        });
                    }
                    initialReadFinished = true;
                    // clear startup cache
                    startupCache = new HashMap<>();
                    log.info(getLogMarker(topic), "Initial read complete!");
                    // do not return true because message was already send by consumer.accept
                }
            }
        }
        return false;
    }

    /**
     * Ask the InitialTopicReader whether the initial read is already done
     * @return true if initial read is already done.
     */
    public boolean isInitialReadFinished() {
        if(!initialReadFinished) {
            log.debug("Topics {} are not initial read!", inputTopics);
        }
        return initialReadFinished;
    }

    /**
     * When using group management, called when partition assignments change.
     * @param assignments the new assignments and their current offsets.
     * @param callback the callback to perform an initial seek after assignment.
     */
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) { //NOSONAR: is ok!
        List<Map.Entry<TopicPartition, Long>> matchingAssignments = assignments.entrySet()
                .stream()
                .filter(topicPartitionLongEntry -> inputTopics.contains(topicPartitionLongEntry.getKey().topic()))
                .toList();
        if (!matchingAssignments.isEmpty()) {
            log.debug("Called with following assignments:");
            matchingAssignments.forEach(e -> log.debug("  {} -> {}", e.getKey().toString(), e.getValue()));
            if (initialAssignment) {
                // we have the measure topics
                log.info("Initial call. Configure offsets for the topics: {}", inputTopics);
                String clientId = consumerFactory.getConfigurationProperties().get("client.id") == null ? "" : consumerFactory.getConfigurationProperties()
                                                                                                                              .get("client.id")
                                                                                                                              .toString();
                try (org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer =
                        consumerFactory.createConsumer(clientId + "_" + UUID.randomUUID() + "-init")) {
                    List<TopicPartition> topicPartitions = inputTopics.stream()
                            .map(consumer::partitionsFor)
                            .flatMap(Collection::stream)
                            .map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
                            .peek(topicPartition -> log.info("TopicPartition: {}", topicPartition)) //NOSONAR
                            .toList();

                    if (topicPartitions.isEmpty()) {
                        log.warn("No initial data to be read!");
                        initialReadFinished = true;
                        return;
                    }

                    initialAssignment = false;
                    if( this.timestamp != null ) { //read from a certain timestamp
                        Map<TopicPartition, Long> partitionTimes = topicPartitions.stream()
                                                                           .map(tp -> Tuple.of(tp, this.timestamp))
                                                                           .collect(Collectors
                                                                                   .toMap(t2 -> t2._1, t2 -> t2._2));
                        Map<TopicPartition, OffsetAndTimestamp> partitionOffsetAndTimestampMap = consumer
                                .offsetsForTimes(partitionTimes);

                        this.beginningOffsets = partitionOffsetAndTimestampMap.entrySet()
                                                                              .stream()
                                                                              .filter(e -> e.getValue() != null) //filter out partitions we have no offsets for.
                                                      .map(e -> Tuple.of(e.getKey(), e.getValue().offset()))
                                                      .collect(Collectors.toMap(t2 -> t2._1, t2-> t2._2));

                    } else { //read from beginning
                        this.beginningOffsets = consumer.beginningOffsets(topicPartitions);
                    }

                    // for all TopicPartitions
                    // save endOffset --> measureTopicsEndOffsets
                    topicsEndOffsets = consumer.endOffsets(topicPartitions);
                    // Save empty partitions to check if unexpected data arrives.
                    emptyPartitions = topicsEndOffsets.entrySet().stream().filter(
                        entry -> this.beginningOffsets.getOrDefault(entry.getKey(), entry.getValue()) >= entry.getValue())
                                                      .map(Map.Entry::getKey).collect(Collectors.toSet());
                    // Filter out empty topics !(beginningOffset < endOffset)
                    topicsEndOffsets = topicsEndOffsets.entrySet().stream().filter(
                        entry -> this.beginningOffsets.getOrDefault(entry.getKey(), entry.getValue()) < entry.getValue())
                                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    if (topicsEndOffsets.isEmpty()) { // no data yet. E.g. in tests
                        initialReadFinished = true;
                    }
                }
            }
            // seekToBeginning for all assignments we don't have done this yet
            matchingAssignments.stream()
                               //In case of a rebalance reset all partitions not yet read
                               .filter(a -> !initialPartitionsRead.contains(a.getKey()) && !emptyPartitions.contains(a.getKey()))
                               .forEach(a -> {
                                   if( this.timestamp == null ) {
                                       callback.seekToBeginning(a.getKey().topic(), a.getKey().partition());
                                       log.info("SeekToBeginning for TopicPartition: {}", a.getKey());
                                   } else {
                                       if( this.beginningOffsets.containsKey(a.getKey())) {
                                           long offset = this.beginningOffsets.get(a.getKey());
                                           callback.seek(a.getKey().topic(), a.getKey().partition(), offset);
                                           log.info("Seek to offset {} for TopicPartition: {}", offset, a.getKey());
                                       } else {
                                           log.error("Seek: Have no offset for TopicPartition: {}. Partition is assumed emtpy !", a.getKey());
                                           emptyPartitions.add(a.getKey());
                                       }
                                   }
                               });
        }
    }
}
