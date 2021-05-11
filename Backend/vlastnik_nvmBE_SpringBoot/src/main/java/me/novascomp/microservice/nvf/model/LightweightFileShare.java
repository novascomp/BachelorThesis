package me.novascomp.microservice.nvf.model;

import java.util.List;

public class LightweightFileShare {

    private String fileShareId;
    private String link;
    private String fileName;
    private List<String> messagesId;

    public LightweightFileShare() {
    }

    public String getFileShareId() {
        return fileShareId;
    }

    public void setFileShareId(String fileShareId) {
        this.fileShareId = fileShareId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getMessagesId() {
        return messagesId;
    }

    public void setMessagesId(List<String> messagesId) {
        this.messagesId = messagesId;
    }
}
