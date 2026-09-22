package de.heuboe.vmis2.jprotoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class MainWrapper {

    public static void main(final String[] args) throws IOException {
        // Store existing IO
        final InputStream in = System.in;
        final PrintStream out = System.out;
        // Prepare new IO
        // final InputStream input = GeneratorTestUtils.fileStream("valueWrappersTestInput.protoB");
        //final InputStream input = GeneratorTestUtils.resourceStream("/valueWrappersTestInput.protoB");
        final InputStream input = GeneratorTestUtils.resourceStream("/meinTestInput.protoB");
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintStream printer = new PrintStream(output);
        // Set new IO
        System.setIn(input);
        System.setOut(printer);
        // Run
        Proto2PojoGenerator.main(args);
        // Restore old IO
        System.setIn(in);
        System.setOut(out);
        // Print result
        final String result = output.toString();
        System.out.println(result);
    }

}
