package me.novascomp.utils.microservice.oauth;

public class MicroserviceCredentials {

    private final String tokenServiceUrl;

    private final String clientId;

    private final String secretId;

    private final String scope;

    public MicroserviceCredentials(String tokenServiceUrl, String clientId, String secretId, String scope) {
        this.tokenServiceUrl = tokenServiceUrl;
        this.clientId = clientId;
        this.secretId = secretId;
        this.scope = scope;
    }

    public String getTokenServiceUrl() {
        return tokenServiceUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "MicroserviceCredentials{" + "tokenServiceUrl=" + tokenServiceUrl + ", clientId=" + clientId + ", secretId=" + secretId + ", scope=" + scope + '}';
    }

}
