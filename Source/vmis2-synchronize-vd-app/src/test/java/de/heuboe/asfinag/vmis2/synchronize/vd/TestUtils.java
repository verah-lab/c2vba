package de.heuboe.asfinag.vmis2.synchronize.vd;

import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

public class TestUtils {

    public enum UpdateType {
        UPDATE, DEFAULT
    }

    public static UpdateType updateType = UpdateType.DEFAULT;

    private TestUtils() {
        //do not instantiate
    }
    public  static int getExpectedPartitions(MessageListenerContainer messageListenerContainer, EmbeddedKafkaBroker embeddedKafkaBroker) {
        if( messageListenerContainer.getContainerProperties().getTopics() != null ) {
            return messageListenerContainer.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic();
        } else if ( messageListenerContainer.getContainerProperties().getTopicPattern() != null ) {
            Pattern topicPattern = messageListenerContainer.getContainerProperties().getTopicPattern();
            int numTopics = (int)embeddedKafkaBroker.getTopics().stream().filter(s -> topicPattern.matcher(s).matches()).count();
            return numTopics * embeddedKafkaBroker.getPartitionsPerTopic();
        } else {
            return embeddedKafkaBroker.getPartitionsPerTopic();
        }
    }
    

    /**
     * Calculate timestamp of last full hour.
     * 
     * @param clock     current time.
     * @return          timestamp of last full hour.
     */
    public static Instant calcNextFullMinute(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MINUTES).plus(1, ChronoUnit.MINUTES);
    }
}
