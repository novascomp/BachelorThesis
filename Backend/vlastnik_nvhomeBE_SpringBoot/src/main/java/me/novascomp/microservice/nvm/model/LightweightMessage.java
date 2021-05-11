package me.novascomp.microservice.nvm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.novascomp.home.model.General;

public class LightweightMessage {

    // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String messageId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String fileId;

    private String heading;
    private String body;

    private String creatorKey;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String filesLink;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resLink;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String prioritiesLink;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String categoriesLink;

    private String categoryComponentLink;
    private String priorityComponentLink;
    private String contentLink;

    private General general;

    public LightweightMessage() {
    }

    public LightweightMessage(String heading, String body, String creatorKey) {
        this.heading = heading;
        this.body = body;
        this.creatorKey = creatorKey;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFilesLink() {
        return filesLink;
    }

    public void setFilesLink(String filesLink) {
        this.filesLink = filesLink;
    }

    public String getResLink() {
        return resLink;
    }

    public void setResLink(String resLink) {
        this.resLink = resLink;
    }

    public String getPrioritiesLink() {
        return prioritiesLink;
    }

    public void setPrioritiesLink(String prioritiesLink) {
        this.prioritiesLink = prioritiesLink;
    }

    public String getCategoriesLink() {
        return categoriesLink;
    }

    public void setCategoriesLink(String categoriesLink) {
        this.categoriesLink = categoriesLink;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(String creatorKey) {
        this.creatorKey = creatorKey;
    }

    public String getCategoryComponentLink() {
        return categoryComponentLink;
    }

    public void setCategoryComponentLink(String categoryComponentLink) {
        this.categoryComponentLink = categoryComponentLink;
    }

    public String getPriorityComponentLink() {
        return priorityComponentLink;
    }

    public void setPriorityComponentLink(String priorityComponentLink) {
        this.priorityComponentLink = priorityComponentLink;
    }

    public String getContentLink() {
        return contentLink;
    }

    public void setContentLink(String contentLink) {
        this.contentLink = contentLink;
    }

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

}
