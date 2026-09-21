package de.heuboe.asfinag.control.base.config;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.control.base.messages.HealthMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {CheckerHealthIndicatorTest.HealthApplication.class,
        CheckerHealthIndicatorTest.Configuration.class, SpringExtension.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {"management.endpoint.health.show-details=always"})
class CheckerHealthIndicatorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private CheckerHealthIndicator checkerHealthIndicator;

    @TestConfiguration
    static class Configuration {

        @Bean
        ActorSystem actorSystem() {
            return ActorSystem.create();
        }
    }

    public static class TestActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(HealthMessage.AskStatus.class, t -> sender().tell(HealthMessage.AppStatus.builder().status(
                            HealthMessage.Status.OK).build(), self())).build();
        }
    }

    @SpringBootApplication
    static class HealthApplication {
        public static void main(String[] args) {
            SpringApplication.run(HealthApplication.class, args);
        }
    }

    @Test
    void missingActor_test() throws Exception {
        checkerHealthIndicator.setActorRef(null);

        mockMvc.perform(get("/actuator/health/checker"))
                .andExpect(status().is(503))
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$..Failure").value("Missing health checker actor."));
    }

    @Test
    void timeoutExceptionTest_test() throws Exception {
        new TestKit(actorSystem) {
            {
                final TestKit probe = new TestKit(actorSystem);

                checkerHealthIndicator.setActorRef(probe.getRef());

                mockMvc.perform(get("/actuator/health/checker"))
                        .andExpect(status().is(503))
                        .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"));
            }
        };
    }

    @Test
    void ok_test() throws Exception {
        new TestKit(actorSystem) {
            {
                ActorRef testActor = actorSystem.actorOf(Props.create(TestActor.class));

                checkerHealthIndicator.setActorRef(testActor);

                mockMvc.perform(get("/actuator/health/checker"))
                        .andExpect(status().is(200))
                        .andExpect(jsonPath("$.status").value("UP"));
            }
        };
    }
}
