package de.heuboe.datex2.mdp.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.push.D2Publisher;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to write mst to files
 */
@Slf4j
public class D2MDPPublisherVSInfo extends D2Publisher {

    private String pubFilePath;
    private String mstId;

    /**
     * Constructor
     *
     * @param filePath path to write file
     * @param mstId mst id
     */
    public D2MDPPublisherVSInfo(String filePath, String mstId) {
        super();
        this.pubFilePath = filePath;
        this.mstId = mstId;
    }

    @Override
    public String getName() {
        return "File-Writer[" + pubFilePath + "]";
    }

    @Override
    public String publish(String content) throws D2ExceptionBase {
        log.info("Write");
        String fileName = null;
        try {
            if (content != null) {
                fileName = writePublication(content);
            }

        } catch (D2ExceptionBase e) {
            e.printStackTrace();
            throw e;
        }

        return fileName;
    }

    private String writePublication(String writeContent) throws D2ExceptionBase {
        // Ausgabe in Datei

        log.debug("Start D2MDPPublisherVInfo::writePublication()");

        String outputLocation = null;

        String fileName = "D2MDPPub_" + mstId + "_" + System.currentTimeMillis() + ".xml";

        String path = pubFilePath + System.getProperty("file.separator") + fileName;

        log.debug("Datei: " + path);

        try (FileOutputStream stream = new FileOutputStream(path)){
            stream.write(writeContent.getBytes("UTF-8"));

            File f = new File(path);
            long lm = f.lastModified();


            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date(lm));

            outputLocation = path;
        } catch (FileNotFoundException ex) {
            log.error("Fehler beim Schreiben der MST-Datei '" + path + "'");
            throw new D2MDPPubException(D2MDPPubException.D2_ERR_WRITE_TO_FILE,
                    ex.getMessage());
        } catch (IOException ex) {
            log.error("Fehler beim Schreiben der MST-Datei '" + path + "'");
            throw new D2MDPPubException(D2MDPPubException.D2_ERR_WRITE_TO_FILE,
                    ex.getMessage());
        }

        return outputLocation;
    }
}
