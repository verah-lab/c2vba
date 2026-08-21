package de.heuboe.now.tls.scripts.tests;

import de.heuboe.tls.receiver.interfaces.Transformer;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A base class that provide several static variables and methods for the test classes.
 */
public class NowTestBase { // NOSONAR

    protected static final String TELE_PATH = "target/generatedTelegrams";
    protected static Transformer transformer;
    protected static int dstOffset = 0;

    /**
     * This method will set up the test environment.
     *
     * @throws IOException if something went wrong while creating the {@link Transformer}.
     */
    @BeforeAll
    public static void setUp() throws IOException {

        // create output directory for telegrams
        File directory = new File(TELE_PATH);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // set offset for timezone
        if (!ZoneId.systemDefault().getRules().getDaylightSavings(Instant.now()).isZero()) {
            dstOffset = 128;
        }

        transformer = new NowRcvTestTransformer();
    }
}
