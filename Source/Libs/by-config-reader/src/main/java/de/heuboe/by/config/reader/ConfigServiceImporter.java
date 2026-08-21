package de.heuboe.by.config.reader;

import de.heuboe.asfinag.vmis2.version.notifier.data.Notification;
import de.heuboe.by.config.reader.tls.ConfigObject;
import de.heuboe.by.config.reader.tls.TlsWorld;
import de.heuboe.config.base.Types;
import eu.vmis_ehe.vmis2.configservice.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Class to export a configuration from the BD-ConfigService
 */
@Component
@Slf4j
public class ConfigServiceImporter {

    @GrpcClient("configService")
    private ConfigVersionServiceGrpc.ConfigVersionServiceStub stub;

    @GrpcClient("configService")
    private ConfigVersionServiceGrpc.ConfigVersionServiceBlockingStub blockingStub;

    @Value("${grpc.client.configService.address}")
    private String address;

    /**
     * exports a configuration from the BD-ConfigService
     *
     * @param config {@link TlsWorld}
     * @return the exported configuration as TlsWorld
     */
    public int importConfig(TlsWorld config) {
        log.info("ConfigService: " + address);
        //add fg- and de-number to the ea object
        List<Integer> errors = config.getVrz().getChildren().stream().map(child -> importConfig(config, child))
                .collect(Collectors.toList());
        config.getVrz().getChildren().stream().forEach(child -> printConfig(config, child));
        if (!errors.isEmpty()) {
            for (Integer error : errors) {
                if (error != 0) {
                    return error;
                }
            }
        }
        createEmptyManuals(config);
        activateVersion(config);
        return 0;
    }

    void printConfig(TlsWorld config, ConfigObject child) {
        try (BufferedWriter br = new BufferedWriter(new FileWriter("input.csv"))) {
            printVersionAndHeader(config.getVersion(), br);
            printOneRecord(config.getVrz(), br);
            printRecord(child, br);
            printWwwChains(config.getWwwChains(), br);
        } catch (IOException e) {
            log.error("Failed to print", e);
        }
    }

    void activateVersion(TlsWorld config) {
        List<VersionKey> versions = createVersionKeys(config);
        ActivateVersionRequest req = getActivateRequest(versions);
        log.info("activate: [{}] ", req.getVersion());
        Iterator<ActivateVersionResponse> iterator = blockingStub.activateVersion(req);
        while (iterator.hasNext()) {
            iterator.next();
        }
        CreateConfigVersionRequest createRequest = CreateConfigVersionRequest.newBuilder().setVersionId(config.getVersion())
                .addAllVersions(createVersionKeys(config)).build();
        Iterator<CreateConfigVersionResponse> configVersion = blockingStub.createConfigVersion(createRequest);
        while (configVersion.hasNext()) {
            configVersion.next();
        }
        ActivateVersionRequest activateRequest = getActivateRequest(config.getVersion());
        log.info("activate: [{}] ", activateRequest.getVersion());
        Iterator<ActivateVersionResponse> responseIterator = blockingStub.activateVersion(activateRequest);
        while (responseIterator.hasNext()) {
            responseIterator.next();
        }
    }

    private ActivateVersionRequest getActivateRequest(List<VersionKey> versions) {
        return ActivateVersionRequest.newBuilder()
                .setVersion(VersionHandle.newBuilder()
                        .setKeys(VersionKeys.newBuilder()
                                .addAllKeys(versions)
                                .build())
                        .build())
                .build();
    }

    private ActivateVersionRequest getActivateRequest(String version) {
        return ActivateVersionRequest.newBuilder()
                .setVersion(VersionHandle.newBuilder()
                        .setVersionId(version)
                        .build())
                .build();
    }

    private List<VersionKey> createVersionKeys(TlsWorld config) {
        return config.getVrz().getChildren().stream()
                .map(uz -> createVersionKeys(uz, config.getVersion()))
                .collect(Collectors.toList());
    }

    private VersionKey createVersionKeys(ConfigObject uz, String version) {
        return VersionKey.newBuilder().setUzId(uz.getId())
                .setBaseVersionId(version)
                .setManualVersionId("0")
                .build();
    }

    int importConfig(TlsWorld config, ConfigObject child) {
        StreamObserver<ImportBaseDataStreamRequest> request;
        ConfigServiceObserver obs;
        try {
            obs = new ConfigServiceObserver();
            request = stub.importBaseDataStream(obs);
        } catch (Exception e) {
            log.error("Failed to connect", e);
            return 13;
        }

        log.info("Import base: " + config.getVersion());
        try {
            writeVersionAndHeader(config.getVersion(), request);
            writeOneRecord(config.getVrz(), request);
            writeRecord(child, request);
            writeWwwChains(config.getWwwChains(), request);
            request.onCompleted();
        } catch (IllegalArgumentException e) {
            request.onError(e);
            log.error("Failed to send data ", e);
            return 14;
        }

        try {
            obs.finished.await();
        } catch (InterruptedException e) {
            log.error("CountDownLatch interrupted ", e);
            Thread.currentThread().interrupt();
            return 16;
        }
        if (obs.error != null) {
            log.error("ConfigService returned error: ", obs.error);
            return 15;
        }
        return 0;
    }

