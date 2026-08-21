package de.heuboe.tls.sequencer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * This class will collect all properties defined in the application.properties. So only this class must be added via
 * "@Autowired" annotation to have access to all properties.
 */
@Configuration
@ConfigurationProperties("de.heuboe.tls.sequencer")
@Validated
@Getter
public class SequencerProperties {

    /* The id of the UZ for this application. Will also be used to search for UZ specific scripts under the script path
       configured above. */
    @Value("${de.heuboe.tls.sequencer.uzid}")
    private String uzid;

    /* The prefix of topics in receive direction. This mainly means all topics that will normally be filled by the
       tls-receiver. It will be used for sending messages. */
    @Value("${de.heuboe.tls.sequencer.receive.topic.prefix}")
    private String receiveTopicPrefix;

    /* The suffix of topics in receive direction. This mainly means all topics that will normally be filled by the
       tls-receiver. It will be used for sending messages. */
    @Value("${de.heuboe.tls.sequencer.receive.topic.suffix}")
    private String receiveTopicSuffix;

    /* The prefix of topics in send direction. This mainly means all topics that will normally be processed by the
       tls-sender for iface telegram generation. It will be used for sending messages. */
    @Value("${de.heuboe.tls.sequencer.send.topic.prefix}")
    private String sendTopicPrefix;

    /* The suffix of topics in send direction. This mainly means all topics that will normally be processed by the
       tls-sender for iface telegram generation. It will be used for sending messages. */
    @Value("${de.heuboe.tls.sequencer.send.topic.suffix}")
    private String sendTopicSuffix;

    /* The key in the Kafka header that will be used to identify the protobuf type of the message. */
    @Value("${de.heuboe.tls.sequencer.header.type}")
    private String headerType;

    /* The content of the header with the sequencerMarker that will be used to differ between sequencer messages from
       different sequencer instances. */
    @Value("${de.heuboe.tls.sequencer.header.sequencerContent:modifiedBySequencer}")
    private String headerSequencerContent;

    /* The key in the Kafka header that will be used to identify a sequencer message. */
    @Value("${de.heuboe.tls.sequencer.header.sequencerMarker}")
    private String headerSequencerMarker;

    /* The absolute path defines where the sequencer scripts can be found. All scripts in this path will be loaded by
       the sequencer. */
    @Value("${de.heuboe.tls.sequencer.script.path:/config}")
    private String scriptPath;

    /* A comma separated list of script files that will be loaded in every sequencer instance. */
    @Value("#{'${de.heuboe.tls.sequencer.script.globalScripts}'.split(',')}")
    private List<String> globalScripts;

    /* The stage and system name influence the scripts that were loaded at the start of the sequencer. The pattern for
       script naming is 'seq-uz-<systemName>-<stageName>*.txt'. If both are empty the script retrieve logic will be
       ignored. Default empty string. */
    @Value("${de.heuboe.tls.sequencer.script.stageName:}")
    private String stageName;

    /* The stage and system name influence the scripts that were loaded at the start of the sequencer. The pattern for
       script naming is 'seq-uz-<systemName>-<stageName>*.txt'. If both are empty the script retrieve logic will be
       ignored. Default empty string. */
    @Value("${de.heuboe.tls.sequencer.script.systemName:}")
    private String systemName;

    /* The absolute path defines where test scripts will be loaded from. This enables the possibility to side load
       scripts that will disable all deployed scripts without changing the deployment. Default empty string. */
    @Value("${de.heuboe.tls.sequencer.script.testModePath:}")
    private String testModePath;

    /* Enables a debug mode that avoid sending sequencer messages to Kafka. If true the messages that will normally be
       sent will be printed to the log. Default false. */
    @Value("${de.heuboe.tls.sequencer.noSendMode:false}")
    private boolean noSendMode;

    /* The number of retries the KafkaOperatorService will resend messages via Akka if the sequencer does not answer in
       the configured response retry timeout. Default 0. */
    @Value("${de.heuboe.tls.sequencer.responseRetries:0}")
    private int responseRetries;

    /* The response timeout in milliseconds the KafkaOperatorService waits until resend the current message if retries
       are configured. Default 5000. */
    @Value("${de.heuboe.tls.sequencer.responseTimeout:5000}")
    private long responseTimeout;
}
