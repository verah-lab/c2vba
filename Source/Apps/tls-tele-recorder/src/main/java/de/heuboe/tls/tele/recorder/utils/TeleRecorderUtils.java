package de.heuboe.tls.tele.recorder.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;
import de.heuboe.tls.receiver.rdr.item.TimeItem;
import de.heuboe.tls.tel.io.TLSCommState;
import de.heuboe.tls.tel.io.TeleSReceived;
import de.heuboe.tls.tel.io.TeleSToSend;
import de.heuboe.tls.tele.recorder.config.TeleRecorderProperties;
import de.heuboe.tls.tele.recorder.model.TeleRecorderCleanLogsStrategy;
import de.heuboe.tls.tlstele.TlsBadTele;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.meta.Direction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static java.util.Map.entry;

/**
 * This interface must be implemented and used in the bean configuration of the implementing project.
 */
@Slf4j
@Component
public class TeleRecorderUtils {

    public static final String FILE_ENDING = ".lts";
    public static final String GZIP_FILE_ENDING = ".gz";
    public static final String PATH_DELIMITER = "/";

    @Autowired
    private TeleRecorderProperties properties;

    private Map<Integer, Map<Integer, TimeGetter>> timeGetter;

    /**
     * Do some initial stuff after bean creation.
     */
    @PostConstruct
    private void init() {
        // create the TimeGetter we need for telegram manipulation
        timeGetter = createTimeGetter();
    }

    /**
     * Creates a {@link TlsTele} from a byte array. The byte array must be an object of the type {@link TeleSReceived}
     * or {@link TeleSToSend}.
     *
     * @param binaryTelegram The byte array that contains the telegram.
     * @param topic          The topic the telegram came from.
     * @return the {@link TlsTele} that was created from the input byte array.
     */
    public TlsTele createTelegram(byte[] binaryTelegram, String topic) {
        try {
            byte[] singleTelegram;
            int realAddress;
            Direction direction;

            if (topic.equals(properties.getReceiveTopic())) {
                // build telegram that came from receive topic
                TeleSReceived teleReceived = TeleSReceived.parseFrom(binaryTelegram);
                singleTelegram = teleReceived.getTlsSTel().toByteArray();
                realAddress = teleReceived.getRealAddress();
                direction = Direction.RECEIVE;
            } else if (topic.equals(properties.getSendTopic())) {
                // build telegram that came from send topic
                TeleSToSend teleSend = TeleSToSend.parseFrom(binaryTelegram);
                singleTelegram = teleSend.getOsi7Tel().toByteArray();
                realAddress = teleSend.getRealAddress();
                direction = Direction.SEND;
            } else {
                log.warn("Message from not supported topic '{}' received!", topic);
                return null;
            }

            // create TlsTele telegram and return it
            return new TlsTele(new Date(), direction, realAddress, singleTelegram, 0, singleTelegram.length);

        } catch (TlsBadTele | InvalidProtocolBufferException | NullPointerException e) {
            try {
                // check if it is a comm state telegram and print it to log
                TLSCommState teleCommState = TLSCommState.parseFrom(binaryTelegram);
                log.debug(
                        "Comm state telegram received! Telegram will not be saved, only printed to log (DEBUG level).");
                log.warn("Telegram contains: {}", binaryTelegram);
                log.debug("IID: {}", teleCommState.getIid());
                log.debug("Address: {}", teleCommState.getAddress());
                log.debug("TimeSent: {}", teleCommState.getTimeSent());
                log.debug("Alive: {}", teleCommState.getAlive());
                log.debug("Queried: {}", teleCommState.getQueried());
            } catch (InvalidProtocolBufferException | NullPointerException ie) {
                log.debug("Telegram skipped. Error constructing TlsTele received via Kafka: {}", ie.getMessage());
                log.debug("Telegram contains: {}", binaryTelegram);
                return null;
            }

            return null;
        }
    }

