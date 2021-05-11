package me.novascomp.microservice.nvf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import me.novascomp.microservice.communication.Microservice;
import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class MicroserviceNvf extends Microservice {

    @Autowired
    public MicroserviceNvf(MicroserviceCredentials microserviceCredentials, ObjectMapper objectMapper) {
        super(microserviceCredentials, objectMapper);
    }

    public HttpResponse<String> deleteFile(@NotNull String fileId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = deleteWhateverByLinkRequest(nvfPath + "files/" + fileId);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getFileShares(@NotNull String fileId, @NotNull Pageable pageable) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = getWhateverByLinkRequest(createFileSharesGetLink(fileId, pageable), pageable);
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String createFileSharesGetLink(@NotNull String fileId, Pageable pageable) {
        return nvfPath + "shares/files/" + fileId;
    }

    public HttpResponse<String> checkFileExistence(String fileId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .GET()
                .uri(URI.create(nvfPath + "files/" + URLEncoder.encode(fileId, StandardCharsets.UTF_8)))
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

}
