package de.heuboe.tls.receiver.core.config;

import de.heuboe.tls.tlstele.meta.TlsDatatypeId;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTimeoffsetTest {
    
    /**
     * <b>Objective:</b> Verify empty input handling<p>
     * <b>Description:</b> Parse empty string as time offset configuration<p>
     * <b>Result:</b> Returns empty map without errors<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_handlesEmptyInput() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        String input = "";
        // Parse empty input
        Map< TlsDatatypeId, Integer > result = config.getTimeoffsetmap( input );
        
        // Verify result is not null and empty
        assertNotNull( result );
        assertTrue( result.isEmpty(), "Result should be empty for empty input" );
    }
    
    /**
     * <b>Objective:</b> Verify duplicate ID handling<p>
     * <b>Description:</b> Parse input with duplicate TlsDatatypeId entries<p>
     * <b>Result:</b> Last occurrence offset is retained<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_handlesDuplicateIds() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        String input = "4/131/120!90, 4/131/120!-30";
        // Parse input with duplicate IDs
        Map< TlsDatatypeId, Integer > result = config.getTimeoffsetmap( input );
        
        // Verify single entry with last offset value
        TlsDatatypeId id = new TlsDatatypeId( (short) 4, (short) 131, (short) 120 );
        assertEquals( 1, result.size(), "Result should contain a single entry for a duplicate ID" );
        assertEquals( -30, result.get( id ), "Offset should reflect the last occurrence" );
    }
    
    /**
     * <b>Objective:</b> Verify mixed valid and invalid entry handling<p>
     * <b>Description:</b> Parse input containing both valid and invalid entries<p>
     * <b>Result:</b> IllegalArgumentException is thrown<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_handlesMixedValidAndInvalidEntries() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        String input = "4/131/120!90, invalid, 4/133/120!-30";
        
        // Verify exception is thrown for invalid entry
        assertThrows( IllegalArgumentException.class, () -> config.getTimeoffsetmap( input ) );
    }
    
    /**
     * <b>Objective:</b> Verify large offset value handling<p>
     * <b>Description:</b> Parse input with maximum and minimum integer offset values<p>
     * <b>Result:</b> Both extreme offset values are correctly parsed<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_handlesLargeOffsets() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        String input = "4/131/120!2147483647, 4/133/120!-2147483648";
        // Parse input with extreme offset values
        Map< TlsDatatypeId, Integer > result = config.getTimeoffsetmap( input );
        
        // Verify both entries are parsed correctly
        assertNotNull( result );
        assertEquals( 2, result.size(), "Result should include entries for both large offsets" );
        
        TlsDatatypeId id1 = new TlsDatatypeId( (short) 4, (short) 131, (short) 120 );
        assertEquals( 2147483647, result.get( id1 ) );
        
        TlsDatatypeId id2 = new TlsDatatypeId( (short) 4, (short) 133, (short) 120 );
        assertEquals( -2147483648, result.get( id2 ) );
    }
    
    /**
     * <b>Objective:</b> Verify valid string parsing<p>
     * <b>Description:</b> Parse well-formed time offset configuration string<p>
     * <b>Result:</b> Map with correct TlsDatatypeId and offset mappings<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_parsesValidString() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        String input = "4/131/120!90, 4/133/120!-30";
        // Parse valid input string
        Map< TlsDatatypeId, Integer > result = config.getTimeoffsetmap( input );
        
        // Verify map contains two entries
        assertNotNull( result );
        assertEquals( 2, result.size() );
        
        // Verify first ID and offset
        TlsDatatypeId id1 = new TlsDatatypeId( (short) 4, (short) 131, (short) 120 );
        assertEquals( 90, result.get( id1 ) );
        
        // Verify second ID and offset
        TlsDatatypeId id2 = new TlsDatatypeId( (short) 4, (short) 133, (short) 120 );
        assertEquals( -30, result.get( id2 ) );
    }
    
    /**
     * <b>Objective:</b> Verify whitespace tolerance<p>
     * <b>Description:</b> Parse input with extra whitespace characters<p>
     * <b>Result:</b> Whitespace is ignored and parsing succeeds<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_handlesWhitespace() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        String input = " 4 / 131 / 120 ! 90 , 4/133/120!-30 ";
        // Parse input with whitespace
        Map< TlsDatatypeId, Integer > result = config.getTimeoffsetmap( input );
        
        // Verify map contains two entries
        assertNotNull( result );
        assertEquals( 2, result.size() );
        
        // Verify first entry is parsed correctly
        TlsDatatypeId id1 = new TlsDatatypeId( (short) 4, (short) 131, (short) 120 );
        assertEquals( 90, result.get( id1 ) );
    }
    
    /**
     * <b>Objective:</b> Verify invalid format rejection<p>
     * <b>Description:</b> Parse malformed input strings<p>
     * <b>Result:</b> IllegalArgumentException is thrown for invalid formats<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffsetmap_throwsExceptionOnInvalidFormat() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        // Verify exception for completely invalid format
        assertThrows( IllegalArgumentException.class, () -> config.getTimeoffsetmap( "invalid" ) );
        // Verify exception for incomplete format
        assertThrows( IllegalArgumentException.class, () -> config.getTimeoffsetmap( "4/131!90" ) );
    }

    /**
     * <b>Objective:</b> Verify retrieval of configured time offset<p>
     * <b>Description:</b> Call getTimeoffset with a specific value<p>
     * <b>Result:</b> Returns the provided value<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffset_returnsConfiguredValue() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        int expectedValue = 120;
        // Retrieve configured value
        int result = config.getTimeoffset( expectedValue );
        
        // Verify result matches the input value
        assertEquals( expectedValue, result, "The time offset should match the configured value" );
    }

    /**
     * <b>Objective:</b> Verify retrieval of default time offset<p>
     * <b>Description:</b> Call getTimeoffset with the default value (0)<p>
     * <b>Result:</b> Returns 0<p>
     * <b>Precondition:</b> ConfigTimeoffset instance initialized<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void getTimeoffset_returnsDefaultValue() {
        // Create configuration instance
        ConfigTimeoffset config = new ConfigTimeoffset();
        int defaultValue = 0;
        // Retrieve default value
        int result = config.getTimeoffset( defaultValue );
        
        // Verify result matches the default value
        assertEquals( defaultValue, result, "The time offset should return the default value (0)" );
    }
}