    private void printWwwChains(Map<String, ConfigObject> wwwChains, BufferedWriter br) throws IOException {
        for (ConfigObject chain : wwwChains.values()) {
            br.write(chain.toCsvString() + "\n");
        }
    }

    void writeWwwChains(java.util.Map<String, ConfigObject> wwwChains, StreamObserver<ImportBaseDataStreamRequest> request) {
        for (ConfigObject chain : wwwChains.values()) {
            request.onNext(ImportBaseDataStreamRequest.newBuilder().setBaseData(chain.toCsvString() + "\n").build());
        }
    }

    void writeOneRecord(ConfigObject obj, StreamObserver<ImportBaseDataStreamRequest> request) {
        ImportBaseDataStreamRequest value =
                ImportBaseDataStreamRequest.newBuilder().setBaseData(obj.toCsvString() + "\n").build();
        request.onNext(value);
    }

    void writeVersionAndHeader(String version, StreamObserver<ImportBaseDataStreamRequest> request) {
        request.onNext(ImportBaseDataStreamRequest.newBuilder().setBaseData(version + "\n").build());
        request.onNext(ImportBaseDataStreamRequest.newBuilder().setBaseData(TlsWorld.getHeaderString() + "\n").build());
    }

    void writeRecord(ConfigObject obj, StreamObserver<ImportBaseDataStreamRequest> request) {
        if (obj.getFg() == 4 && !obj.isCl() && (obj.getTlsRef() == null || obj.getTlsRef().isBlank())) {
            return;
        }
        ImportBaseDataStreamRequest value =
                ImportBaseDataStreamRequest.newBuilder().setBaseData(obj.toCsvString() + "\n").build();
        request.onNext(value);
        if (obj.getType() != Types.ConfigItemType.MQ && obj.getType() != Types.ConfigItemType.AQ) {
            obj.getChildren()
                    .forEach(child -> writeRecord(child, request));
        }
    }

    private void printOneRecord(ConfigObject obj, BufferedWriter br) throws IOException {
        br.write(obj.toCsvString() + "\n");
    }

    private void printVersionAndHeader(String version, BufferedWriter br) throws IOException {
        br.write(version + "\n");
        br.write(TlsWorld.getHeaderString() + "\n");
    }

    private void printRecord(ConfigObject obj, BufferedWriter br) throws IOException {
        if (obj.getFg() == 4 && !obj.isCl() && (obj.getTlsRef() == null || obj.getTlsRef().isBlank())) {
            return;
        }
        br.write(obj.toCsvString() + "\n");
        if (obj.getType() != Types.ConfigItemType.MQ && obj.getType() != Types.ConfigItemType.AQ) {
            for (ConfigObject child : obj.getChildren()) {
                printRecord(child, br);
            }
        }
    }

    void createEmptyManuals(TlsWorld config) {
        List<CreateManualDataRequest.ManVersionDesc> manVersions = config.getVrz().getChildren().stream().map(c -> CreateManualDataRequest.ManVersionDesc.newBuilder()
                .setNewVersion(createVersionKeys(c, config.getVersion()))
                .build()).collect(Collectors.toList());
        log.info("Create empty manuals: " + manVersions);
        CreateManualDataRequest request = CreateManualDataRequest.newBuilder().addAllVersions(manVersions).build();
        Iterator<CreateManualDataResponse> manualData = blockingStub.createManualData(request);
        while (manualData.hasNext()) {
            manualData.next();
        }
    }

    static class ConfigServiceObserver implements StreamObserver<ImportBaseDataResponse> {

        private final CountDownLatch finished = new CountDownLatch(1);
        private Throwable error;

        @Override
        public void onNext(ImportBaseDataResponse value) {
            if (!value.getWarningsList().isEmpty()) {
                List<String> collect = value.getWarningsList().stream()
                        .map(Notification::getMessage)
                        .filter(ConfigServiceObserver::messageFilter)
                        .collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    log.warn("ConfigService returned warnings:\n {}", String.join("\n",collect));
                }
            }
        }

        private static boolean messageFilter(String m) {
            return !m.contains("TLS_NO_IP") &&
                    !m.contains("Ungültige/fehlende Koordinaten") &&
                    !(m.contains("EQ") && m.contains("hat keinen Cluster"));
        }

        @Override
        public void onError(Throwable t) {
            log.error("Failed to transfer config: ", t);
            error = t;
            finished.countDown();
        }

        @Override
        public void onCompleted() {
            log.info("config-service completed import successful");
            finished.countDown();
        }
    }
}