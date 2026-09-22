package de.heuboe.asfinag.vmis2.tls.resources.generator.proto;

import de.heuboe.tls.resources.generator.proto.ProtoGenerator;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * Main class, that generates all protobuf files.
 */
public class Vmis2ProtoGenerator {

    /**
     * Generates protobuf files for the following funktionsgruppen: 0 (header), 254, 1, 2, 3, 4, 6, 9
     *
     * @param args The absoulte output path for the protobuf files.
     * @throws TemplateException Freemarker Runtime Exception
     * @throws IOException       If the scripts are not found
     */
    public static void main(String[] args) throws TemplateException, IOException {
        String absoluteTargetPath;

        if (args.length == 0) {
            // get path of current class
            File f = new File(Vmis2ProtoGenerator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            // will return something like <Path on your system>\vmis2-tls-resources\vmis2-tls-generator-java\target
            String path = f.getParent();
            // remove target
            path = path.substring(0, path.lastIndexOf("\\"));
            // remove  tls-generator-proto
            path = path.substring(0, path.lastIndexOf("\\"));

            // attach new package path for output
            absoluteTargetPath = path + "\\vmis2-tls-proto-interface\\src\\main\\proto\\";

        } else if (args.length == 1) {
            // relative target path for the proto files
            // if executed directly use '../vmis2-tls-proto-interface/src/main/proto/'
            absoluteTargetPath = args[0];

        } else {
            throw new IllegalArgumentException("Too many parameters");
        }

        // create project specific script getter
        Vmis2RcvScriptGetter rcvScriptGetter = new Vmis2RcvScriptGetter();
        Vmis2SendScriptGetter sendScriptGetter = new Vmis2SendScriptGetter();

        ProtoGenerator generator = new ProtoGenerator("ProtoHeaderTemplate.ftl", "ProtoMessageTemplate.ftl",
                rcvScriptGetter, sendScriptGetter, absoluteTargetPath, "eu.vmis_ehe.vmis2.tls", false);

        generator.generateProto("rcv-fg-custom.txt", "FgCustom.proto");
    }

}
