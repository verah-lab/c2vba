package de.heuboe.tls.tele.recorder.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A TCP server for streaming telegrams.
 */
@Slf4j
public class TeleRecorderTcpServer extends Thread {

    private final int port;
    private boolean isLegacy;

    @Getter
    private final Map<Integer, OutputStream> tcpStreams = new ConcurrentHashMap<>();

    /**
     * Constructor for creating the thread with the definition of the port for the TCP server.
     *
     * @param port     The port the TCP server should stream on.
     * @param isLegacy A flag that determine if this TCP server sends legacy telegrams (for logging only).
     */
    public TeleRecorderTcpServer(int port, boolean isLegacy) {
        super();
        this.port = port;
        this.isLegacy = isLegacy;
        log.info("{}TCP server started on port {} ...", isLegacy ? "Legacy " : "", port);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            // keep server running, awaiting connections
            while (true) {
                // hang in here until a client connects
                Socket socket = serverSocket.accept();

                // add the OutputStream of the connected client with its connected port to the streaming map
                tcpStreams.put(socket.getPort(), socket.getOutputStream());
                log.info("{}TCP Server: Connection from port {} established.", isLegacy ? "Legacy " : "",
                        socket.getPort());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(int port) {
        tcpStreams.remove(port);
        log.info("{}TCP Server: Connection from port {} closed.", isLegacy ? "Legacy " : "", port);
    }

}