    /**
     * Get a list of telegram names saved on the server.
     *
     * @return A list of telegram names.
     */
    public List<String> getTelegramList(boolean newestFirst) {
        File[] files = new File(properties.getAbsolutLogPath()).listFiles(pathname -> {
            String name = pathname.getName().toLowerCase();
            return (name.endsWith(FILE_ENDING) || name.endsWith(GZIP_FILE_ENDING)) && pathname.isFile();
        });

        if (files != null) {
            Stream<String> tmp = Arrays.stream(files)
                    .map(File::getAbsolutePath);

            if (newestFirst) {
                return Arrays.stream(files)
                        .map(File::getAbsolutePath)
                        .sorted((o1, o2) -> (o2.compareTo(o1)))
                        .collect(Collectors.toList());
            } else {
                return Arrays.stream(files)
                        .map(File::getAbsolutePath)
                        .sorted()
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    /**
     * Compress the passed file (including file path) with GZIP and return if the process was successfull.
     *
     * @param file The path and the name of the file that should be compressed.
     * @return true if compression was successful, else false.
     */
    public boolean compressLog(String file) {

        // get the file name for logging
        String pureFileName = getFileNameFromPath(file);

        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(file + GZIP_FILE_ENDING));
             FileInputStream fis = new FileInputStream(file)
        ) {
            // copy file
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gos.write(buffer, 0, len);
            }
            log.info("Compressed '{}' to '{}'.", pureFileName, pureFileName + GZIP_FILE_ENDING);
            return true;
        } catch (IOException e) {
            log.error("Compression of '{}' failed with message: {}", pureFileName, e.getMessage());
        }
        return false;
    }

    /**
     * Add the absolut telegram log path to the passed file name and return it.
     *
     * @param fileName The name of the file the absolut log path should be added to.
     * @return the absolut log path including the passed file name.
     */
    public String getFileNameWithPath(String fileName) {
        return properties.getAbsolutLogPath() + TeleRecorderUtils.PATH_DELIMITER + fileName;
    }

    /**
     * Extract the file name from the passed full file name (path including file name).
     *
     * @param fullFileName The path and the file name.
     * @return the extracted file name.
     */
    public String getFileNameFromPath(String fullFileName) {
        String result = fullFileName.substring(fullFileName.lastIndexOf(PATH_DELIMITER) + 1);
        if (result.equals(fullFileName)) {
            result = fullFileName.substring(fullFileName.lastIndexOf("\\") + 1);
        }
        return result;
    }

    /**
     * Delete the file with the passed name from the configured absolutLogPath in {@link TeleRecorderProperties}.
     *
     * @param file The name of the file that should be deleted.
     * @return true if file was deleted, else false.
     */
    public boolean deleteFile(String file) {
        try {
            Files.delete(new File(file).toPath());
            log.debug("Log file '{}' deleted.", getFileNameFromPath(file));
            return true;
        } catch (IOException e) {
            log.warn("Log file '{}' could not be deleted with message: {}", getFileNameFromPath(file), e.getMessage());
            return false;
        }
    }

    /**
     * Check if the log directory exists and create it if not.
     *
     * @param absolutLogPath The path to the log directory.
     */
    public void checkDirectory(String absolutLogPath) {
        File dir = new File(absolutLogPath);
        if (!dir.exists() && !dir.mkdir()) {
            log.error("Directory '{}' could not be created! Service stopped.", absolutLogPath);
            System.exit(-1);
        }
    }

    /**
     * Clean the log folder depending on the configured cleaning strategy.
     */
    public void cleanTelegrams() {
        if (properties.getCleanLogs().getStrategy() == TeleRecorderCleanLogsStrategy.NUMBER) {
            cleanTelegramsByNumber();
        } else if (properties.getCleanLogs().getStrategy() == TeleRecorderCleanLogsStrategy.SIZE) {
            cleanTelegramsBySize();
        }
    }

