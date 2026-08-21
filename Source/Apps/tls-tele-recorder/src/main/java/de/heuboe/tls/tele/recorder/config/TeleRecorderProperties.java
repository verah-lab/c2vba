package de.heuboe.tls.tele.recorder.config;

import de.heuboe.tls.tele.recorder.model.TeleRecorderCleanLogs;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class will collect all properties defined in the application.properties. So only this class must be added via
 * "@Autowired" annotation to have access to all properties.
 */
@Configuration
@ConfigurationProperties("tls.tele.recorder")
//@Validated
@Getter
@Setter
public class TeleRecorderProperties {

    /* Interval time for creating new logs in seconds. Default 3600. */
    @Value("${tls.tele.recorder.interval.static:3600}")
    private int staticInterval;

    @Value("${tls.tele.recorder.interval.cron:}")
    private String cronInterval;

    /* Topic for receiving telegrams. */
    private String receiveTopic;

    /* Defines the number of retries a received message will be resend from KafkaOperatorService via Akka if
       something fails. Default 0. */
    private int responseRetries = 0;

    /* Defines the response time in milliseconds the service must notify the KafkaOperatorService about successful
       message handling before resend the message if configured in responseRetries property. Default 5000. */
    private long responseTimeout = 5000;

    /* Topic for sending telegrams. */
    private String sendTopic;

    /* File path for saving log files. */
    private String absolutLogPath;

    /* Configuration for log cleaning. */
    private TeleRecorderCleanLogs cleanLogs;

    /* Flag that enables or disables the HTTP server. Default false. */
    @Value("${tls.tele.recorder.server.http.enabled:false}")
    private boolean httpServerEnabled;

    /* Flag that enables or disables the TPC server. Default true. */
    @Value("${tls.tele.recorder.server.tcp.enabled:true}")
    private boolean tcpServerEnabled;

    /* Definition of the TCP port for streaming telegrams. */
    @Value("${tls.tele.recorder.server.tcp.port:-1}")
    private int tcpPort;

    /* Definition of the TCP port for streaming legacy telegrams. */
    @Value("${tls.tele.recorder.server.tcp.legacyPort:-1}")
    private int legacyTcpPort;

    /* This property change the real address of a telegram to the given value before sending it via TCP. The value must
       be greater than 0. This will be used for legacy telegrams only! The recorded telegrams will not be affected from
       this change. Default 0. */
    @Value("${tls.tele.recorder.server.tcp.updateRealAddress:0}")
    private int updateRealAddress;

    /**
     * Flag to activate the timestamp manipulation for telegrams. This manipulation is only necessary for a legacy
     * transfer of telegrams from the new E21X TPF to the old core system. Due to major changes in the GPRS UZ with
     * regard to a new timestamp behavior this manipulation is necessary to support the legacy system. This will be used
     * for legacy telegrams only! Default false.
     */
    @Value("${tls.tele.recorder.server.tcp.manipulateTimestamps.activated:false}")
    private boolean manipulateTimestampsActivated;

    /**
     * Determine the timezone for extracting the timestamp from telegrams. Default Europe/Berlin.
     */
    @Value("${tls.tele.recorder.server.tcp.manipulateTimestamps.timezone:Europe/Berlin}")
    private String manipulateTimestampsTimezone;

    /* Flag that enable a GZIP compression for completed log files. The log files will be deleted after compression.
       Compressed files will also be deleted due to log cleaning! Default false. */
    @Value("${tls.tele.recorder.compressLogs:false}")
    private boolean compressLogs;

    /* Determines the number of max recursive calls of the saveToFile method due to filestream access errors. */
    @Value("${tls.tele.recorder.maxSaveRetries:5}")
    private int maxSaveRetries;
}
