package de.heuboe.tls.iface.prot.tlsoip.test;

import de.heuboe.tls.iface.prot.tlsoip.ConnectionConfig;
import de.heuboe.tls.iface.prot.tlsoip.ConnectionConfigParamList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

//@TestPropertySource(
//        properties = {
//                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//                "spring.kafka.consumer.group-id=SenderEmbeddedKafkaTest",
//                "spring.kafka.listener.missing-topics-fatal=true"}
//        locations="classpath:commStat1.properties"               // !!! properties
//        )

@Slf4j
@EnableConfigurationProperties
//@SpringJUnitConfig

@SpringBootTest//(
//        properties = {
//                //"de.heuboe.tls.sender.config.forceAlive=true" // wins over property from file
//        }
//        )

@ContextConfiguration( classes = {
        de.heuboe.tls.iface.prot.tlsoip.test.config.Config.class
        } 
)

@EnableAutoConfiguration
class ParamListTest {
    
    @Test
    void thePLTest() {
        ConnectionConfigParamList pl = new ConnectionConfigParamList();
        ConnectionConfigParamList pl2 = pl.getDefault();
        String s1;
        
        s1 = pl2.getValue( "CLieNT" );
        assertEquals( "true", s1 );
        
        s1 = pl2.getValue( "SECurecONnectiON" );
        assertEquals( "false", s1 );
        
        s1 = pl2.getValue( "cliEnt_aUThenTIcaTION" );
        assertEquals( "false", s1 );


        s1 = pl2.getValue( "CONnEcT_DelaY" );
        assertEquals( "0", s1 );
        
        s1 = pl2.getValue( "CONnEcT_DuraTIon" );
        assertEquals( "0", s1 );
       

        s1 = pl2.getValue( "HellO_DElAY" );
        assertEquals( "10", s1 );
        
        s1 = pl2.getValue( "HellOTimeOUT" );
        assertEquals( "30", s1 );
        
        
        s1 = pl2.getValue( "RECeipt_Count" );
        assertEquals( "10", s1 );
        
        s1 = pl2.getValue( "receiptDELAY" );
        assertEquals( "10", s1 );
        
        s1 = pl2.getValue( "receiptTimeout" );
        assertEquals( "30", s1 );
        
        s1 = pl2.getValue( "receipt_gRACE_time" );
        assertEquals( "0", s1 );
        
        
        s1 = pl2.getValue( "reconnectdelay" );
        assertEquals( "60", s1 );
        
        s1 = pl2.getValue( "LOGfile_SIZE" );
        assertEquals( "100000", s1 );
        
        s1 = pl2.getValue( "logfilerotate" );
        assertEquals( "3", s1 );
        
        s1 = pl2.getValue( "logfile_rot_ate" );
        assertEquals( null, s1 );
        
        
        ConnectionConfig cc = pl2.getAsConnectionConfig();
        assertNotEquals( null, cc.getLogFileSize() );

        Integer in = cc.getLogFileSize();
        assertEquals( 100000, in );
    }

}
