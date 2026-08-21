package de.heuboe.tls.receiver.core.receiver;

import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.rdr.impl.DataObject;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;
import de.heuboe.tls.receiver.rdr.item.TimeItem;
import de.heuboe.tls.tlstele.meta.TlsDatatypeId;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverTimeOffsetTest {

    /**
     * <b>Objective:</b> Verify positive time shift<p>
     * <b>Description:</b> Apply a positive global time offset to a DataObject containing a TimeItem<p>
     * <b>Result:</b> TimeItem is shifted forward by the configured seconds<p>
     * <b>Precondition:</b> Receiver with positive global timeoffset<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void applyTimeOffset_shiftsTimeItemByConfiguredSeconds() {
        // Initialize receiver with positive offset
        Receiver receiver = new Receiver();
        receiver.timeoffset = 90; // +90 seconds
        receiver.timeoffsetMap = new HashMap<>();

        // Create base TimeItem
        long baseMillis = 1_700_000_000_000L; // fixed epoch for test stability
        Date base = new Date(baseMillis);
        TimeItem ti = new TimeItem("testTime", base, 7);
        
        // Setup DataObject with metadata
        DataObject dataObject = new DataObject( "LVEirgendwas", "permIdOfDE" );
        dataObject.getItems().add( ti );
        
        dataObject.setEtelMeta( new DataObject.ETelMeta( 1, 132, 1, 2) );
        dataObject.setDeMeta( new DataObject.DeMeta( 13, 207, 1, null ) );
        
        List<DataObjectIf> in = Arrays.asList( dataObject );
        
        // Execute time offset application
        List<DataObjectIf> out = receiver.applyTimeOffset(in);

        // Verify result
        assertEquals(1, out.size(), "Exactly one element expected");
        assertTrue( out.get( 0 ).getItems().get( 0 ) instanceof TimeItem, "Output should still be a TimeItem");
        TimeItem shifted = (TimeItem) out.get( 0 ).getItems().get( 0 );

        assertEquals(ti.getName(), shifted.getName(), "Name must be preserved");
        assertEquals(ti.getConsumedSize(), shifted.getConsumedSize(), "Consumed size must be preserved");
        assertEquals(baseMillis + 90_000L, shifted.getAsDate().getTime(), "Time must be shifted by 90s (in ms)");
    }

    /**
     * <b>Objective:</b> Verify negative time shift<p>
     * <b>Description:</b> Apply a negative global time offset to a DataObject containing a TimeItem<p>
     * <b>Result:</b> TimeItem is shifted backward by the configured seconds<p>
     * <b>Precondition:</b> Receiver with negative global timeoffset<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void applyTimeOffset_handlesNegativeOffset() {
        // Initialize receiver with negative offset
        Receiver receiver = new Receiver();
        receiver.timeoffset = -30; // -30 seconds
        receiver.timeoffsetMap = new HashMap<>();

        // Create base TimeItem
        long baseMillis = 1_700_000_100_000L;
        Date base = new Date(baseMillis);
        TimeItem ti = new TimeItem("negTime", base, 3);
        
        // Setup DataObject
        DataObject dataObject = new DataObject( "LVEirgendwas", "permIdOfDE" );
        dataObject.getItems().add( ti );
        
        dataObject.setEtelMeta( new DataObject.ETelMeta( 1, 132, 1, 2) );
        dataObject.setDeMeta( new DataObject.DeMeta( 13, 207, 1, null ) );
        
        List<DataObjectIf> in = Arrays.asList( dataObject );

        // Execute shift
        List<DataObjectIf> out = receiver.applyTimeOffset( in );

        // Verify backward shift
        TimeItem shifted = (TimeItem) out.get(0).getItems().get(0);
        assertEquals(baseMillis - 30_000L, shifted.getAsDate().getTime(), "Time must be shifted by -30s (in ms)");
    }

    /**
     * <b>Objective:</b> Verify non-time items are ignored<p>
     * <b>Description:</b> Apply time offset to a DataObject containing both TimeItem and IntegerItem<p>
     * <b>Result:</b> Only TimeItem is shifted, IntegerItem remains unchanged<p>
     * <b>Precondition:</b> Receiver with global timeoffset<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void applyTimeOffset_passesThroughNonTimeObjectsUnchanged() {
        // Setup receiver
        Receiver receiver = new Receiver();
        receiver.timeoffset = 10; // any value
        receiver.timeoffsetMap = new HashMap<>();
        
        // Create mixed items
        IntegerItem other = new IntegerItem( "Integer item", 123456789L, 4 );
        
        long baseMillis = 1_700_000_100_000L;
        Date base = new Date(baseMillis);
        TimeItem ti = new TimeItem("someTime", base, 3);
        
        // Setup DataObject with both items
        DataObject dataObject = new DataObject( "LVEirgendwas", "permIdOfDE" );
        dataObject.getItems().add( other );
        dataObject.getItems().add( ti );
        
        dataObject.setEtelMeta( new DataObject.ETelMeta( 1, 132, 1, 2) );
        dataObject.setDeMeta( new DataObject.DeMeta( 13, 207, 1, null ) );
        
        List<DataObjectIf> in = Arrays.asList( dataObject );

        // Execute shift
        List<DataObjectIf> out = receiver.applyTimeOffset( in );
        
        // Verify items
        DataObject dataObjectOut = (DataObject) out.get(0);

        assertEquals(2, dataObjectOut.getItems().size(), "Exactly two elements expected");
        IntegerItem outItem = (IntegerItem) dataObjectOut.getItems().get(0);

        assertSame(other, outItem, "Non-TimeItem must be passed through unchanged");
        
        TimeItem shifted = (TimeItem) dataObjectOut.getItems().get(1);
        assertEquals(baseMillis + 10_000L, shifted.getAsDate().getTime(), "Time must be shifted by -10s (in ms)");
    }

    /**
     * <b>Objective:</b> Verify map-based time shift<p>
     * <b>Description:</b> Apply time offset using a specific entry in timeoffsetMap for the given datatype<p>
     * <b>Result:</b> TimeItem is shifted by the offset defined in the map, ignoring the global default<p>
     * <b>Precondition:</b> Receiver with global timeoffset and specific map entry<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void applyTimeOffset_usesMapOffsetWhenAvailable() {
        // Setup receiver with map and global offset
        Receiver receiver = new Receiver();
        receiver.timeoffset = 10; // global default
        Map<TlsDatatypeId, Integer> map = new HashMap<>();
        TlsDatatypeId typeId = new TlsDatatypeId((short) 1, (short) 132, (short) 207);
        map.put(typeId, 500); // 500 seconds for this specific type
        receiver.timeoffsetMap = map;

        // Setup DataObject matching map entry
        long baseMillis = 1_700_000_000_000L;
        Date base = new Date(baseMillis);
        TimeItem ti = new TimeItem("mapTime", base, 7);

        DataObject dataObject = new DataObject("LVEirgendwas", "permIdOfDE");
        dataObject.getItems().add(ti);
        dataObject.setEtelMeta(new DataObject.ETelMeta(1, 132, 1, 2));
        dataObject.setDeMeta(new DataObject.DeMeta(13, 207, 1, null));
        // TlsDatatypeId(fg, id, typ) -> (1, 132, 207)

        List<DataObjectIf> in = Arrays.asList(dataObject);
        // Execute shift
        List<DataObjectIf> out = receiver.applyTimeOffset(in);

        // Verify map offset was used
        TimeItem shifted = (TimeItem) out.get(0).getItems().get(0);
        assertEquals(baseMillis + 500_000L, shifted.getAsDate().getTime(), "Time must be shifted by 500s from map");
    }

    /**
     * <b>Objective:</b> Verify fallback to global offset<p>
     * <b>Description:</b> Apply time offset when timeoffsetMap exists but lacks an entry for the specific datatype<p>
     * <b>Result:</b> TimeItem is shifted by the global default offset<p>
     * <b>Precondition:</b> Receiver with global offset and map missing relevant entry<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void applyTimeOffset_fallsBackToGlobalWhenMapEntryMissing() {
        // Setup receiver with non-matching map entry
        Receiver receiver = new Receiver();
        receiver.timeoffset = 100; // global default
        Map<TlsDatatypeId, Integer> map = new HashMap<>();
        TlsDatatypeId otherTypeId = new TlsDatatypeId((short) 9, (short) 999, (short) 99);
        map.put(otherTypeId, 500);
        receiver.timeoffsetMap = map;

        // Setup DataObject
        long baseMillis = 1_700_000_000_000L;
        Date base = new Date(baseMillis);
        TimeItem ti = new TimeItem("fallbackTime", base, 7);

        DataObject dataObject = new DataObject("LVEirgendwas", "permIdOfDE");
        dataObject.getItems().add(ti);
        dataObject.setEtelMeta(new DataObject.ETelMeta(1, 132, 1, 2));
        dataObject.setDeMeta(new DataObject.DeMeta(13, 207, 1, null));
        // TlsDatatypeId is (1, 132, 207)

        List<DataObjectIf> in = Arrays.asList(dataObject);
        // Execute shift
        List<DataObjectIf> out = receiver.applyTimeOffset(in);

        // Verify fallback to global offset
        TimeItem shifted = (TimeItem) out.get(0).getItems().get(0);
        assertEquals(baseMillis + 100_000L, shifted.getAsDate().getTime(), "Time must be shifted by 100s global default");
    }

    /**
     * <b>Objective:</b> Verify multiple offsets in one batch<p>
     * <b>Description:</b> Apply time offsets to multiple objects with different datatypes in a single call<p>
     * <b>Result:</b> Each object is shifted correctly according to its specific map entry or global fallback<p>
     * <b>Precondition:</b> Receiver with multiple map entries and global fallback<p>
     * <b>Requirements:</b> <p>
     */
    @Test
    void applyTimeOffset_multipleObjectsDifferentOffsets() {
        // Setup receiver with multiple specific offsets
        Receiver receiver = new Receiver();
        receiver.timeoffset = 10;
        Map<TlsDatatypeId, Integer> map = new HashMap<>();
        TlsDatatypeId type1 = new TlsDatatypeId((short) 1, (short) 132, (short) 207);
        TlsDatatypeId type2 = new TlsDatatypeId((short) 1, (short) 133, (short) 207);
        map.put(type1, 60);
        map.put(type2, -60);
        receiver.timeoffsetMap = map;

        long baseMillis = 1_700_000_000_000L;
        Date base = new Date(baseMillis);

        // Object 1: uses map (+60)
        DataObject obj1 = new DataObject("obj1", "p1");
        obj1.getItems().add(new TimeItem("t1", base, 7));
        obj1.setEtelMeta(new DataObject.ETelMeta(1, 132, 1, 2));
        obj1.setDeMeta(new DataObject.DeMeta(13, 207, 1, null));

        // Object 2: uses map (-60)
        DataObject obj2 = new DataObject("obj2", "p2");
        obj2.getItems().add(new TimeItem("t2", base, 7));
        obj2.setEtelMeta(new DataObject.ETelMeta(1, 133, 1, 2));
        obj2.setDeMeta(new DataObject.DeMeta(13, 207, 1, null));

        // Object 3: uses global fallback (+10)
        DataObject obj3 = new DataObject("obj3", "p3");
        obj3.getItems().add(new TimeItem("t3", base, 7));
        obj3.setEtelMeta(new DataObject.ETelMeta(1, 134, 1, 2));
        obj3.setDeMeta(new DataObject.DeMeta(13, 207, 1, null));

        List<DataObjectIf> in = Arrays.asList(obj1, obj2, obj3);
        // Execute shift for all objects
        List<DataObjectIf> out = receiver.applyTimeOffset(in);

        // Verify individual shifts
        assertEquals(baseMillis + 60_000L, ((TimeItem)out.get(0).getItems().get(0)).getAsDate().getTime());
        assertEquals(baseMillis - 60_000L, ((TimeItem)out.get(1).getItems().get(0)).getAsDate().getTime());
        assertEquals(baseMillis + 10_000L, ((TimeItem)out.get(2).getItems().get(0)).getAsDate().getTime());
    }
}
