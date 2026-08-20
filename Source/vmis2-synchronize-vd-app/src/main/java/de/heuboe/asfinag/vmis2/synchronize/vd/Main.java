package de.heuboe.asfinag.vmis2.synchronize.vd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Main Class to start the service in stand-alone mode.
 */
@SpringBootApplication
@Slf4j
public class Main {

    /**
     * Main method to start the application.
     * <p>
     * The application can be configured via external properties as supported by Spring Boot. External
     * properties might be properties specified in a property file, as system properties or as program
     * arguments. Possible properties are documented in the {@code application.properties} file that is
     * located in this jar.
     *
     * @param args application properties can be provided as arguments in Spring Boot notation (
     *        {@code --property=value}) but also as system properties or in an property file.
     *
     * @see <a href=
     *      "http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config">Spring-Boot
     *      (for detailed explanation of external configuration possibilities)</a>
     *
     */
    public static void main(final String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.setAdditionalProfiles("default", "UseServices");
        app.run(args);
    }

}
