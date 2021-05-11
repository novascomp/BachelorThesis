package me.novascomp.utils.microservice.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MicroserviceTokenRequest {

    private final String tokenServiceUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    private Long requestTime;
    private String bearerToken;
    private OAuthResponse authResponse;

    private ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(MicroserviceTokenRequest.class.getName());

    public MicroserviceTokenRequest(MicroserviceCredentials credentials, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.tokenServiceUrl = credentials.getTokenServiceUrl();
        this.clientId = credentials.getClientId();
        this.clientSecret = credentials.getSecretId();
        this.scope = credentials.getScope();

        try {
            initToken();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MicroserviceTokenRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initToken() throws IOException, InterruptedException {
        HttpResponse<String> httpResponse = requestToken();
        LOG.log(Level.INFO, httpResponse.toString());

        if (httpResponse.statusCode() == 200) {
            requestTime = System.currentTimeMillis();
            authResponse = objectMapper.readValue(httpResponse.body(), OAuthResponse.class);
        }
    }

    private HttpResponse<String> requestToken() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", "client_credentials");
        data.put("client_id", clientId);
        data.put("client_secret", clientSecret);
        data.put("scope", scope);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(ofFormData(data))
                .uri(URI.create(tokenServiceUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.of(5, SECONDS))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    //https://mkyong.com/java/java-11-httpclient-examples/
    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    public String getBearerToken() {
        if (authResponse != null) {
            final long expiresIn = requestTime + (authResponse.getExpiresIn() * 1000);

            if (System.currentTimeMillis() < expiresIn) {
                return authResponse.getAccessToken();
            } else {
                try {
                    initToken();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(MicroserviceTokenRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return authResponse.getAccessToken();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "MicroserviceTokenRequest{" + "tokenServiceUrl=" + tokenServiceUrl + ", clientId=" + clientId + ", clientSecret=" + clientSecret + ", scope=" + scope + ", bearerToken=" + bearerToken + '}';
    }

}
