package de.heuboe.asfinag.control.base.config;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import de.heuboe.asfinag.control.base.messages.HealthMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Spring Boot health indicator
 */
@Slf4j
@Component
public class CheckerHealthIndicator implements HealthIndicator {

    /**
     * Timeout for the health checker actor.
     */
    private final long timeOutInMilliSeconds;

    /**
     * Health checker actor.
     */
    private ActorRef healthChecker;

    /**
     * Constructor.
     * @param timeOutInMilliSeconds Timeout for the health checker actor.
     */
    public CheckerHealthIndicator(@Value("${de.heuboe.asfinag.control.base.health.askStatusMsgTimeout:5000}")
        long timeOutInMilliSeconds) {
        this.timeOutInMilliSeconds = timeOutInMilliSeconds;
    }

    /**
     * Sets the health checker actor.
     *
     * @param healthChecker to get the system status from
     */
    public void setActorRef(ActorRef healthChecker) {
        this.healthChecker = healthChecker;
    }

    @Override
    public Health health() {
        if (healthChecker == null) {
            return buildHealthObject(HealthMessage.AppStatus.builder().status(HealthMessage.Status.NOT_OK)
                    .failureMessage("Missing health checker actor.").build());
        }
        Timeout timeout = new Timeout(Duration.create(timeOutInMilliSeconds, TimeUnit.MILLISECONDS));
        log.debug("Ask health state - sending AskStatus message with timeout {}ms", timeOutInMilliSeconds);
        Future<Object> ask = Patterns.ask(healthChecker, HealthMessage.AskStatus.builder().build(), timeout);

        HealthMessage.AppStatus appStatus = null;

        try {
            appStatus = (HealthMessage.AppStatus) Await.result(ask, timeout.duration());
        } catch (TimeoutException timeoutException) {
            log.debug("Timeout exception occurred!");
        } catch (Exception exception) {                 //NOSONAR appStatus will be null and handled by buildHealthObject()
            log.error("Unknown health exception", exception);
        }

        return buildHealthObject(appStatus);
    }

    private Health buildHealthObject(HealthMessage.AppStatus appStatus) {
        if (Objects.isNull(appStatus)) {
            log.debug("HealthIndicator returns OUT_OF_SERVICE state");
            return Health.outOfService().build();
        }

        if (appStatus.getStatus().equals(HealthMessage.Status.OK)) {
            log.debug("HealthIndicator returns state UP");
            return Health.up().build();
        } else {
            log.debug("HealthIndicator returns state DOWN with failure {}", appStatus.getFailureMessage());
            return Health.down().withDetail("Failure", appStatus.getFailureMessage()).build();
        }
    }
}
