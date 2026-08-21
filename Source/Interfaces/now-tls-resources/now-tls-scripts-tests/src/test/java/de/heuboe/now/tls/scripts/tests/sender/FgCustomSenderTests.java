package de.heuboe.now.tls.scripts.tests.sender;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.heuboe.now.tls.scripts.tests.NowSenderTestTransformer;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sender.interfaces.Transformer.TypedDeBlock;
import de.heuboe.tls.sender.transf.impl.TransformerImpl;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.meta.TlsDatatypeId;
import eu.vmis_ehe.vmis2.tls.received.LVEBetriebsparameterAbstandswarnung;
import eu.vmis_ehe.vmis2.tls.received.LVEBetriebsparameterAbstandswarnungList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FgCustomSenderTests {

    private static TransformerImpl transformer;

    @BeforeAll
    public static void setUp() throws JsonParseException, JsonMappingException, IOException {
        transformer = new NowSenderTestTransformer();
    }

    @Test
    public void test_1_3_191_LVEBetriebsparameterAbstandswarnung() {

        LVEBetriebsparameterAbstandswarnung.Builder builder = LVEBetriebsparameterAbstandswarnung.newBuilder();
        builder.setId("AQ_A02_2_800_F1.A");
        builder.setAktiv(86); // LowByte
        builder.setNettozeitluecke(17); // LowByte
        LVEBetriebsparameterAbstandswarnung message = builder.build();

        LVEBetriebsparameterAbstandswarnungList.Builder listBuilder = LVEBetriebsparameterAbstandswarnungList.newBuilder();
        listBuilder.addElements(message);
        LVEBetriebsparameterAbstandswarnungList messageList = listBuilder.build();

        GenericProtoObject gpo = new GenericProtoObject("eu.vmis_ehe.vmis2.tls.received.LVEBetriebsparameterAbstandswarnungList", messageList.toByteArray(), null);
        Map<String, byte[]> kafkaHeader = new HashMap<>();

        List<TypedDeBlock> actual = transformer.transformMulti(gpo, "LVEBetriebsparameterAbstandswarnungSoll", kafkaHeader);

        TlsDatatypeId tlsDatatype = new TlsDatatypeId((short) 1, (short) 3, (short) 191);
        TlsDeBlock deBlock = new TlsDeBlock(null, 33, 191);
        byte[] content =
                {
                        86,
                        17
                };
        deBlock.setContent(content);

        List<TypedDeBlock> expected = new ArrayList<>();
        expected.add(new TypedDeBlock("AQ_A02_2_800_F1.A", 8431421, tlsDatatype, deBlock, 0, actual.get(0).getIntputData(), Collections.emptyMap()));

        assertEquals(expected, actual);
    }

}
