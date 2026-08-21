package de.heuboe.asfinag.tls.replay.testlve;

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import de.heuboe.asfinag.tls.replay.testlve.config.KafkaConfig;
import eu.vmis_ehe.vmis2.tls.received.LVEBetriebsparameterList;
import eu.vmis_ehe.vmis2.tls.received.LVEDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.LVEErgDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion13List;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion16List;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion21List;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion23List;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion3List;
import eu.vmis_ehe.vmis2.tls.received.LVEKanalsteuerungList;

@Component
@EnableKafka
@ComponentScan( basePackageClasses= {KafkaConfig.class} )
public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private final AtomicInteger counter = new AtomicInteger(0);

    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * The full value constructor which will lead to a valid object (instance) of this class.
     * 
     * @param kafkaTemplate the template that is used for high-level Kafka operations
     */
    @Autowired
    public Receiver(final KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "VMIS_2_tls_LVEBetriebsparameter", groupId="rnTestReplay")
    public void receive00(final LVEBetriebsparameterList payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEBetriebsparameter {} =\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }

    @KafkaListener(topics = "VMIS_2_tls_LVEDeFehler", groupId="rnTestReplay")
    public void receive01(final LVEDeFehlerList payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEDeFehler {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }

    @KafkaListener(topics = "VMIS_2_tls_LVEErgDeFehler", groupId="rnTestReplay")
    public void receive02(final LVEErgDeFehlerList payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEErgDeFehler{}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }

    @KafkaListener(topics = "VMIS_2_tls_LVEKanalsteuerung", groupId="rnTestReplay")
    public void receive03(final LVEKanalsteuerungList payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEKanalsteuerung {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }
    
    @KafkaListener(topics = "VMIS_2_tls_LVEErgebnisVersion3", groupId="rnTestReplay")
    public void receive04(final LVEErgebnisVersion3List payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEErgebnisVersion3 {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }
    
    @KafkaListener(topics = "VMIS_2_tls_LVEErgebnisVersion13", groupId="rnTestReplay")
    public void receive04a(final LVEErgebnisVersion13List payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEErgebnisVersion13 {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }
    
    @KafkaListener(topics = "VMIS_2_tls_LVEErgebnisVersion16", groupId="rnTestReplay")
    public void receive04b(final LVEErgebnisVersion16List payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEErgebnisVersion16 {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }
    
    @KafkaListener(topics = "VMIS_2_tls_LVEErgebnisVersion21", groupId="rnTestReplay")
    public void receive04c(final LVEErgebnisVersion21List payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEErgebnisVersion21 {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }
    
    @KafkaListener(topics = "VMIS_2_tls_LVEErgebnisVersion23", groupId="rnTestReplay")
    public void receive05(final LVEErgebnisVersion23List payload) throws InterruptedException {
        LOGGER.info("received payload VMIS_2_tls_LVEErgebnisVersion23 {}=\n'\n{}'", new Throwable().getStackTrace()[0].getMethodName(), payload);
        this.counter.incrementAndGet();
    }
    
}
