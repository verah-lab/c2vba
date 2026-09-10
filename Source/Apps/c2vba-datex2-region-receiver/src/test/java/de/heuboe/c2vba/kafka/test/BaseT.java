package de.heuboe.c2vba.kafka.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.heuboe.test.result.comparator.ResultComparator;

@ExtendWith(SpringExtension.class)
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "de.heuboe.kafka.listener",
							    "de.heuboe.c2vba.kafka.test" })
@TestPropertySource(locations="classpath:/springCfgTest/")
public class BaseT {

    public static final String BASE_SUPPLY_DIR = "map";
    
    protected static ResultComparator trc = null;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
    
    @Autowired
    protected ConfigurableApplicationContext primaryCtx;

    protected static boolean isInitialized = false;
    
    protected boolean initWls() {
        return true;
    }
    
    protected void postInit()  throws Exception {
    }

    protected void preInit() throws Exception  {
    }

    @BeforeAll
    public static void setUp() throws Exception {
        
        System.setProperty( "spring.config.location", "classpath:/springCfgTest/application.properties" );
        
        ResultComparator.createFolders( true );
        trc = ResultComparator.createComparator(
                 System.getProperty("user.dir") + "/src/test/resources/checkResult",
                 System.getProperty("user.dir") + "/src/test/resources/result",
                 5 );
    }
}
