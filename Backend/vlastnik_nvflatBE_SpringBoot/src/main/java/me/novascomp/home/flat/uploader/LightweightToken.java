package me.novascomp.home.flat.uploader;

public class LightweightToken {

    private String tokenId;
    private String key;
    private boolean mapped;

    private LightweightFlat flat;
    private String flatId;

    public LightweightToken() {
    }

    public LightweightToken(String tokenId, String key) {
        this.tokenId = tokenId;
        this.key = key;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public LightweightFlat getFlat() {
        return flat;
    }

    public void setFlat(LightweightFlat flat) {
        this.flat = flat;
    }

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    public String getFlatId() {
        return flatId;
    }

    public void setFlatId(String flatId) {
        this.flatId = flatId;
    }
}
