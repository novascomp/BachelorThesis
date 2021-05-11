package me.novascomp.home.flat.uploader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatUploader {

    private List<NVHomeFlat> flatsToUpload = new ArrayList<>();

    @JsonIgnore
    private final Map<String, LightweightToken> generatedTokens = new HashMap<>();

    public List<NVHomeFlat> getFlatsToUpload() {
        return flatsToUpload;
    }

    public void setFlatsToUpload(List<NVHomeFlat> flatsToUpload) {
        this.flatsToUpload = flatsToUpload;
    }

    public Map<String, LightweightToken> getGeneratedTokens() {
        return generatedTokens;
    }

    @Override
    public String toString() {
        return "FlatUploader{" + "flatsToUpload=" + flatsToUpload.toString() + '}';
    }

}
