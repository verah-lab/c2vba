package de.heuboe.tls.tele.recorder.server;

import de.heuboe.tls.tele.recorder.config.TeleRecorderProperties;
import de.heuboe.tls.tele.recorder.utils.TeleRecorderUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link TeleRecorderHttpServer} provides the server logic for distribute telegrams via sockets.
 */
@RequestMapping("teleRecorder")
@Slf4j
public class TeleRecorderHttpServer {

    /* Map that contains all connected clients with their session id and there output stream. */
    @Getter
    @Setter
    private Map<String, ServletOutputStream> streams = new ConcurrentHashMap<>();

    @Autowired
    private TeleRecorderProperties properties;

    @Autowired
    private TeleRecorderUtils teleRecorderUtils;

    public TeleRecorderHttpServer() {
        log.info("HTTP server started ...");
    }

    /**
     * Sends the current telegrams that are saved on the server.
     *
     * @param request  The {@link HttpServletRequest} that contains client information.
     * @param response The {@link HttpServletResponse} that will be used to answer the client.
     * @throws IOException if reading response or writing result fails.
     */
    @RequestMapping(value = "/${tls.tele.recorder.server.http.path.readTelegrams:read}", method = RequestMethod.GET)
    public void readCurrentTelegrams(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String sessionId = request.getSession().getId();
        String addr = request.getRemoteHost();
        int port = request.getRemotePort();

        ServletOutputStream responseOutputStream = response.getOutputStream();

        // get a sorted list of all telegram files
        List<String> fileList = teleRecorderUtils.getTelegramList(false);

        log.info("Client with session id '{}' from {}:{} requested current telegrams.", sessionId, addr, port);

        for (String file : fileList) {
            Path path = Paths.get(file);

            // print file content as byte array to stream
            responseOutputStream.write(Files.readAllBytes(path));
        }

        if (fileList.isEmpty()) {
            log.info("No telegrams sent to client.");
        } else {
            log.info("{} telegram{} sent to client.", fileList.size(), fileList.size() == 1 ? "" : "s");
        }
    }

    /**
     * Streams all telegrams that are newly incoming to the connected clients.
     *
     * @param request  The {@link HttpServletRequest} that contains client information.
     * @param response The {@link HttpServletResponse} that will be used to answer the client.
     */
    @RequestMapping(value = "/${tls.tele.recorder.server.http.path.streamTelegrams:stream}", method = RequestMethod.GET)
    public void streamTelegrams(HttpServletRequest request, HttpServletResponse response) {

        String sessionId = request.getSession().getId();
        String addr = request.getRemoteHost();
        int port = request.getRemotePort();

        log.info("Streaming client connected with session id '{}' from {}:{}", sessionId, addr, port);

        try {
            connectClient(request, response);
            while (true) {

                Thread.sleep(100);
                if (streams.get(sessionId) == null) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            log.trace("Streaming interrupted: {}", e.getLocalizedMessage());
            Thread.currentThread().interrupt();
        }
        log.info("Connection closed by client with session id '{}'.", sessionId);
        disconnectClient(sessionId);
    }

    /**
     * Adds the session id and output stream of a client to the stream map.
     *
     * @param request  The {@link HttpServletRequest} that contains client information.
     * @param response The {@link HttpServletResponse} that will be used to answer the client.
     * @throws IOException if accessing the output stream fails.
     */
    private void connectClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        streams.put(request.getSession().getId(), response.getOutputStream());
    }

    /**
     * Removes the session id and output stream of a client from the stream map.
     *
     * @param sessionId The session id of the client.
     */
    private void disconnectClient(String sessionId) {
        streams.remove(sessionId);
    }
}
