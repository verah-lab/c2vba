package de.heuboe.asfinag.vmis2.synchronize.vd;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.statemachinesystems.mockclock.MockClock;

import de.heuboe.asfinag.vmis2.synchronize.vd.schedule.ScheduleUtils;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.HbKafkaUtils;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLane;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLanes;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;


public class BasicTest {
    
    private static final MockClock eventClock    = MockClock.at(2019, 7, 30, 8, 0, 0, ZoneId.systemDefault());
    private static final MockClock procClock     = MockClock.at(2019, 7, 30, 8, 0, 10, ZoneId.systemDefault());
    
    @Test
    public void testData() throws JsonGenerationException, JsonMappingException, IOException {
        
        List<PShortTermCollectedDataLane> list = new ArrayList<>();
        String id = "A23_1";
        list.add(new PShortTermCollectedDataLane(
                Instant.now(eventClock), Instant.now(procClock), "2038735",
                7, 3, 90, 60, 3f, 50, 20, 60,
                15, PTlsDataVersion.VERSION_4, true, false, false,
                Instant.now(eventClock), 81, 1, 100));  //slow vehicle
        
        PShortTermCollectedDataLanes data = new PShortTermCollectedDataLanes(list, id);
        
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        
        File f = new File("src/test/resources/testData/PShortTermCollectedDataLanes.json");
        if(!f.exists()) {
            f.createNewFile();
        }
        objectMapper.writeValue(f, data);
    }
    
    @Test
    public void testTimer() {
        List<Integer> parts = ScheduleUtils.getCronExpParts(59);
        assertTrue(parts.size() == 1);
        parts = ScheduleUtils.getCronExpParts(60);
        assertTrue(parts.size() == 2);
        parts = ScheduleUtils.getCronExpParts(3601);
        assertTrue(parts.size() == 3);
        parts = ScheduleUtils.getCronExpParts(216001);
        assertTrue(parts.size() == 3);
    }
    
    @Test
    public void testStringList() {
        List<Integer> ils = new ArrayList<>();
        ils.add(15);
        ils.add(30);
        ils.add(60);
        String txt = ScheduleUtils.getListString(ils);
        for(Integer il : ils) {
            assertTrue(txt.contains(String.valueOf(il)));
        }
    }
    
    @Test
    public void testKafkaUtils() {
        String encoded = HbKafkaUtils.encodeTopicName("A22.0.45 (A23)");
        assertTrue("A22.0.45_.__--A23--_".equals(encoded));
        
        String decoded = HbKafkaUtils.decodeTopicName("A22.0.45_.__--A23--_");
        assertTrue("A22.0.45 (A23)".equals(decoded));
    }

}