    /**
     * Clean the log folder based on a log file number strategy.
     */
    public void cleanTelegramsByNumber() {
        if (properties.getCleanLogs().getNumber() > 0) {

            log.trace("Cleaning logs.");

            // get a sorted list of all log files (file ending independent!)
            List<String> fileList = getTelegramList(false);

            // check if size of log files is greater than defined size
            if (fileList.size() > properties.getCleanLogs().getNumber()) {
                while (fileList.size() > properties.getCleanLogs().getNumber()) {
                    if (deleteFile(fileList.get(0))) {
                        fileList.remove(0);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Clean the log folder based on a log file size strategy.
     */
    public void cleanTelegramsBySize() {
        if (properties.getCleanLogs().getSize() > 0) {

            log.trace("Cleaning logs.");

            // get a sorted list of all log files (file ending independent!)
            List<String> fileList = getTelegramList(true);

            float size = 0;
            List<String> filesToDelete = new ArrayList<>();

            // calculate current size of all log files
            for (String file : fileList) {
                try {
                    size += Files.size(Paths.get(file));
                    // if the configured file size was reached ...
                    if ((size / 1024) > properties.getCleanLogs().getSize() * 1024) {
                        // ... mark all following files
                        filesToDelete.add(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // remove all marked files
            filesToDelete.forEach(this::deleteFile);
        }
    }

    /**
     * Manipulate the real address of the passed telegram. A complete new telegram object will be created because
     * {@link TlsTele} currently does not support a direct manipulation of values.
     *
     * @param telegram    The {@link TlsTele} telegram that should be manipulated.
     * @param realAddress The new real address of the telegram.
     * @return a new {@link TlsTele} telegram with the new realAddress.
     */
    public TlsTele updateRealAddress(TlsTele telegram, int realAddress) {
        try {
            log.debug("Changing read address from '{}' to '{}' for telegram {}", telegram.getRealAddress(),
                    properties.getUpdateRealAddress(), telegram);
            return new TlsTele(telegram.getTimeStamp(), telegram.getDirection(), realAddress, telegram.getBytes(), 0,
                    telegram.getSize());
        } catch (TlsBadTele e) {
            log.error("Real address of telegram could not be manipulated. Leaving telegram unchanged!");
            return telegram;
        }
    }

    /**
     * This will manipulate the timestamps of the telegram. Every single telegram will be handled separately. In every
     * single telegram a special timestamp containing DE block with the DE number 255 will be searched and removed. The
     * timestamp of the removed DE block will be added as bytes at the end of all other DE blocks of the handled single
     * telegram. If no timestamp DE block could be found the current time will be used.
     *
     * @param telegram The {@link TlsTele} telegram the timestamp should be manipulated in.
     */
    public void manipulateTimestamp(TlsTele telegram) {
        // check all single telegrams
        for (TlsETel etel : telegram.getEtels()) {
            // extract time bytes from single telegram
            byte[] timeBytes = getTimeBytesAndRemoveDEBlock(etel);

            // add timestamp at the end of each remaining DE block
            for (TlsDeBlock deBlock : etel.getDeblocks()) {
                log.debug("Appending time as byte array {} to DE block with DE type {} and DE number {}",
                        timeBytes, deBlock.getDeTyp(), deBlock.getDeNr());
                deBlock.setContent(appendByteArray(deBlock.getContent(), timeBytes));
            }
        }
    }

    /**
     * This will extract the time bytes as byte array from the passed single telegram. If the single telegram contains a
     * DE block with the DE number 255 and a matching DE type the time will be extracted from this block. Further this
     * DE block will be removed from the single telegram. If no such DE block could be found, the current time will be
     * used.
     *
     * @param etel The {@link TlsETel} single telegram the time bytes should be extracted from.
     * @return an array of 4 time bytes that represent the extracted time.
     */
    private byte[] getTimeBytesAndRemoveDEBlock(TlsETel etel) {

        List<TlsDeBlock> markedForRemoval = new ArrayList<>();
        byte[] timeBytes = new byte[4];

        // use current timestamp if we can not extract information from the telegram
        long time = new Date().getTime() / 1000;

        if (etel.getDeblockCount() > 1) {

            Map<String, DataItem> timeItem = new HashMap<>();
            timeItem.put("#ActualTime", new TimeItem("#ActualTime", new Date(), 0));

            // we must search for DE block 255 that will contain the necessary timestamp, per TLS definition the DE
            // block with the DE number 255 must be the first DE block
            TlsDeBlock deBlock = etel.getDeblocks().get(0);

            if (deBlock.getDeNr() == 255) {
                // for different DE types use different TimeGetter
                DataItem dataItem = timeGetter
                        .get(etel.getFg())
                        .get(deBlock.getDeTyp())
                        .get(deBlock.getBytes(), 3, timeItem);

                if (dataItem != null) {
                    // if a DE block that represent a time block was found, mark it for removal
                    markedForRemoval.add(deBlock);
                    // get the timestamp from the data item if it was set in seconds
                    time = dataItem.getAsLong() / 1000;

                    log.debug("Time '{}' extracted from DE block with DE typ {} and DE number {} ",
                            time, deBlock.getDeTyp(), deBlock.getDeNr());
                }
            }
        }

        // get relevant bytes from timestamp
        timeBytes[0] = (byte) ((time / (256 * 256 * 256)) % 256); // eHighByte
        timeBytes[1] = (byte) ((time / (256 * 256)) % 256); // vHighByte
        timeBytes[2] = (byte) ((time / 256) % 256); // HighByte
        timeBytes[3] = (byte) (time % 256); // LowByte
        log.debug("Time '{}' converted to byte array {}", time, timeBytes);

        if (!markedForRemoval.isEmpty()) {
            log.debug("Removing {} time block from single telegram with FG {} / Id {}", markedForRemoval.size(),
                    etel.getFg(), etel.getTlsId());
            etel.getDeblocks().removeAll(markedForRemoval);
        } else {
            log.debug("Using current time '{}' for replacement because no suitable DE block was found!", time);
        }

        return timeBytes;
    }

    /**
     * This will create a nested map that contain all necessary {@link TimeGetter} associated to the fg / type
     * combinations. The returned map will have the following structure:
     * <code><br/>
     * [<br/>
     * &nbsp;&nbsp;FG, [<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;[ deType, {@link TimeGetter } ]<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;[ ... ]<br/>
     * &nbsp;&nbsp;],<br/>
     * &nbsp;&nbsp;..., [ ... ]<br/>
     * ]
     * </code>
     *
     * @return a nested map that contain all {@link TimeGetter} associated to their fg / type combinations.
     */
    private Map<Integer, Map<Integer, TimeGetter>> createTimeGetter() {
        // use the user defined timezone for our TimeGetter
        TimeGetter.setTimeZone(properties.getManipulateTimestampsTimezone());

        // build TimeGetter objects for each fg / type combination
        TimeGetter tg30 = new TimeGetter("h:m:s", "typ30", "int");
        TimeGetter tg31 = new TimeGetter("h:m:s:d", "typ31", "int");
        TimeGetter tg31Fg2 = new TimeGetter("Y:M:d:h:m:s", "typ31", "int");
        TimeGetter tg48Fg1 = new TimeGetter("h:m:s", "typ48", "int");
        TimeGetter tg64Fg1 = new TimeGetter("y:M:d:h", "typ64", "int");
        TimeGetter tg64Fg2 = new TimeGetter("y:M:d:h:m", "typ64", "int");
        TimeGetter tg64Fg9 = new TimeGetter("h:m:s", "typ64", "int");

        // add all TimeGetter into a nested map and return it -> <fg, <deType, TimeGetter>>
        return Map.ofEntries(
                entry(1, Map.ofEntries(
                        entry(30, tg30),
                        entry(48, tg48Fg1),
                        entry(64, tg64Fg1)
                )),
                entry(2, Map.ofEntries(
                        entry(30, tg30),
                        entry(31, tg31Fg2),
                        entry(64, tg64Fg2)
                )),
                entry(3, Map.ofEntries(
                        entry(30, tg30)
                )),
                entry(4, Map.ofEntries(
                        entry(30, tg30),
                        entry(31, tg31)
                )),
                entry(5, Map.ofEntries(
                        entry(30, tg30)
                )),
                entry(6, Map.ofEntries(
                        entry(30, tg30)
                )),
                entry(9, Map.ofEntries(
                        entry(31, tg31),
                        entry(64, tg64Fg9)
                ))
        );
    }

    /**
     * Appends the second byte array at the end of the first byte array.
     *
     * @param baseByteArray      The byte array the second one should be appended at.
     * @param appendingByteArray The byte array that should be appended at the first one.
     * @return the joined byte array.
     */
    private byte[] appendByteArray(byte[] baseByteArray, byte[] appendingByteArray) {
        return ByteBuffer.allocate(baseByteArray.length + appendingByteArray.length)
                .put(baseByteArray)
                .put(appendingByteArray)
                .array();
    }
}
