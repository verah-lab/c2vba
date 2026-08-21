package de.heuboe.tls.iface.prot.tlsoip.test;

import de.heuboe.tls.iface.prot.tlsoip.ConnectionConfig;
import de.heuboe.tls.iface.prot.tlsoip.TlsOverIp;
import de.heuboe.tls.iface.prot.tlsoip.TlsOverIpConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertTrue;

//@TestPropertySource(
//        properties = {
//                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//                "spring.kafka.consumer.group-id=SenderEmbeddedKafkaTest",
//                "spring.kafka.listener.missing-topics-fatal=true"}
//        locations="classpath:commStat1.properties"               // !!! properties
//        )

//@Slf4j
@EnableConfigurationProperties
//@SpringJUnitConfig

@SpringBootTest(
        properties = {
                //"de.heuboe.tls.sender.config.forceAlive=true" // wins over property from file
        }
        )

@ContextConfiguration( classes = {
        de.heuboe.tls.iface.prot.tlsoip.test.config.Config.class
        
        } )

@EnableAutoConfiguration
public class ConnectionTest {
    
    @Autowired
    TlsOverIpConfig theConfig;
    
    @Test
    public void connectionConfigTest() throws InterruptedException {
        int index = 0;
        for (ConnectionConfig conCfg : theConfig.getConnectionConfigList()) {
            if (null != conCfg.getServerHostB()) {
                break;
            }
            index++;
        }
        System.out.println( "using config at index " + index );
        
        ConnectionConfig cfg = theConfig.getConnectionConfigList().get( index );
        
        TlsOverIp oip = new TlsOverIp( theConfig );
        
        oip.startCommunication( cfg.getOsi2Port(), cfg.getOsi2Address() );
        
        boolean b = true;
        int cnt = 13;
        
        while (b && cnt > 0) {
            Thread.sleep( 10000 ); // NOSONAR test just stop test from terminating
            if (cnt > 0) {
                cnt--;
            }
        }
        assertTrue( cnt == 0 );
    }

}
