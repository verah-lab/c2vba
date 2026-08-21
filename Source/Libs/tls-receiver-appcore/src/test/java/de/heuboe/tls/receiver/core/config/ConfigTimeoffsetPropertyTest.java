package de.heuboe.tls.receiver.core.config;

import de.heuboe.tls.tlstele.meta.TlsDatatypeId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <b>Objective:</b> Verify property injection in ConfigTimeoffset<p>
 * <b>Description:</b> Tests that Spring correctly injects properties into the ConfigTimeoffset beans.<p>
 * <b>Result:</b> Properties are correctly injected and beans return expected values.<p>
 */
@SpringBootTest
@ContextConfiguration(classes = {ConfigTimeoffset.class})
@TestPropertySource(properties = {
        "de.heuboe.asfinag.tls.receiver.timeoffset=120",
        "de.heuboe.asfinag.tls.receiver.timeoffset-map=4/131/120!90, 4/133/120!-30"
})
class ConfigTimeoffsetPropertyTest {

    @Autowired
    @Qualifier("timeoffset")
    private Integer timeoffset;

    @Autowired
    @Qualifier("timeoffsetmap")
    private Map<TlsDatatypeId, Integer> timeoffsetmap;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ConfigTimeoffset.class);

    /**
     * <b>Objective:</b> Verify injection of timeoffset property<p>
     * <b>Description:</b> Checks if the "timeoffset" bean has the value from properties.<p>
     * <b>Result:</b> Matches the configured property value (120).<p>
     * <b>Precondition:</b> Property de.heuboe.asfinag.tls.receiver.timeoffset set to 120<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void testTimeoffsetInjection() {
        assertEquals(120, timeoffset, "The timeoffset bean should be injected with the value from properties");
    }

    /**
     * <b>Objective:</b> Verify injection of timeoffset-map property<p>
     * <b>Description:</b> Checks if the "timeoffsetmap" bean is correctly parsed from properties.<p>
     * <b>Result:</b> Matches the configured property mappings.<p>
     * <b>Precondition:</b> Property de.heuboe.asfinag.tls.receiver.timeoffset-map set<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void testTimeoffsetmapInjection() {
        assertNotNull(timeoffsetmap);
        assertEquals(2, timeoffsetmap.size());
        
        TlsDatatypeId id1 = new TlsDatatypeId((short) 4, (short) 131, (short) 120);
        assertEquals(90, timeoffsetmap.get(id1));
        
        TlsDatatypeId id2 = new TlsDatatypeId((short) 4, (short) 133, (short) 120);
        assertEquals(-30, timeoffsetmap.get(id2));
    }

    /**
     * <b>Objective:</b> Verify default value when timeoffset property is absent<p>
     * <b>Description:</b> Test getTimeoffset return value without property<p>
     * <b>Result:</b> Returns default value 0<p>
     * <b>Precondition:</b> Property de.heuboe.asfinag.tls.receiver.timeoffset is absent<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void testTimeoffsetAbsence() {
        contextRunner.run(context -> {
            // Check if the bean "timeoffset" is present
            assertThat(context).hasBean("timeoffset");
            // Get the value of the bean
            Integer value = context.getBean("timeoffset", Integer.class);
            // Verify that the value is 0 (the default value specified in @Value)
            assertThat(value).isZero();
        });
    }

    /**
     * <b>Objective:</b> Verify default value when timeoffset-map property is absent<p>
     * <b>Description:</b> Test getTimeoffsetmap return value without property<p>
     * <b>Result:</b> Returns empty map<p>
     * <b>Precondition:</b> Property de.heuboe.asfinag.tls.receiver.timeoffset-map is absent<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void testTimeoffsetmapAbsence() {
        contextRunner.run(context -> {
            // Check if the bean "timeoffsetmap" is present
            assertThat(context).hasBean("timeoffsetmap");
            // Get the value of the bean
            Map<?, ?> map = context.getBean("timeoffsetmap", Map.class);
            // Verify that the map is empty (the default value specified in @Value is empty string, which parses to empty map)
            assertThat(map).isEmpty();
        });
    }
}
