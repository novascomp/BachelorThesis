package me.novascomp.utils.microservice.communication;

public class Endpoint {

    protected final String basePath;

    public Endpoint(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }

}
