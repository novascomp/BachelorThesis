package me.novascomp.microservice.nvflat.model;

public class LightweightDetail {

    private String detailId;

    private String size;

    private String commonShareSize;

    private LightweightFlat flat;

    private String residentsLink;

    private String messagesLink;

    private String flatLink;

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCommonShareSize() {
        return commonShareSize;
    }

    public void setCommonShareSize(String commonShareSize) {
        this.commonShareSize = commonShareSize;
    }

    public LightweightFlat getFlat() {
        return flat;
    }

    public void setFlat(LightweightFlat flat) {
        this.flat = flat;
    }

    public String getResidentsLink() {
        return residentsLink;
    }

    public void setResidentsLink(String residentsLink) {
        this.residentsLink = residentsLink;
    }

    public String getMessagesLink() {
        return messagesLink;
    }

    public void setMessagesLink(String messagesLink) {
        this.messagesLink = messagesLink;
    }

    public String getFlatLink() {
        return flatLink;
    }

    public void setFlatLink(String flatLink) {
        this.flatLink = flatLink;
    }

}
