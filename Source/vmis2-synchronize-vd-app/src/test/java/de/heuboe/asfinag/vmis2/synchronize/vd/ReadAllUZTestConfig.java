package de.heuboe.asfinag.vmis2.synchronize.vd;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.google.protobuf.util.JsonFormat;

import de.heuboe.asfinag.vmis2.constants.KafkaConstants;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import eu.vmis_ehe.vmis2.configservice.ServiceVersion;
import eu.vmis_ehe.vmis2.configservice.pojo.PChildOpt;
import eu.vmis_ehe.vmis2.configservice.pojo.PConfigItemType;
import eu.vmis_ehe.vmis2.configservice.pojo.PGetAllItemsRequest;
import eu.vmis_ehe.vmis2.paramservice.ParameterSetList;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import jakarta.annotation.PostConstruct;

@TestConfiguration
class ReadAllUZTestConfig {
    @Autowired
    IDGenerator idGen;
    
    @Autowired
    protected AlgoParameterIdProperties paramIds;

    @Autowired
    private SynchronizeVdProperties appProperties;
    
    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;
    
    protected static final String DATA_BASE_PATH = "src/test/resources/testData/";
    
    @Mock private ConfigServiceGrpc.ConfigServiceBlockingStub configServiceBlockingStub;
    
    @Mock private SystemExit systemExit;

    @PostConstruct
    void init()  throws Exception  {
        MockitoAnnotations.openMocks(this);
        Path d = Path.of("src/test/resources/stubData");
        
        when(configServiceBlockingStub.getServiceVersion(any())).thenReturn(ServiceVersion
                                                                                .newBuilder()
                                                                                .setConfigVersion("dummy")
                                                                                .setInterfaceVersion("dummy")
                                                                                .setServiceVersion("dummy")
                                                                                .build());

        /**
         * **************** Create possible config responses ****************
         */
        String cfgVDE = Files.readString(d.resolve("cfgVDE.json"));
        GetItemsReply.Builder replyBuilder = GetItemsReply.newBuilder();
        JsonFormat.parser().merge(cfgVDE, replyBuilder);
        GetItemsReply replyVDE = replyBuilder.build();
        
        String cfgVDEUpdate = Files.readString(d.resolve("cfgVDE_WIE_PLA.json"));
        GetItemsReply.Builder replyBuilder3 = GetItemsReply.newBuilder();
        JsonFormat.parser().merge(cfgVDEUpdate, replyBuilder3);
        GetItemsReply replyVDEAllUZs = replyBuilder3.build();

        String cfgRSTVDE = Files.readString(d.resolve("cfgRSTVDE.json"));
        GetItemsReply.Builder replyRSTBuilder = GetItemsReply.newBuilder();
        JsonFormat.parser().merge(cfgRSTVDE, replyRSTBuilder);
        GetItemsReply replyRSTVDE = replyRSTBuilder.build();

        String cfgRSTVDEUpdate = Files.readString(d.resolve("cfgRSTVDE_WIE_PLA.json"));
        GetItemsReply.Builder rstUpdate = GetItemsReply.newBuilder();
        JsonFormat.parser().merge(cfgRSTVDEUpdate, rstUpdate);
        GetItemsReply replyRSTVDEAllUZs = rstUpdate.build();


        /**
         * **************** Response VDEs (get all UZs (no uzId filter)) *******************
         */        
        GetAllItemsRequest reqVDEAllUZs = PGetAllItemsRequest.to(PGetAllItemsRequest.builder()
                .type(PConfigItemType.VDE_SENSOR).build());       
        when(configServiceBlockingStub.getAllItems(reqVDEAllUZs)).thenAnswer(new Answer<GetItemsReply>() {
            @Override
            public GetItemsReply answer(InvocationOnMock invocation) throws Throwable {
            	// Return one UZ if update is set
                if (TestUtils.UpdateType.UPDATE.equals(TestUtils.updateType)) {
                    return replyVDE;
                } else { // Return two UZs
                    return replyVDEAllUZs;
                }
            }
        });
        
        /**
         * **************** Response RSTS (get all UZs (no uzId filter))*******************
         */
        PChildOpt childOpt = PChildOpt.builder()        // with childs VDE_SENSORs
                .childTypeList(Arrays.asList(PConfigItemType.VDE_SENSOR.name())).build();
        GetAllItemsRequest reqRSTsAllUZs = PGetAllItemsRequest.to(PGetAllItemsRequest.builder()
                .type(PConfigItemType.RST).childOptsList(Arrays.asList(childOpt)).build());
        when(configServiceBlockingStub.getAllItems(reqRSTsAllUZs)).thenAnswer(new Answer<GetItemsReply>() {
            @Override
            public GetItemsReply answer(InvocationOnMock invocation) throws Throwable {
            	// Return RSTs of one UZ if update is set
                if (TestUtils.UpdateType.UPDATE.equals(TestUtils.updateType)) {
                	return replyRSTVDE;
                } else {// Return RSTs of two UZ if update is set
                	return replyRSTVDEAllUZs;
                }
            }
        });   
        
        
        // Read synchronize algo parameter from file
        ParameterSetList.Builder algoParaBuilder = ParameterSetList.newBuilder();
        String json = new String(Files.readAllBytes(
                Paths.get(DATA_BASE_PATH + "Parameter/PParmeterSetList#ZeitsychronisationINSTANZ1.json")));
        JsonFormat.parser().merge(json, algoParaBuilder);
        PParameterSetList algoPara = PParameterSetList.from(algoParaBuilder.build());
        assertNotNull(algoPara);
        
        // Send algo parameter
        String topic = appProperties.getParameterSystemTopics().get(0)
                .replace("{systemWideShortcut}", appProperties.getSystemWideShortcut());
        kafkaTemplate
                .send(MessageBuilder.withPayload(algoPara)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .setHeader(KafkaHeaders.KEY, "Zeitsynchronisation-VRZ-INSTANZ1")
                        .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID,
                                paramIds.getTimeSyncDefSetId())
                        .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "VRZ")
                        .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE,
                                appProperties.getInstanceName())
                        .build())
                .get();
    }

    @Bean
    @Profile("test")
    ConfigServiceGrpc.ConfigServiceBlockingStub configServiceBlockingStub() {
        return configServiceBlockingStub;
    }
    
    @Bean
    @Profile("test")
    SystemExit systemExit() {
        return systemExit;
    }
}
