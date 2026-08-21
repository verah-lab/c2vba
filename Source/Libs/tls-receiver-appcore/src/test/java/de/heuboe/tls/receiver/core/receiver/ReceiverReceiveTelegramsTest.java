package de.heuboe.tls.receiver.core.receiver;

import de.heuboe.tls.receiver.core.telein.TlsKafkaTelgramReceiver;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.interfaces.DataWriter;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.receiver.rdr.impl.DataObject;
import de.heuboe.tls.receiver.rdr.impl.DataObject.ETelMeta;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;
import de.heuboe.tls.tlstele.TlsTele;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Tests the receive flow inside the Receiver's worker Runnable by mocking all
 * autowired collaborators except the primitive timeoffset.
 */
class ReceiverReceiveTelegramsTest {

    /**
     * <b>Objective:</b> Verify processing of transformed objects.<p>
     * <b>Description:</b> Tests if the receiver correctly processes telegrams through the transformer and writes results via dataWriter.<p>
     * <b>Result:</b> Telegrams are transformed and written successfully.<p>
     * <b>Precondition:</b> Valid telegrams are available from the receiver.<p>
     * <b>Requirements:</b> 
     */
    @Test
    void receiveTelegrams_processesTransformedObjects_andWritesThem() throws Exception {
        // Arrange
        Receiver receiver = new Receiver();
        setField(receiver, "timeoffset", 0); // do not mock timeoffset
        setField(receiver, "timeoffsetMap", new HashMap<>() );

        // mocks for autowired members
        TlsKafkaTelgramReceiver teleReceiverK = mock(TlsKafkaTelgramReceiver.class);
        Transformer transformer = mock(Transformer.class);
        DataWriter dataWriter = mock(DataWriter.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        SystemMessageManagement smm = mock(SystemMessageManagement.class);

        // Provide a mock counter directly on the receiver to avoid using the builder
        Counter mockCounter = mock(Counter.class);

        // Provide a telegram coming from receiver and a transformed object list
        TlsTele anyTele = mock(TlsTele.class);
        when(teleReceiverK.receive()).thenReturn(List.of(anyTele)).thenReturn(List.of());
        
        IntegerItem obj1 = new IntegerItem("int", 1L, 4);
        IntegerItem obj2 = new IntegerItem("int2", 2L, 4);
        
        DataObject dataObject = new DataObject("LVEirgendwas", "permIdOfDE");
        dataObject.getItems().add( obj1 );
        dataObject.getItems().add( obj2 );
        
        when(transformer.transform(anyTele)).thenReturn(List.of(dataObject));

        // Inject mocks and simple values
        setField(receiver, "teleReceiverK", teleReceiverK);
        setField(receiver, "teleReceiver", teleReceiverK);
        setField(receiver, "transformer", transformer);
        setField(receiver, "dataWriter", dataWriter);
        setField(receiver, "meterRegistry", meterRegistry);
        setField(receiver, "smm", smm);
        setField(receiver, "uzId", "testUz");
        setField(receiver, "counter", mockCounter);

        // Act: directly call the now package-private receiveTelegrams
        // <b>Objective:</b> Execute the telegram receiving and processing flow.<p>
        receiver.receiveTelegrams();

        // Assert interactions
        // <b>Objective:</b> Verify that all components were interacted with correctly.<p>
        verify(teleReceiverK, atLeastOnce()).receive();
        verify(transformer).transform(anyTele);
        verify(dataWriter).beginEtel();
        verify(dataWriter, times(1)).write(any(DataObjectIf.class));
        verify(dataWriter).endEtel();
        verify(mockCounter).increment(1.0); // 1 telegram in the batch
        verifyNoInteractions(smm); // no errors/messages expected in this happy path
    }

    /**
     * <b>Objective:</b> Verify general time offset application on TimeItems.<p>
     * <b>Description:</b> Checks if a configured time offset is correctly applied to TimeItems within transformed DataObjects.<p>
     * <b>Result:</b> TimeItems are shifted by the configured offset value.<p>
     * <b>Precondition:</b> Transformer returns DataObject containing TimeItems and a time offset is configured.<p>
     * <b>Requirements:</b> 
     */
    @Test
    void receiveTelegrams_appliesTimeOffset_whenTransformerReturnsListWithTimeItem() throws Exception {
        // Arrange
        Receiver receiver = new Receiver();
        // set a positive timeoffset to verify shifting is applied inside receiveTelegrams flow
        setField(receiver, "timeoffset", 10); // +10 seconds
        setField(receiver, "timeoffsetMap", new HashMap<>() );

        TlsKafkaTelgramReceiver teleReceiverK = mock(TlsKafkaTelgramReceiver.class);
        Transformer transformer = mock(Transformer.class);
        DataWriter dataWriter = mock(DataWriter.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        SystemMessageManagement smm = mock(SystemMessageManagement.class);

        Counter mockCounter = mock(Counter.class);

        // one telegram available
        TlsTele anyTele = mock(TlsTele.class);
        when(teleReceiverK.receive()).thenReturn(List.of(anyTele)).thenReturn(List.of());

        // transformer returns a list that contains a non-time object and a TimeItem
        IntegerItem nonTime = new IntegerItem("int", 123L, 4);
        long baseMillis = 1_700_000_000_000L;
        de.heuboe.tls.receiver.rdr.item.TimeItem timeItem = new de.heuboe.tls.receiver.rdr.item.TimeItem(
                "time", new java.util.Date(baseMillis), 7);
        
        DataObject dataObject = new DataObject( "LVEirgendwas", "permIdOfDE" );
        dataObject.getItems().add( nonTime );
        dataObject.getItems().add( timeItem );
        dataObject.getItemMap().put( timeItem.getName(), timeItem );
        dataObject.getItemMap().put( nonTime.getName(), nonTime );
        
        dataObject.setEtelMeta( new ETelMeta( 1, 132, 1, 2) );
        dataObject.setDeMeta( new DataObject.DeMeta( 13, 207, 1, null ) );
        
        when(transformer.transform(anyTele)).thenReturn(List.of(dataObject));

        // inject collaborators
        setField(receiver, "teleReceiverK", teleReceiverK);
        setField(receiver, "teleReceiver", teleReceiverK);
        setField(receiver, "transformer", transformer);
        setField(receiver, "dataWriter", dataWriter);
        setField(receiver, "meterRegistry", meterRegistry);
        setField(receiver, "smm", smm);
        setField(receiver, "uzId", "testUz");
        setField(receiver, "counter", mockCounter);

        // Act
        // <b>Objective:</b> Execute receiving flow with time offset.<p>
        receiver.receiveTelegrams();

        // Assert: two writes, first passes through original non-time object instance, second is a shifted TimeItem
        // <b>Objective:</b> Verify that time items are correctly shifted and others are not.<p>
        var captor = org.mockito.ArgumentCaptor.forClass(DataObjectIf.class);
        verify(dataWriter, times(1)).write(captor.capture());
        List<DataObjectIf> written0 = captor.getAllValues();
        DataObject written = (DataObject) written0.get(0);

        // the first element should be the same instance (non-time objects are forwarded unchanged)
        // <b>Objective:</b> Ensure non-time items remain unchanged.<p>
        org.junit.jupiter.api.Assertions.assertSame(nonTime, written.getItems().get(0));

        // the second element should be a TimeItem with shifted time by +10 seconds
        // <b>Objective:</b> Ensure time items are shifted by the configured offset.<p>
        org.junit.jupiter.api.Assertions.assertTrue(written.getItems().get(1) instanceof de.heuboe.tls.receiver.rdr.item.TimeItem);
        de.heuboe.tls.receiver.rdr.item.TimeItem shifted =
                (de.heuboe.tls.receiver.rdr.item.TimeItem) written.getItems().get(1);
        org.junit.jupiter.api.Assertions.assertEquals(baseMillis + 10_000L, shifted.getAsDate().getTime());

        // also ensure usual begin/end flow happened, and counter incremented for one telegram
        verify(dataWriter).beginEtel();
        verify(dataWriter).endEtel();
        verify(mockCounter).increment(1.0);
        verifyNoInteractions(smm);
    }

    /**
     * <b>Objective:</b> Verify handling of null transformation results.<p>
     * <b>Description:</b> Tests if the receiver correctly logs and notifies when the transformer returns null for a telegram.<p>
     * <b>Result:</b> Warning message is sent and no data is written.<p>
     * <b>Precondition:</b> Transformer is configured to return null for a received telegram.<p>
     * <b>Requirements:</b> 
     */
    @Test
    void receiveTelegrams_logsAndNotifies_whenTransformReturnsNull() throws Exception {
        // Arrange
        Receiver receiver = new Receiver();
        setField(receiver, "timeoffset", 0);
        setField(receiver, "timeoffsetMap", new HashMap<>() );

        TlsKafkaTelgramReceiver teleReceiverK = mock(TlsKafkaTelgramReceiver.class);
        Transformer transformer = mock(Transformer.class);
        DataWriter dataWriter = mock(DataWriter.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        SystemMessageManagement smm = mock(SystemMessageManagement.class);

        Counter mockCounter = mock(Counter.class);

        TlsTele anyTele = mock(TlsTele.class);
        when(teleReceiverK.receive()).thenReturn(List.of(anyTele)).thenReturn(List.of());

        // transform yields null to trigger warning path
        when(transformer.transform(anyTele)).thenReturn(null);

        setField(receiver, "teleReceiverK", teleReceiverK);
        setField(receiver, "teleReceiver", teleReceiverK);
        setField(receiver, "transformer", transformer);
        setField(receiver, "dataWriter", dataWriter);
        setField(receiver, "meterRegistry", meterRegistry);
        setField(receiver, "smm", smm);
        setField(receiver, "uzId", "testUz");
        setField(receiver, "counter", mockCounter);

        // Act
        // <b>Objective:</b> Execute receiving flow where transformation returns null.<p>
        receiver.receiveTelegrams();

        // Assert: beginEtel is called before discovering null, but no writes/end
        // <b>Objective:</b> Verify that no data is written and a system message is sent.<p>
        verify(dataWriter, atLeastOnce()).beginEtel();
        verify(dataWriter, never()).write(any());
        verify(dataWriter, never()).endEtel();
        // a message should be sent once
        verify(smm, atLeastOnce()).sendMessage(contains("Telegram transformed to null"));
    }

    // ---- helpers ----
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
