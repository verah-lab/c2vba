package de.now.tls.legacy.data.converter.actors;

import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * An Akka Extension to provide access to Spring managed Actor Beans.
 */
@Component
public class SpringExtension implements Extension {

    private ApplicationContext applicationContext;

    /**
     * Used to initialize the Spring application context for the extension.
     *
     * @param applicationContext The spring application context.
     */
    public void initialize(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Create a Props for the specified actorBeanName using the SpringActorProducer class.
     *
     * @param actorBeanName The name of the actor bean to create Props for.
     * @return a Props that will create the named actor bean using Spring.
     */
    public Props props(String actorBeanName) {
        return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, null);
    }

    /**
     * Create a Props for the specified actorBeanName using the SpringActorProducer class.
     *
     * @param actorBeanName The name of the actor bean to create Props for.
     * @param args          The argument of the actor bean.
     * @return a Props that will create the named actor bean using Spring.
     */
    public Props props(String actorBeanName, Object... args) {
        return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, args);
    }
}
