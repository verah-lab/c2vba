package de.heuboe.tls.tele.recorder.job;

import de.heuboe.tls.tele.recorder.config.TeleRecorderProperties;
import de.heuboe.tls.tele.recorder.utils.TeleRecorderUtils;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This is the job class for the Quartz scheduler.
 */
@Slf4j
@Component
@PersistJobDataAfterExecution
public class TeleRecorderJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // extract the current filestream from the job data
            Tuple2<String, OutputStream> fileStream =
                    (Tuple2<String, OutputStream>) context.getJobDetail().getJobDataMap().get("filestream");

            // initialize the utils
            TeleRecorderUtils utils = (TeleRecorderUtils) context.getJobDetail().getJobDataMap().get("utils");

            // initialize the properties
            TeleRecorderProperties properties =
                    (TeleRecorderProperties) context.getJobDetail().getJobDataMap().get("properties");

            GregorianCalendar now = new GregorianCalendar();
            String oldFile = "";

            // close filestream if exists to avoid error while cleaning logs
            if (fileStream._1() != null && fileStream._2() != null) {
                log.debug("Closing filestream for {}", fileStream._1());
                fileStream._2().close();

                // save old file name for later compression and deleting
                oldFile = utils.getFileNameWithPath(fileStream._1());
            }

            // create a new file name
            String filename = String.format("%02d%02d%02d-%02d%02d%02d%s",
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND),
                    TeleRecorderUtils.FILE_ENDING);

            // create the file stream for the new file
            OutputStream os = new FileOutputStream(properties.getAbsolutLogPath() +
                    TeleRecorderUtils.PATH_DELIMITER + filename, true);

            // add filename and stream to job result
            fileStream = new Tuple2<>(filename, os);
            context.setResult(fileStream);
            context.getJobDetail().getJobDataMap().put("filestream", fileStream);

            log.info("New file '{}' generated.", filename);

            // if compression is enabled
            if (properties.isCompressLogs() && !oldFile.isEmpty()) {
                // compress old file
                if (utils.compressLog(oldFile)) {
                    // delete old file if compression was successful
                    utils.deleteFile(oldFile);
                }
            }
        } catch (ClassCastException e) {
            log.error("Failed to cast the job data. This should no happen \\oO/");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
