package de.now.tls.legacy.data.converter.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import de.heuboe.asfinag.tls.cfgchgdetector.ConfigChangeDetector;
import de.heuboe.asfinag.tls.cfggetter.Vmis2TlsCfgGetter;
import de.heuboe.tls.cfglib.INotificationToApp;
import de.heuboe.tls.cfglib.IOsi7Cfg;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorConstructorData;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorTopicConfiguration;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.now.tls.legacy.data.converter.actors.SpringExtension;
import de.now.tls.legacy.data.converter.model.LegacyDataConverterDevices;
import de.now.tls.legacy.data.converter.utils.LegacyDataConverterUtils;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.tls.received.*;
import eu.vmis_ehe.vmis2.tls.send.SteuerSequenz;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The configuration class for the service. Here we will initialize the {@link KafkaOperatorConstructorData} object and
 * define all topics the {@link KafkaOperatorService} should listen to.
 *
 * @author alexandero
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator", "de.now.tls.legacy.data.converter"})
public class LegacyDataConverterConfig implements INotificationToApp {

    @GrpcClient("ConfigService")
    private ConfigServiceGrpc.ConfigServiceBlockingStub configServiceBlockingStub;

    @Autowired
    private LegacyDataConverterProperties properties;

    @Autowired
    private LegacyDataConverterDevices legacyDataConverterDevices;

    @Getter
    private ActorRef actor;

    /**
     * Create the {@link ActorSystem} we need to communicate with the {@link KafkaOperatorService} as bean.
     *
     * @param applicationContext The {@link ApplicationContext} for initialization.
     * @param springExtension    The {@link SpringExtension} for initialization.
     * @return the {@link ActorSystem}.
     */
    @Bean
    public ActorSystem actorSystem(ApplicationContext applicationContext, SpringExtension springExtension) {
        ActorSystem system = ActorSystem.create("legacy-data-converter");
        springExtension.initialize(applicationContext);
        return system;
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
     * Get the name of the topic that will be used for config change detection.
     *
     * @param topicName The name of the topic that should be used config change detection.
     * @return the name of the topic as string.
     */
    @Bean(name = "changeTopic")
    public String getChangeTopic(
            @Value("${de.now.tls.legacy.data.converter.config.service.change.topic}") String topicName) {
        return topicName;
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

    /**
     * Get the name of the group id for the change detection topic.
     *
     * @param groupId The name of the group id for the change detection topic.
     * @return the name of the group id as string.
     */
    @Bean(name = "changeTopicGroupId")
    public String getChangeTopicGroupId(
            @Value("${de.now.tls.legacy.data.converter.config.service.change.groupId}") String groupId) {
        return groupId;
    }

    @Override
    public void configChanged(IOsi7Cfg oldCfg, IOsi7Cfg newCfg) {
        log.info("Configuration changed from '{}' to '{}'.",
                oldCfg.getCfgServiceVersion().getConfigVersion(), newCfg.getCfgServiceVersion().getConfigVersion());
        legacyDataConverterDevices.collectLegacyDevices();
    }

    /**
     * Create the {@link KafkaOperatorConstructorData} object as bean. Here we will configure all topics the
     * {@link KafkaOperatorService} should listen to.
     *
     * @return the {@link KafkaOperatorConstructorData}.
     */
    @Bean
    public KafkaOperatorConstructorData kafkaOperatorConstructorData() {
        KafkaOperatorConstructorData constructorData = new KafkaOperatorConstructorData("", "");

        if (properties.getTopicPrefixReceive().isEmpty()) {
            log.error("The property 'tls.legacy.data.converter.topic.prefix.receive' must not be empty!");
            System.exit(-1);
        }

        if (properties.getTopicPrefixSend().isEmpty()) {
            log.error("The property 'tls.legacy.data.converter.topic.prefix.send' must not be empty!");
            System.exit(-1);
        }

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixReceive() + WVZGrundeinstellung32.class.getSimpleName())
                        .build());

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixReceive() + WZGGrundeinstellung33.class.getSimpleName())
                        .build());

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixReceive() + WVZStellzustand48.class.getSimpleName())
                        .build());

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixReceive() + WZGStellzustand55.class.getSimpleName())
                        .build());

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixSend() + WZGGrundeinstellung.class.getSimpleName() + "Soll")
                        .build());

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixSend() + WZGStellzustand.class.getSimpleName() + "Soll")
                        .build());

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic(properties.getTopicPrefixSend() + "WZG" + SteuerSequenz.class.getSimpleName())
                        .build());

        constructorData.setResponseRetries(properties.getResponseRetries());
        constructorData.setResponseTimeout(properties.getResponseTimeout());

        return constructorData;
    }

    /**
     * Create the {@link KafkaOperatorService} as bean.
     *
     * @param springExtension The {@link SpringExtension} bean.
     * @param actorSystem     The {@link ActorSystem} bean.
     * @param constructorData The {@link KafkaOperatorConstructorData} bean.
     * @return the {@link KafkaOperatorService}.
     */
    @Bean
    public KafkaOperatorService kafkaOperatorService(SpringExtension springExtension,
                                                     ActorSystem actorSystem,
                                                     KafkaOperatorConstructorData constructorData,
                                                     TlsCfgGetter tlsCfgGetter,
                                                     ConfigChangeProvider configChangeProvider,
                                                     LegacyDataConverterUtils utils) {

        // this seems not to be the best position
        // build Osi7Cfg
        Osi7Cfg osi7Cfg = new Osi7Cfg();
        osi7Cfg.setCfgSvc(tlsCfgGetter);
        osi7Cfg.buildUZConfig(properties.getUzId());

        // enable config change detection
        configChangeProvider.register(osi7Cfg);
        osi7Cfg.setAppNotification(this);

        // add config getter to legacy device bean
        legacyDataConverterDevices.setTlsCfgGetter(tlsCfgGetter);

        utils.setOsi7Cfg(osi7Cfg);
        legacyDataConverterDevices.collectLegacyDevices();

        this.actor = actorSystem.actorOf(springExtension.props("legacyDataConverterService"), "legacyDataConverter");
        return new KafkaOperatorService(actor, actorSystem, constructorData);
    }
}
