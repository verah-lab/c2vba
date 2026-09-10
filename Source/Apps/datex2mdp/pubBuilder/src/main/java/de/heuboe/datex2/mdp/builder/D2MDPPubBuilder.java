package de.heuboe.datex2.mdp.builder;

import java.time.Instant;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;

import de.heuboe.datex2.base.config.SchemaConfig;
import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.mdp.builder.writer.D2MDPPubWriter;
import de.heuboe.datex2.push.D2PubSenderMDM;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.mdp.D2LogicalModel;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

/**
 * Main class
 */
@SpringBootApplication
@Slf4j
public class D2MDPPubBuilder {

    /**
     * main
     * @param args args
     */
    public static void main(String[] args) {
        SpringApplication.run(D2MDPPubBuilder.class, args);
    }

    @Autowired
    private D2MDPPubWriter writer;
    @Autowired
    private D2MDPPublisherVSInfo fileWriter;
    @Autowired
    private D2PubSenderMDM publisher;

    @Autowired
    private SchemaConfig schemaConfig;

    /**
     * Published in a fix intervall the current data
     *
     * @throws JAXBException if unmarshal to xml document fails
     * @throws D2ExceptionBase if push to MDM or file fails
     */
    @Scheduled(cron = "${cron.expression}")
    public void publish() throws JAXBException, D2ExceptionBase {
        Date pubtime = Date.from(Instant.now());
        Option<D2LogicalModel> model = writer.parseMst(pubtime);
        if (model.isDefined()) {
            JAXBUtil jaxbUtil = new JAXBUtil(schemaConfig.getDatex2SchemaFile(), D2LogicalModel.class.getPackageName(), schemaConfig.getDatex2SchemaNameSpace(), true);
            String document = jaxbUtil.getDocument(model.get(), "d2LogicalModel", D2LogicalModel.class);
            fileWriter.publish(document);
            publisher.publish(document);
        }
    }
}
