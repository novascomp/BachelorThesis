package me.novascomp.files.server.ocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.files.config.NVFUtils;
import me.novascomp.files.model.Server;
import me.novascomp.files.server.webdav.WebdavRequestor;

public abstract class OcsRequestor extends WebdavRequestor {

    protected ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(OcsRequestor.class.getName());

    public OcsRequestor(NVFUtils nvfUtils, ObjectMapper objectMapper, Server server) {
        super(nvfUtils, server);
        this.objectMapper = objectMapper;
    }

    protected HttpResponse<String> sendCommand(HttpRequest.BodyPublisher bodyPublisher, String additionalPath, OcsCommand command) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method(command.toString(), bodyPublisher)
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(server.getBaseUrl() + server.getOcsPath() + additionalPath + OcsCommand.JSON_FORMAT_REQUEST))
                .headers(NVFUtils.AUTHORIZATION_HEADER, authorization,
                        "Content-Type", "application/json;charset=UTF-8",
                        "Accept", "application/json")
                .timeout(Duration.of(120, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        LOG.log(Level.INFO, request.toString());
        LOG.log(Level.INFO, response.toString());
        return response;
    }
}
