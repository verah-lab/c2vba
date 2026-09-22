package de.heuboe.vmis2.jprotoc;

import static de.heuboe.vmis2.jprotoc.GeneratorTestUtils.resourceStream;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class GeneratePojoTest {

    @Test
    /**
     * just test if generator runs through w/o exceptions
     */
    void runGenerationTest() throws Exception {
        // Store existing IO
        final InputStream in = System.in;
        final PrintStream out = System.out;
        // Prepare new IO
        final InputStream input = resourceStream("/meinTestInput.protoB");
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintStream printer = new PrintStream(output);
        // Set new IO
        System.setIn(input);
        System.setOut(printer);
        // Run
        Proto2PojoGenerator.main();
        // Restore old IO
        System.setIn(in);
        System.setOut(out);
        // Evaluate result
        final String result = output.toString();
        assertFalse(result.isEmpty());
    }

}
