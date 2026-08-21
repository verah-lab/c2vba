package de.heuboe.tls.sequencer.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import de.heuboe.asfinag.tls.cfgchgdetector.ConfigChangeDetector;
import de.heuboe.asfinag.tls.cfggetter.Vmis2TlsCfgGetter;
import de.heuboe.tls.cfglib.INotificationToApp;
import de.heuboe.tls.cfglib.IOsi7Cfg;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.heuboe.tls.grammar.sequencer.ObjectDirection;
import de.heuboe.tls.grammar.sequencer.flops.FlopStorage;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorConstructorData;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorTopicConfiguration;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.sequencer.actors.SpringExtension;
import de.heuboe.tls.sequencer.parser.Parser;
import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import de.heuboe.tls.sequencer.services.SequencerSendingService;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;

/**
 * The bean config class that must be extended by the specific projects.
 */
@Slf4j
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator", "de.heuboe.tls.grammar"})
public class SequencerConfig implements INotificationToApp {

    @Autowired
    protected SequencerBeanContainer sequencerBeanContainer;

    @GrpcClient("ConfigService")
    ConfigServiceBlockingStub configServiceBlockingStub;

    @Getter
    private ActorRef actor;

    /**
     * {@link ActorSystem} Bean creation method.
     *
     * @param applicationContext The current {@link ApplicationContext}.
     * @param springExtension    The current {@link SpringExtension}.
     * @return initialized {@link ActorSystem} bean.
     */
    @Bean
    public ActorSystem actorSystem(ApplicationContext applicationContext, SpringExtension springExtension) {
        ActorSystem system = ActorSystem.create("tls-sequencer");
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * {@link SequencerMessageManagement} Bean creation method.
     *
     * @return initialized {@link SequencerMessageManagement} bean.
     */
    @Bean
    public SequencerMessageManagement sequencerMessageManagement() {
        return new SequencerMessageManagement() {
            @Override
            public void sendMessage(String message) {
                /* Empty because usage of message management is optional for projects. */
            }

            @Override
            public void sendMessage(String message, String objectId) {
                /* Empty because usage of message management is optional for projects. */
            }
        };
    }

    /**
     * Get the name of the topic that will be used for config change detection.
     *
     * @param topicName The name of the topic that should be used config change detection.
     * @return the name of the topic as string.
     */
    @Bean(name = "changeTopic")
    public String getChangeTopic(
            @Value("${de.heuboe.tls.sequencer.config.service.change.topic}") String topicName) {
        return topicName;
    }

    /**
     * Get the name of the group id for the change detection topic.
     *
     * @param groupId The name of the group id for the change detection topic.
     * @return the name of the group id as string.
     */
    @Bean(name = "changeTopicGroupId")
    public String getChangeTopicGroupId(
            @Value("${de.heuboe.tls.sequencer.config.service.change.groupId}") String groupId) {
        return groupId;
    }

    /**
     * {@link ConfigChangeProvider} Bean initialization method.
     *
     * @return an implementation of the {@link ConfigChangeProvider} interface.
     */
    @Bean(name = "configChangeProvider")
    public ConfigChangeProvider getChangeProvider() {
        return new ConfigChangeDetector();
    }

    @Override
    public void configChanged(IOsi7Cfg oldCfg, IOsi7Cfg newCfg) {
        log.info("Configuration changed from '{}' to '{}'.",
                oldCfg.getCfgServiceVersion().getConfigVersion(), newCfg.getCfgServiceVersion().getConfigVersion());
    }

    /**
     * {@link TlsCfgGetter} Bean initialization method.
     *
     * @return initialized {@link TlsCfgGetter} bean.
     */
    @Bean
    public TlsCfgGetter tlsCfgGetter() {
        Vmis2TlsCfgGetter cfgGetter = new Vmis2TlsCfgGetter();
        cfgGetter.setCfgSvc(configServiceBlockingStub);
        return cfgGetter;
    }

    /**
     * {@link Parser} Bean initialization method. At this point the Osi7Cfg will be retrieved from the config service.
     *
     * @param parser               The parser that should be initialized.
     * @param properties           The sequencer properties from the application.properties.
     * @param tlsCfgGetter         The TLSConfigGetter that provides the ConfigService access.
     * @param configChangeProvider The ConfigChangeProvider that provides the config change.
     * @return initialized {@link Parser} bean.
     */
    @Bean
    public Parser initParser(Parser parser, SequencerProperties properties, TlsCfgGetter tlsCfgGetter,
                             ConfigChangeProvider configChangeProvider) {
        // build Osi7Cfg
        Osi7Cfg osi7Cfg = new Osi7Cfg();
        osi7Cfg.setCfgSvc(tlsCfgGetter);
        osi7Cfg.buildUZConfig(properties.getUzid());

        // enable config change detection
        configChangeProvider.register(osi7Cfg);
        osi7Cfg.setAppNotification(this);

        // add Osi7Cfg to bean container to access config in grammar execution
        sequencerBeanContainer.setOsi7Cfg(osi7Cfg);

        // initialize parser with Osi7Cfg
        parser.setOsi7Cfg(osi7Cfg);
        return parser;
    }

    /**
     * {@link SequencerSendingService} Bean initialization method.
     *
     * @param properties The sequencer properties from the application.properties.
     * @return initialized SequencerSendingService bean.
     */
    @Bean
    public SequencerSendingService sequencerSendingService(SequencerProperties properties) {
        SequencerSendingService sequencerSendingService = new SequencerSendingService(properties);
        sequencerBeanContainer.setSequencerSendingService(sequencerSendingService);
        return sequencerSendingService;
    }

    /**
     * {@link KafkaOperatorConstructorData} Bean creation method.
     *
     * @param properties The sequencer properties from the application.properties.
     * @param parser     The initialized {@link Parser}.
     * @return initialized {@link KafkaOperatorConstructorData} bean.
     */
    @Bean
    public KafkaOperatorConstructorData kafkaOperatorConstructorData(SequencerProperties properties, Parser parser) {
        KafkaOperatorConstructorData constructorData =
                new KafkaOperatorConstructorData(properties.getReceiveTopicPrefix(),
                        properties.getReceiveTopicSuffix());

        // create kafkaOperator constructor data based on the topics that were parsed from the scripts
        parser.getTopics().forEach(dataType -> {
            String prefix = properties.getReceiveTopicPrefix();
            String suffix = properties.getReceiveTopicSuffix();

            if (dataType.getDirection() == ObjectDirection.OUT) {
                prefix = properties.getSendTopicPrefix();
                suffix = properties.getSendTopicSuffix();
            }

            // change topic name if necessary
            String topic = dataType.getName();
            if (!dataType.getTargetTopic().isEmpty()) {
                topic = dataType.getTargetTopic();
            }

            // add topic to constructor data
            constructorData.addTopic(KafkaOperatorTopicConfiguration.newBuilder()
                    .topic(topic)
                    .topicPrefix(prefix)
                    .topicSuffix(suffix)
                    .history(dataType.isHistory())
                    .build());
        });

        constructorData.setResponseRetries(properties.getResponseRetries());
        constructorData.setResponseTimeout(properties.getResponseTimeout());

        return constructorData;
    }

    /**
     * {@link KafkaOperatorService} Bean creation method.
     *
     * @param springExtension         The current {@link SpringExtension}.
     * @param actorSystem             The current {@link ActorSystem}.
     * @param constructorData         The initialized {@link KafkaOperatorConstructorData}.
     * @param flopStorage             The {@link FlopStorage} that will hold all flops that are used in runtime.
     * @param parser                  The {@link Parser} that parse the sequencer scripts.
     * @param sequencerSendingService The {@link SequencerSendingService} for sending messages to kafka.
     * @return initialized {@link KafkaOperatorService} bean.
     */
    @Bean(destroyMethod = "stopAllTopicListener")
    public KafkaOperatorService kafkaOperatorService(SpringExtension springExtension,
                                                     ActorSystem actorSystem,
                                                     KafkaOperatorConstructorData constructorData,
                                                     FlopStorage flopStorage,
                                                     Parser parser,
                                                     SequencerSendingService sequencerSendingService) {
        this.actor = actorSystem.actorOf(springExtension.props("sequencerService"), "sequencer");
        KafkaOperatorService kos = new KafkaOperatorService(actor, actorSystem, constructorData);
        sequencerBeanContainer.setFlopStorage(flopStorage);
        parser.setSequencerBeanContainer(sequencerBeanContainer);
        sequencerSendingService.setKafkaOperatorService(kos);
        return kos;
    }
}
