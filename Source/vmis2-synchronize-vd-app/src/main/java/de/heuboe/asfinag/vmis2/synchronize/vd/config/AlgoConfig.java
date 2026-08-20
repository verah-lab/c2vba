package de.heuboe.asfinag.vmis2.synchronize.vd.config;

import java.time.Clock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import akka.actor.ActorSystem;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.synchronize.vd.SystemExit;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.AlgoContext;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.AlgoRunner;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.InfrastructureFromSystem;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.TlsSynVdPublisher;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * Configuration of Algo relevant beans.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 08.08.2019
 *
 */
@Configuration
@ComponentScan(basePackages = {"de.heuboe.asfinag.control.base.actors",
        "de.heuboe.asfinag.control.base.config"})
public class AlgoConfig {

    @GrpcClient("ConfigService")
    ConfigServiceGrpc.ConfigServiceBlockingStub configServiceStub;

    /**
     * ActorSystem Bean creation method.
     *
     * @param springExtension   the springExtension bean.
     * @param applicationContext    the applicationContext bean.
     * @return actor system bean
     */
    @Bean(destroyMethod = "terminate")
    ActorSystem actorSystem(SpringExtension springExtension, ApplicationContext applicationContext) {
        ActorSystem system = ActorSystem.create("vmis2-synchronize-vd-algo");
        springExtension.initialize(applicationContext);
        return system;
    }

    @Bean
    @Profile("default")
    ConfigServiceGrpc.ConfigServiceBlockingStub configServiceBlockingStub() {
        return configServiceStub;
    }

    @Bean
    @Profile("UseServices")
    InfrastructureManager infrastructure(SynchronizeVdProperties properties,
            ConfigServiceGrpc.ConfigServiceBlockingStub configServiceBlockingStub) {
        return new InfrastructureFromSystem(configServiceBlockingStub, properties);
    }

    @Bean
    AlgoContext algoContext(SynchronizeVdProperties properties) {
        return new AlgoContext(properties);
    }

    @Bean
    AlgoRunner algoRunner(ActorSystem actorSystem, SynchronizeVdProperties properties,
            AlgoParameterIdProperties paramIds, AlgoContext algoContext, InfrastructureManager infrastructure) {
        return new AlgoRunner(actorSystem, properties, paramIds, algoContext, infrastructure);
    }
    
    @Bean
    TlsSynVdPublisher tlsSynVdPublisher() {
        return new TlsSynVdPublisher();
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
    
    /**
     * Returns an id generator.
     * 
     * @return id generator
     */
    @Bean
    IDGenerator controlIdGenerator() {
        return new IDGenerator();
    }
    
    @Bean
    @Profile("default")
    SystemExit systemExit() {
        return new SystemExit();
    }
}
