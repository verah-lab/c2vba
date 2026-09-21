package de.heuboe.asfinag.vmis2.configservice.system;

import com.google.protobuf.Empty;
import eu.vmis_ehe.vmis2.configservice.*;
import eu.vmis_ehe.vmis2.configservice.ChildOpt.Mode;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simple integration test that opens a connection, then logs some data
 */
@Slf4j
public class TestClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) throws InterruptedException {

        ManagedChannel channel = null;
        try {
            log.info( "Connecting to config service at {}:{}", SERVER_HOST, SERVER_PORT );
            channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT).usePlaintext().build();
            ConfigServiceBlockingStub service = ConfigServiceGrpc.newBlockingStub(channel);

            ServiceVersion v = service.getServiceVersion(Empty.getDefaultInstance());
            log.info("Talking to service version {}", v.getServiceVersion());

            log.info("");
            log.info( "Reading all UZs " );
            List<CfgUz> uzs = service.getAllItems(GetAllItemsRequest.newBuilder().setType(ConfigItemType.UZ).build()).getUzs().getUzsList();
            log.info( "  got {}:", uzs.size());
            uzs.forEach( uz -> log.info( "  {} - {}", uz.getId(), uz.getName()));

            log.info("");
            String uzId = uzs.get(0).getId();
            log.info("Reading uz '{}' with some child ids", uzId);
            GetItemsRequest request = GetItemsRequest.newBuilder()
                    .setType( ConfigItemType.UZ )
                    .addIds(uzId)
                    .addChildOpts(ChildOpt.newBuilder()
                            .addChildType(ConfigItemType.MQ.name())
                            .addChildType(ConfigItemType.AQ.name())
                            .setMode(Mode.ID)
                            .build())
                    .build();
            CfgUz uz = service.getItems(request).getUzs().getUzsList().get(0);
            log.info("   Got {}, with {} MQs and {} AQs", uz.getName(), uz.getMqIds().getIdsList().size(), uz.getAqIds().getIdsList().size());

            log.info("");
            String aqId = uz.getAqIds().getIdsList().get(0);
            log.info("Reading aq '{}' with all its wzgs", aqId );
            request = GetItemsRequest.newBuilder()
                    .setType( ConfigItemType.AQ )
                    .addIds( aqId )
                    .addChildOpts(ChildOpt.newBuilder()
                            .setMode(Mode.CHILD)
                            .build())
                    .build();
            CfgAq aq = service.getItems(request).getAqs().getAqsList().get(0);
            log.info("   Got {}  ({}), with {} WZGs", aq.getName(), aq.getType().name(), aq.getWzgs().getWzgsList().size());
            aq.getWzgs().getWzgsList().forEach( wzg ->
                    log.info("      {}: ({}),  {}", wzg.getName(), wzg.getType().name(), wzg.getLanePos().getDescription()));

        } finally {
            channel.shutdown().awaitTermination( 5, TimeUnit.SECONDS );
        }
    }
}
