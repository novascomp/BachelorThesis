package me.novascomp.microservice.nvf;

import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;

public class MicroserviceNvfCredentials extends MicroserviceCredentials {

    private final String nvfUploadPath;

    private final String rootUrl;

    public MicroserviceNvfCredentials(String rootUrl, String nvfUploadPath, String tokenServiceUrl, String clientId, String secretId, String scope) {
        super(tokenServiceUrl, clientId, secretId, scope);
        this.nvfUploadPath = nvfUploadPath;
        this.rootUrl = rootUrl;
    }

    public String getNvfUploadPath() {
        return nvfUploadPath;
    }

    public String getRootUrl() {
        return rootUrl;
    }

}
