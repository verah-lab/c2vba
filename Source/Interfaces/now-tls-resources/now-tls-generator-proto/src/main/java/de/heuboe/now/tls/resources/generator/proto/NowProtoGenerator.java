package de.heuboe.now.tls.resources.generator.proto;

import de.heuboe.tls.resources.generator.proto.ProtoGenerator;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * Main class, that generates all protobuf files.
 */
public class NowProtoGenerator {

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
            File f = new File(NowProtoGenerator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            // will return something like <Path on your system>\now-tls-resources\now-tls-generator-java\target
            String path = f.getParent();
            // remove target
            path = path.substring(0, path.lastIndexOf("\\"));
            // remove  tls-generator-proto
            path = path.substring(0, path.lastIndexOf("\\"));

            // attach new package path for output
            absoluteTargetPath = path + "\\now-tls-proto-interface\\src\\main\\proto\\";

        } else if (args.length == 1) {
            // relative target path for the proto files
            // if executed directly use '../now-tls-proto-interface/src/main/proto/'
            absoluteTargetPath = args[0];

        } else {
            throw new IllegalArgumentException("Too many parameters");
        }

        // create project specific script getter
        NowRcvScriptGetter rcvScriptGetter = new NowRcvScriptGetter();
        NowSendScriptGetter sendScriptGetter = new NowSendScriptGetter();

        // we must ignore all default scripts to enable the override of a receiver script
        ProtoGenerator generator = new ProtoGenerator("ProtoHeaderTemplate.ftl", "ProtoMessageTemplate.ftl",
                rcvScriptGetter, sendScriptGetter, absoluteTargetPath, "eu.vmis_ehe.vmis2.tls", true);

        generator.generateProto("rcv-fg-custom.txt", "FgCustom.proto");

        // manually generate all default scripts
        generator.generateProto("rcv-fg254.txt", "Fg254.proto");
        generator.generateProto("rcv-fg1.txt", "Fg1.proto");
        generator.generateProto("rcv-fg2.txt", "Fg2.proto");
        generator.generateProto("rcv-fg3.txt", "Fg3.proto");
        // replace default fg4 script with project specific one
        generator.generateProto("now-rcv-fg4.txt", "Fg4.proto");
        generator.generateProto("rcv-fg6.txt", "Fg6.proto");
        generator.generateProto("rcv-fg9.txt", "Fg9.proto");
        generator.generateProto("rcv-fg-heuboe.txt", "FgHeuboe.proto");
        generator.generateProto("send-fake-fg-all.txt", "Send.proto");
        generator.generateProto("send-fake-fg-SteuerSequenz.txt", "SteuerSequenz.proto");
    }

}
