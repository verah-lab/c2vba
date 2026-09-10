package de.heuboe.sdbby.strategy.matching;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Configuration
@ComponentScan(basePackages = { "de.heuboe.sdbby.strategy.matching" })
@TestPropertySource(locations="classpath:/springCfgTest/application-local.properties")
public class BaseT {
	
}