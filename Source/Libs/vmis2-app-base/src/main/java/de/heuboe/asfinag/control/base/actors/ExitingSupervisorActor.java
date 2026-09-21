package de.heuboe.asfinag.control.base.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

/**
 * The ExitingSupervisor exits the app if the SupervisorActor throws an exception.
 */
@Slf4j
public class ExitingSupervisorActor extends AbstractActor {

    private FiniteDuration withinTimeRange;
    private int retriesUntilExit;

    @Getter
    private ActorRef child;

    private ExitingSupervisorActor(Props childProps, String childName, int retriesUntilExit, FiniteDuration withinTimeRange) {
        this.withinTimeRange= withinTimeRange;
        this.retriesUntilExit = retriesUntilExit;

        child = context().actorOf(childProps, childName);
        context().watch(child);
    }

    private ExitingSupervisorActor(SpringExtension springExtension, String actorBeanName, String childName, int retriesUntilExit, FiniteDuration withinTimeRange, Object... childArgs) {
        this.withinTimeRange= withinTimeRange;
        this.retriesUntilExit = retriesUntilExit;

        if( childArgs == null ) {
            child = context().actorOf(springExtension.props(actorBeanName), childName);
        } else {
            child = context().actorOf(springExtension.props(actorBeanName, childArgs), childName);
        }
        context().watch(child);
    }

    /**
     * Helper to create the Actor
     * @param childProps    Props to create the child
     * @param childName     name of the child
     * @param retriesUntilExit maximum number of restarts
     * @param withinTimeRange within this time window
     * @return Props
     */
    public static Props props(Props childProps, String childName, int retriesUntilExit, FiniteDuration withinTimeRange) {
        return Props.create(ExitingSupervisorActor.class, () -> new ExitingSupervisorActor(childProps, childName, retriesUntilExit, withinTimeRange));
    }

    /**
     * Helper to create the Actor with spring wiring
     * @param springExtension  Akka Extension to provide access to Spring managed Actor Beans
     * @param actorBeanName Name of the Actor Bean (Actor should be of @Scope("prototype")
     * @param childName     name of the child
     * @param retriesUntilExit maximum number of restarts
     * @param withinTimeRange within this time window
     * @param childArgs args to create Actor. May be null
     * @return Props
     */
    public static Props props(SpringExtension springExtension, String actorBeanName, String childName, int retriesUntilExit, FiniteDuration withinTimeRange, Object... childArgs) {
        return Props.create(ExitingSupervisorActor.class, () -> new ExitingSupervisorActor(springExtension, actorBeanName, childName, retriesUntilExit, withinTimeRange, childArgs));
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(retriesUntilExit, withinTimeRange, true, DeciderBuilder
            .matchAny(throwable -> SupervisorStrategy.stop())
            .build()
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, this::handleTermination)
                .matchAny(o -> child.forward(o, context()))
                .build();
    }

    private void handleTermination(Terminated t) {
        log.info("Child {} is terminated! Exiting Programm", t.actor().path());
        System.exit(1);
    }
}
