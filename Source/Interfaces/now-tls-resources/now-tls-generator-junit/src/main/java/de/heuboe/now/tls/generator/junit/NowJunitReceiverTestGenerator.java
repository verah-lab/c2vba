package de.heuboe.now.tls.generator.junit;

import de.heuboe.tls.resources.generator.RcvScriptGetter;
import de.heuboe.tls.resources.generator.SendScriptGetter;
import de.heuboe.tls.resources.generator.java.JavaCreator;
import de.heuboe.tls.resources.generator.junit.receiver.JunitReceiverJavaGenerator;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * Main class, that generates a test for each datatype of a rcv-script.
 */
public class NowJunitReceiverTestGenerator {

    /**
     * The main method for generating receiver tests.
     *
     * @param args A list of arguments.
     * @throws TemplateException if a problem with the freemarker template occurs.
     * @throws IOException if something went wrong while generating the test class.
     */
    public static void main(String[] args) throws TemplateException, IOException {
        String absoluteTargetPath;

        // get path of current class
        File f = new File(NowJunitReceiverTestGenerator.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath());
        // will return something like <Path on your system>\now-tls-resources\now-tls-generator-junit\target
        String path = f.getParent();
        // remove target
        path = path.substring(0, path.lastIndexOf("\\"));
        // remove  now-tls-generator-java
        path = path.substring(0, path.lastIndexOf("\\"));

        if (args.length == 0) {
            // attach new package path for output
            absoluteTargetPath = path + "\\now-tls-generator-junit\\target\\generatedRcvTests\\";

        } else if (args.length == 1) {
            // target path for the java files
            absoluteTargetPath = args[0];
        } else {
            throw new IllegalArgumentException("Too many parameters");
        }

        // create project specific script getter
        RcvScriptGetter rcvScriptGetter = new RcvScriptGetter();
        SendScriptGetter sendScriptGetter = new SendScriptGetter();

        JunitReceiverJavaGenerator generator = new JunitReceiverJavaGenerator(null, null, null,
                "RcvJUnitTestTemplate.ftl", rcvScriptGetter, sendScriptGetter, absoluteTargetPath,
                "eu.vmis_ehe.vmis2.tls", null, null, true, new JavaCreator(), false);

        // call generation method for all necessary scripts
        generator.generate("rcv-fg-custom.txt");
    }
}
