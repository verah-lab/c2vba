package de.heuboe.tls.tele.recorder.services;

import akka.actor.AbstractActor;
import de.heuboe.tls.kafka.operator.messages.KafkaOperatorMessage;
import de.heuboe.tls.tele.recorder.config.TeleRecorderProperties;
import de.heuboe.tls.tele.recorder.job.TeleRecorderJob;
import de.heuboe.tls.tele.recorder.model.TeleRecorderCleanLogsStrategy;
import de.heuboe.tls.tele.recorder.server.TeleRecorderHttpServer;
import de.heuboe.tls.tele.recorder.server.TeleRecorderTcpServer;
import de.heuboe.tls.tele.recorder.utils.TeleRecorderUtils;
import de.heuboe.tls.tlstele.TlsTele;
import io.vavr.Tuple2;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletOutputStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the {@link TeleRecorderService} that holds the main logic for handling messages.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Getter
@Slf4j
public class TeleRecorderService extends AbstractActor implements JobListener {

    @Autowired
    private TeleRecorderUtils utils;

    @Autowired
    private TeleRecorderProperties properties;

    @Autowired
    private TeleRecorderUtils teleRecorderUtils;

    private Tuple2<String, OutputStream> fileStream = new Tuple2<>(null, null);
    private TeleRecorderTcpServer tcpServer;
    private TeleRecorderTcpServer legacyTcpServer;
    private TeleRecorderHttpServer httpServer;
    private Scheduler scheduler;
    private final JobKey jobKey = new JobKey("logJob");
    private final TriggerKey triggerKey = new TriggerKey("logTrigger");

    public TeleRecorderService() {
        log.info("TeleRecorder service constructed");
    }

