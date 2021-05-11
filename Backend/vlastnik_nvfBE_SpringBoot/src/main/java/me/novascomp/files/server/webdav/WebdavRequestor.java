package me.novascomp.files.server.webdav;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import me.novascomp.files.config.NVFUtils;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.Server;
import org.springframework.http.HttpStatus;

public abstract class WebdavRequestor {

    protected final NVFUtils nvfUtils;
    protected final Server server;
    protected final String authorization;

    public WebdavRequestor(NVFUtils nvfUtils, Server server) {
        this.nvfUtils = nvfUtils;
        this.server = server;
        this.authorization = "Basic " + nvfUtils.getBase64(server.getUsername(), server.getPassword());
    }

    protected HttpStatus getStatusCode(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        return HttpStatus.valueOf(statusCode);
    }

    protected String getFolderPath(Folder folder) {

        String path = "";
        Folder prevFolder = folder;
        while (prevFolder != null) {
            path += prevFolder.getName() + "/";
            prevFolder = prevFolder.getPreviousFolderId();
        }
        return path;
    }

    protected HttpResponse<String> sendCommand(String fullPath, HttpRequest.BodyPublisher bodyPublisher, WebDavCommand command) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method(command.toString(), bodyPublisher)
                .version(HttpClient.Version.HTTP_1_1)
                .uri(URI.create(server.getBaseUrl() + server.getWebdavPath() + fullPath + WebDavCommand.JSON_FORMAT_REQUEST))
                .header(NVFUtils.AUTHORIZATION_HEADER, authorization)
                .timeout(Duration.of(1000, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }
}
