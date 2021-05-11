package me.novascomp.utils.microservice.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;
import me.novascomp.utils.microservice.oauth.MicroserviceTokenRequest;

public abstract class Microservice {

    protected final MicroserviceCredentials microserviceCredentials;
    protected final MicroserviceTokenRequest microserviceTokenRequest;
    protected final ObjectMapper objectMapper;

    protected final static long REQUESTED_TIMEOUT = 30;
    protected final static long REQUESTED_FILE_TIMEOUT = 60;
    protected final static String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
    protected final static String MULTIPART_FORM_DATA_BOUNDARY = "multipart/form-data;boundary=";
    protected final static String BEARER = "Bearer ";

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    @Autowired
    @Qualifier("nvhomePath")
    protected String nvhomePath;

    @Autowired
    @Qualifier("nvmPath")
    protected String nvmPath;

    @Autowired
    @Qualifier("production")
    protected boolean production;

    public Microservice(MicroserviceCredentials microserviceCredentials, ObjectMapper objectMapper) {
        this.microserviceCredentials = microserviceCredentials;
        this.microserviceTokenRequest = new MicroserviceTokenRequest(this.microserviceCredentials, objectMapper);
        this.objectMapper = objectMapper;
    }

    public HttpResponse<String> getWhateverByLinkResponse(@NotNull String link) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(link);
        LOG.log(Level.INFO, request.toString());
        return returnHttpResponse(request);
    }

    protected HttpResponse<String> getWhateverByLinkUnlimitedPageableResponse(@NotNull String link) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequestUnlimitdPageable(link);
        LOG.log(Level.INFO, request.toString());
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getWhateverByLinkResponse(@NotNull String link, @NotNull Pageable pageable) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(link, pageable);
        LOG.log(Level.INFO, request.toString());
        return returnHttpResponse(request);
    }

    protected HttpResponse<String> postWhateverByLinkResponse(@NotNull String link, @NotNull Object body, @NotNull String principalToken) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = postWhateverByLinkRequest(link, body, principalToken);
        LOG.log(Level.INFO, objectMapper.writeValueAsString(body));
        LOG.log(Level.INFO, request.toString());
        return returnHttpResponse(request);
    }

    protected HttpResponse<String> postWhateverByLinkResponse(@NotNull String link, @NotNull Object body) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = postWhateverByLinkRequest(link, body);
        LOG.log(Level.INFO, objectMapper.writeValueAsString(body));
        LOG.log(Level.INFO, request.toString());
        return returnHttpResponse(request);
    }

    protected HttpResponse<String> deleteWhateverByLinkResponse(@NotNull String link) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = deleteWhateverByLinkRequest(link);
        LOG.log(Level.INFO, request.toString());
        return returnHttpResponse(request);
    }

    protected HttpResponse<String> returnHttpResponse(HttpRequest request) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        LOG.log(Level.INFO, httpResponse.toString());
        return httpResponse;
    }

    protected HttpRequest getWhateverByLinkRequest(@NotNull String link) {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .GET()
                .uri(URI.create(link))
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected HttpRequest getWhateverByLinkRequest(@NotNull String link, Pageable pageable) {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .GET()
                .uri(URI.create(link + addQueryParam(pageable)))
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected HttpRequest getWhateverByLinkRequestUnlimitdPageable(@NotNull String link) {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .GET()
                .uri(URI.create(link + addQueryParamUnlimited()))
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected HttpRequest postWhateverByLinkRequest(@NotNull String link, @NotNull Object body, Pageable pageable) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .uri(URI.create(link + addQueryParam(pageable)))
                .header(HttpHeaders.CONTENT_TYPE, JSON_CONTENT_TYPE)
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected HttpRequest postWhateverByLinkRequest(@NotNull String link, @NotNull Object body, @NotNull String principalToken) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + principalToken)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .uri(URI.create(link))
                .header(HttpHeaders.CONTENT_TYPE, JSON_CONTENT_TYPE)
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected HttpRequest postWhateverByLinkRequest(@NotNull String link, @NotNull Object body) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .uri(URI.create(link))
                .header(HttpHeaders.CONTENT_TYPE, JSON_CONTENT_TYPE)
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected HttpRequest deleteWhateverByLinkRequest(@NotNull String link) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .DELETE()
                .uri(URI.create(link))
                .timeout(Duration.of(REQUESTED_TIMEOUT, SECONDS))
                .build();
    }

    protected String addQueryParamUnlimited() {
        return "?page=" + 0 + "&size=" + Integer.MAX_VALUE;
    }

    protected String addQueryParam(Pageable pageable) {
        if (String.valueOf(pageable.getSort()).contains("UNSORTED")) {
            return "?page=" + String.valueOf(pageable.getPageNumber()) + "&size=" + String.valueOf(pageable.getPageSize());
        } else {
            return "?page=" + String.valueOf(pageable.getPageNumber()) + "&size=" + String.valueOf(pageable.getPageSize()) + "&sort=" + String.valueOf(pageable.getSort()).replaceAll(" ", ",").replaceAll(":", "");
        }
    }

    protected HttpResponse<String> sendMultipartFormData(Map<Object, Object> map, String path) throws URISyntaxException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String boundary = new BigInteger(256, new Random()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MULTIPART_FORM_DATA_BOUNDARY + boundary)
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .POST(ofMimeMultipartData(map, boundary))
                .uri(URI.create(path))
                .timeout(Duration.of(REQUESTED_FILE_TIMEOUT, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    protected HttpResponse<String> sendMultipartFormDataByPath(Map<Object, Object> map, String path, String filename) throws URISyntaxException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String boundary = new BigInteger(256, new Random()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MULTIPART_FORM_DATA_BOUNDARY + boundary)
                .header(HttpHeaders.AUTHORIZATION, BEARER + microserviceTokenRequest.getBearerToken())
                .POST(ofMimeMultipartData(map, boundary, filename))
                .uri(URI.create(path))
                .timeout(Duration.of(REQUESTED_FILE_TIMEOUT, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    //https://golb.hplar.ch/2019/01/java-11-http-client.html
    protected HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
            String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) entry.getValue();
                String mimeType = file.getContentType();
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + file.getOriginalFilename()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(file.getInputStream().readAllBytes());
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    protected HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
            String boundary, String filename) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + filename
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
