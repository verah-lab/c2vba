package de.heuboe.asfinag.vmis2.configservice.system;

import java.io.IOException;

import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceImplBase;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import eu.vmis_ehe.vmis2.configservice.GetItemsRequest;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestServer {

    private static final int SERVER_PORT = 8980;
    
    private final int port;
    private final Server server;

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // ConfigService dummy implementation
    private static class ConfigService extends ConfigServiceImplBase {

        @Override
        public void getItems( GetItemsRequest request, StreamObserver<GetItemsReply> responseObserver ) {
            log.info( "getUzs( {}, {})", request.getType(), request.getIdsList());
            responseObserver.onNext( GetItemsReply.newBuilder().build());
            responseObserver.onCompleted();
            log.info( "  done.");
        }
    }
    
    // ===============================================================================================================================
     // Main method.
    public static void main( String[] args ) throws Exception {
        
        TestServer server = new TestServer( SERVER_PORT );
        server.start();
        server.blockUntilShutdown();
    }    

    // Create a TestServer server listening on 'port'.
    public TestServer( int port ) throws IOException {
      this( ServerBuilder.forPort(port), port );
    }

    // Create a TestServer server using serverBuilder as a base
    public TestServer( ServerBuilder<?> serverBuilder, int port ) {
      this.port = port;
      server = serverBuilder.addService( new ConfigService())
          .build();
    }

    // Start serving requests.
    public void start() throws IOException {
        server.start();
        log.info( "Server started, listening on " + port );
        Runtime.getRuntime().addShutdownHook( new Thread(() -> {
            // Use stderr here since the logger may has been reset by its JVM shutdown hook.
            System.err.println( "*** shutting down gRPC server since JVM is shutting down" );
            TestServer.this.stop();
            System.err.println( "*** server shut down" );
        }));
    }

    // Stop serving requests and shutdown resources.
    public void stop() {
        if( server != null ) {
            server.shutdown();
        }
    }

    // Await termination on the main thread since the grpc library uses daemon threads.
    private void blockUntilShutdown() throws InterruptedException {
        if( server != null ) {
            server.awaitTermination();
        }
    }

}
