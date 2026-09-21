package de.heuboe.asfinag.control.base.services;

import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This is a synchronized version of the {@link InitialTopicReader}. It contains all public methods of the
 * {@link InitialTopicReader} but with the keyword 'synchronized'.
 *
 * @param <T> type of message content
 */
public class SynchronizedInitialTopicReader<T> {

    /**
     * Initial topic reader.
     */
    private final InitialTopicReader<T> initialTopicReader;

    /**
     * Constructor for compacted topics. Reads every topic from the start.
     *
     * @param topicPattern    topic pattern to check
     * @param consumerFactory consumer factory
     */
    public SynchronizedInitialTopicReader(String topicPattern, ConsumerFactory<String, byte[]> consumerFactory) {
        this.initialTopicReader = new InitialTopicReader<>(topicPattern, consumerFactory);
    }

    /**
     * Constructor for normal topics. Reads every topic from a certain timestamp.
     *
     * @param topicPattern    topic pattern to check
     * @param timestamp       timestamp (Instant) to read from
     * @param consumerFactory consumer factory
     */
    public SynchronizedInitialTopicReader(String topicPattern, Instant timestamp,
            ConsumerFactory<String, byte[]> consumerFactory) {
        this.initialTopicReader = new InitialTopicReader<>(topicPattern, timestamp, consumerFactory);
    }

    /**
     * Checks whether initialization of a topic is already done.
     *
     * @param pObject   message content
     * @param topic     topic name
     * @param partition partition
     * @param key       message key
     * @param offset    offset on partition
     * @param consumer  consumer to inform about the last message for given key
     * @return true if last message of the key was already send to consumer
     */
    public synchronized boolean isInitialized(T pObject, String topic, int partition, String key, long offset,
            Consumer<InitialTopicReader<T>.Message> consumer) {
        return initialTopicReader.isInitialized(pObject, topic, partition, key, offset, consumer);
    }

    /**
     * Checks whether initialization of a topic is already done.
     *
     * @param pObject    message content
     * @param topic      topic name
     * @param partition  partition
     * @param key        message key
     * @param offset     offset on partition
     * @param consumer   consumer to inform about the last message for given key
     * @param systemExit flag to use function with system exit or not
     * @return true if last message of the key was already send to consumer
     */
    public synchronized boolean isInitialized(T pObject, String topic, int partition, String key, long offset,
            Consumer<InitialTopicReader<T>.Message> consumer, boolean systemExit) {
        return initialTopicReader.isInitialized(pObject, topic, partition, key, offset, consumer, systemExit);
    }

    /**
     * Checks whether initialization of a topic is already done. Use it if header values are necessary.
     *
     * @param pObject      message content
     * @param topic        topic name
     * @param partition    partition
     * @param key          message key
     * @param offset       offset on partition
     * @param headerValues key value pairs of header values (optional)
     * @param consumer     consumer to inform about the last message for given key
     * @param systemExit   flag to use function with system exit or not
     * @return true if last message of the key was already send to consumer
     */
    public synchronized boolean isInitialized(T pObject, String topic, int partition, String key, long offset,
            Map<String, String> headerValues, Consumer<InitialTopicReader<T>.Message> consumer, boolean systemExit) {
        return initialTopicReader
                .isInitialized(pObject, topic, partition, key, offset, headerValues, consumer, systemExit);
    }

    /**
     * Ask the SynchronizedInitialTopicReader whether the initial read is already done
     *
     * @return true if initial read is already done.
     */
    public synchronized boolean isInitialReadFinished() {
        return initialTopicReader.isInitialReadFinished();
    }

    /**
     * When using group management, called when partition assignments change.
     *
     * @param assignments the new assignments and their current offsets.
     * @param callback    the callback to perform an initial seek after assignment.
     */
    public synchronized void onPartitionsAssigned(Map<TopicPartition, Long> assignments,
            ConsumerSeekAware.ConsumerSeekCallback callback) {
        initialTopicReader.onPartitionsAssigned(assignments, callback);
    }
}