    /**
     * Checks if the utils class was created correctly.
     */
    @PostConstruct
    public void initCheck() {
        if (utils == null) {
            log.error("Bean '{}' seems not to be created correctly. Make sure the interface is implemented and the " +
                    "bean is correctly configured!", TeleRecorderUtils.class.getSimpleName());
            System.exit(-1);
        }

        // check log directory and create it if necessary
        utils.checkDirectory(properties.getAbsolutLogPath());

        if (!properties.getCronInterval().isEmpty()) {
            log.info("Logging interval: {}", properties.getCronInterval());
        } else {
            log.info("Logging interval: {} seconds", properties.getStaticInterval());
        }

        if (properties.getCleanLogs().getStrategy() == TeleRecorderCleanLogsStrategy.NONE) {
            log.warn("No log cleaning strategy activated! This could lead to problems if not enough disk space is " +
                    "available.");
        } else if (properties.getCleanLogs().getStrategy() == TeleRecorderCleanLogsStrategy.NUMBER) {
            log.info("Maximum number of log files: {}", properties.getCleanLogs().getNumber());
        } else {
            log.info("Maximum size of log files: {} MB", properties.getCleanLogs().getSize());
        }

        if (properties.isCompressLogs()) {
            log.info("Log compression is enabled.");
        }

        // start TCP server if configured
        if (properties.isTcpServerEnabled()) {
            if (properties.getTcpPort() >= 1024 && properties.getTcpPort() <= 65353) {
                tcpServer = new TeleRecorderTcpServer(properties.getTcpPort(), false);
                tcpServer.start();
            } else if (properties.getTcpPort() >= 0 && properties.getTcpPort() < 1024 || properties.getTcpPort() > 65353) {
                log.error("Please use a TCP port between 1024 and 65353 to enable the TCP server!");
            }

            if (properties.getLegacyTcpPort() >= 1024 && properties.getLegacyTcpPort() <= 65353) {
                if (properties.getUpdateRealAddress() > 0) {
                    log.info("Real address of legacy telegrams will be updated to '{}'",
                            properties.getUpdateRealAddress());
                }

                if (properties.isManipulateTimestampsActivated()) {
                    log.info("Timestamp manipulation for legacy telegrams activated! Using timezone '{}'",
                            properties.getManipulateTimestampsTimezone());
                }

                legacyTcpServer = new TeleRecorderTcpServer(properties.getLegacyTcpPort(), true);
                legacyTcpServer.start();
            } else if (properties.getLegacyTcpPort() >= 0 && properties.getLegacyTcpPort() < 1024 || properties.getLegacyTcpPort() > 65353) {
                log.error("Please use a legacy TCP port between 1024 and 65353 to enable the TCP server!");
            }
        }

        // start HTTP server if configured
        if (properties.isHttpServerEnabled()) {
            httpServer = new TeleRecorderHttpServer();
        }

        try {
            // create the scheduler that will enable the file creation / switch
            scheduler = StdSchedulerFactory.getDefaultScheduler();

            // add this class as listener for job events to retrieve the filestream for the new file from the job
            scheduler.getListenerManager().addJobListener(this);

            // start the scheduler
            scheduler.start();

            // create the job data that will be used inside the job
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("properties", properties);
            jobData.put("utils", utils);
            jobData.put("filestream", fileStream);

            // build the job with its key, the job class and the created job data
            JobDetail job = JobBuilder.newJob(TeleRecorderJob.class)
                    .withIdentity(jobKey)
                    .setJobData(new JobDataMap(jobData))
                    .build();

            // build trigger basics
            TriggerBuilder<Trigger> tb = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(job);

            // set scheduler for trigger depending on configuration
            if (properties.getCronInterval().isEmpty()) {
                tb.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(properties.getStaticInterval()));
            } else {
                tb.withSchedule(CronScheduleBuilder.cronSchedule(properties.getCronInterval()));
            }

            // build the trigger
            Trigger trigger = tb.build();

            // add the job and trigger to the scheduler
            scheduler.scheduleJob(job, trigger);

            // trigger the job the first time manually to create the first log file if cron timer is configured
            if (!properties.getCronInterval().isEmpty()) {
                scheduler.triggerJob(jobKey);
            }

        } catch (SchedulerException | RuntimeException e) {
            log.error("Could not start scheduling with error message '{}'", e.getMessage());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaOperatorMessage.class, msg -> {
                    if (handleMessage(msg)) {
                        sender().tell("Message received and processed.", self());
                        log.debug("Sender informed about successful message processing.");
                    } else {
                        log.warn("Message processing failed. Sender will not be informed.");
                    }
                })
                .matchAny(msg -> log.error("Unknown message received: {}", msg))
                .build();
    }

    /**
     * Handles the message that was received from kafka via actor. It initial create a {@link TlsTele} object and tries
     * to write the content into a file. Further it will start a cleaning of old log files if the property
     * 'cleanTelegrams' is set.
     *
     * @param msg The {@link KafkaOperatorMessage} that holds all necessary content.
     * @return true if message handling was successful, else false.
     */
    private boolean handleMessage(KafkaOperatorMessage msg) {
        // create a telegram from the message content
        TlsTele telegram = utils.createTelegram(msg.messageValue(), msg.topic());

        // abort message handling if the telegram itself is null
        if (telegram == null) {
            return true;
        }

        // delete old log files if necessary
        utils.cleanTelegrams();

        // write telegram to log file
        saveToFile(telegram, 0, "");

        streamTelegram(telegram);

        return true;
    }

    /**
     * Writes the {@link TlsTele} into a file in the appropriate property 'tls.tele.recorder.absolutTelegramPath' in a
     * binary file format.
     *
     * @param telegram     The {@link TlsTele} that should be written into a file.
     * @param run          A counter to avoid endless recursive calls in case of "Stream Closed" errors while switching
     *                     files.
     * @param errorMessage The error message that causes a recursive call.
     */
    private void saveToFile(TlsTele telegram, int run, String errorMessage) {

        if (run > properties.getMaxSaveRetries()) {
            log.error("Writing telegram due to stream access errors failed {} times with message '{}'. Skip saving " +
                            "telegram '{}'\r\nTo avoid this error you can increase the service property 'maxSaveRetries'.",
                    properties.getMaxSaveRetries(), errorMessage, telegram);
            return;
        }

        log.trace("Writing telegram {}", telegram);

        try {
            if (fileStream == null || fileStream._2() == null) {
                throw new IOException("Filestream is null!");
            }

            // write telegram to file stream
            telegram.saveToRec(fileStream._2());
            log.debug("Telegram in {} direction from logical address '{}' saved.",
                    telegram.getDirection(), telegram.getLogAddress());
            log.trace("Telegram successfully written to '{}'", fileStream._1());
            log.trace("{}", telegram);
        } catch (IOException e) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            saveToFile(telegram, ++run, e.getMessage());
        }
    }

    /**
     * Writes the {@link TlsTele} into the every {@link ServletOutputStream} that exists in the stream map.
     *
     * @param telegram The {@link TlsTele} that should be written into the stream.
     */
    private void streamTelegram(TlsTele telegram) {
        if (properties.isTcpServerEnabled()) {
            // check if real address change is requested
            if (properties.getUpdateRealAddress() > 0) {
                // Hint: this option is mainly used for the legacy logtls core system of e21x to handle telegrams from
                // the new GPRS UZ.
                telegram = utils.updateRealAddress(telegram, properties.getUpdateRealAddress());
            }

            if (tcpServer != null) {
                streamToTcp(telegram);
            }
            if (legacyTcpServer != null) {
                // manipulate the timestamps if requested
                if (properties.isManipulateTimestampsActivated()) {
                    utils.manipulateTimestamp(telegram);
                }

                streamToLegacyTcp(telegram);
            }
        }

        if (properties.isHttpServerEnabled()) {
            streamToHttp(telegram);
        }
    }

    /**
     * Stream the telegram to all clients that are connected to the TCP server.
     *
     * @param telegram The {@link TlsTele} that should be streamed.
     */
    private void streamToTcp(TlsTele telegram) {
        for (Map.Entry<Integer, OutputStream> entry : tcpServer.getTcpStreams().entrySet()) {
            OutputStream stream = entry.getValue();
            try {
                if (stream != null) {
                    // write content of the telegram
                    telegram.saveToRec(stream);
                }
            } catch (IOException e) {
                // kill session
                tcpServer.close(entry.getKey());
                log.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Stream the legacy telegram to all clients that are connected to the legacy TCP server.
     *
     * @param telegram The {@link TlsTele} that should be streamed.
     */
    private void streamToLegacyTcp(TlsTele telegram) {
        for (Map.Entry<Integer, OutputStream> entry : legacyTcpServer.getTcpStreams().entrySet()) {
            OutputStream stream = entry.getValue();
            try {
                if (stream != null) {
                    // write content of the telegram
                    telegram.legacySaveToRec(stream);
                }
            } catch (IOException e) {
                // kill session
                legacyTcpServer.close(entry.getKey());
                log.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Stream the telegram to all clients that are connected to the HTTP server.
     *
     * @param telegram The {@link TlsTele} that should be streamed.
     */
    private void streamToHttp(TlsTele telegram) {
        for (String sessionId : httpServer.getStreams().keySet()) {
            ServletOutputStream stream = httpServer.getStreams().get(sessionId);
            try {
                if (stream != null) {
                    // write the content of the telegram
                    telegram.saveToRec(stream);
                }
            } catch (IOException e) {
                // kill session
                httpServer.getStreams().put(sessionId, null);
                log.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public String getName() {
        return "TeleRecorderJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // not necessary
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // not necessary
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        try {
            fileStream = (Tuple2<String, OutputStream>) context.getResult();
            log.debug("Job executed ... filestream updated for file '{}'", fileStream._1());
        } catch (ClassCastException e) {
            log.error("Failed to update the filestream after job execution. This should not happen \\oO/");
        }
    }
}
