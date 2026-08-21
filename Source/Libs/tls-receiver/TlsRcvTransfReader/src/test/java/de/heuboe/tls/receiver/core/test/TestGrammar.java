package de.heuboe.tls.receiver.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.heuboe.tls.receiver.interfaces.DeBlockDefinitionIf;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;
import de.heuboe.tls.receiver.rdr.impl.TransformationReaderImpl;

public class TestGrammar {
    
    private static class HelpMsg implements SystemMessageManagement {

        @Override
        public void sendMessage( String message ) {
            System.out.println( "SystemMessage: " + message );
        }

        @Override
        public void sendMessage( String message, String objectId ) {
            System.out.println( "SystemMessage: " + message + " objectId: " + objectId );
        }
        
    }
    
    private static HelpMsg msgManagement;

        @BeforeAll
        public static void setUp() throws Exception {
            msgManagement = new HelpMsg();
        }

        @Test
        public void gram1Test() throws IOException {
                TransformationReader rdr = new TransformationReaderImpl();

                TransformationRulesContainer transformationRules = rdr.createTransformationRules( new File( "src/test/resources/rcv-test.txt" ), msgManagement );
                List<DeBlockDefinitionIf> chk = transformationRules.getDefinition( 1, 129, 1 );
                assertTrue(chk.size() > 0);
                DeBlockDefinitionIf deblockGetter = chk.get( 0 );
                for (GetterRule r : deblockGetter.getGetterRules()) {
                        System.out.println( r.getName() + " intern " +  r.getType() + " target " + r.getTargetType() );
                        if (r.getName().equals( "fehlercode" )) {
                                assertEquals( "Duebbele", r.getTargetType() );
                        }
                        if (r.getName().equals( "hersteller" )) {
                                assertEquals( "", r.getTargetType() );
                        }
                }
                assertTrue(chk.size() > 0);
        }

        @Test
        public void gram2Test() throws IOException {
                TransformationReader rdr = new TransformationReaderImpl();

                // exception will be thrown and caught by the parser
                TransformationRulesContainer transformationRules = rdr.createTransformationRules( new File( "src/test/resources/rcv-test-mitFehler.txt" ), msgManagement );
                assertEquals( null, transformationRules );
                assertEquals( null, transformationRules );
        }

    @Test
    public void setTimeZoneTest() {
        int a = 0;
        TimeGetter.setTimeZone( "UTC" );
        a++;
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            TimeGetter.setTimeZone( "Willi" );
        } );

        TimeGetter.setTimeZone( "GMT+01:00" );
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            TimeGetter.setTimeZone( "GMT+1" );
        } );
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            TimeGetter.setTimeZone( "UTC+1" );                             // UTC+1 is no legal timezone!!!
        } );

        assertEquals( 1, a );
    }

        @Test
        public void gram3Test() throws IOException {
                TransformationReader rdr = new TransformationReaderImpl();

                TransformationRulesContainer transformationRules = rdr.createTransformationRules( new File( "src/test/resources/rcv-test-ohneTypeList.txt" ), msgManagement );
                List<DeBlockDefinitionIf> chk = transformationRules.getDefinition( 1, 129, 2 );
                assertTrue(chk.size() > 0);
                DeBlockDefinitionIf deblockGetter = chk.get( 0 );
                for (GetterRule r : deblockGetter.getGetterRules()) {
                        System.out.println( r.getName() + " intern " +  r.getType() + " target " + r.getTargetType() );
                        if (r.getName().equals( "fehlercode" )) {
                                assertEquals( "typUndefiniert", r.getTargetType() );
                        }
                        if (r.getName().equals( "hersteller" )) {
                                assertEquals( "", r.getTargetType() );
                        }
                        if (r.getName().equals( "grafik" )) {
                            assertEquals( "Binary", r.getTargetType() );
                        }
                }
                assertTrue(chk.size() > 0);
        }
}
