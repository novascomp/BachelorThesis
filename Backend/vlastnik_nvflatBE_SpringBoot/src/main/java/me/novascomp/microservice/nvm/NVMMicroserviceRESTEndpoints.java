package me.novascomp.microservice.nvm;

import me.novascomp.utils.microservice.communication.Endpoint;

public class NVMMicroserviceRESTEndpoints extends Endpoint {

    private final String filesPath = "files/";
    private final String messagesPath = "messages/";
    private final String bycreator = "bycreator/";

    public NVMMicroserviceRESTEndpoints(String basePath) {
        super(basePath);
    }

    public String getUploadNvfFileById(String messageId) {
        return basePath + messagesPath + messageId + "/files";
    }

    public String getFileByIdSharesPath(String nvfFileId) {
        return basePath + filesPath + nvfFileId + "/shares";
    }

    public String getMessageComponentPathById(String componentName, String messageId) {
        return basePath + messagesPath + messageId + "/" + componentName;
    }

    public String getMessageComponentPathById(String componentName, String messageId, String componentId) {
        return basePath + messagesPath + messageId + "/" + componentName + "/" + componentId;
    }

    public String getComponentPathById(String componentName, String componentId) {
        return basePath + componentName + "/" + componentId;
    }

    public String getMessagesByCreatorKeyPath(String creatorKey) {
        return basePath + messagesPath + bycreator + creatorKey;
    }

    public String getMessagesByCategoryHierarchy() {
        return basePath + messagesPath + "bycategoryhierarchy";
    }

    public String getMessagesByCategoriesPath() {
        return basePath + messagesPath + "bycategories";
    }

    public String getMessagesDeleteCategoriesPath(String messageId, String categoryId) {
        return basePath + messagesPath + messageId + "/" + "categories/" + categoryId;
    }

    public String getMessageByIdPath(String nvmId) {
        return basePath + messagesPath + nvmId;
    }

    public String getMessagesPath() {
        return basePath + messagesPath;
    }

    public String getComponentPath(String componentName) {
        return basePath + componentName;
    }

    public String getComponentListPath(String componentName) {
        return basePath + componentName + "/list";
    }

    public String getComponentListDeletePath(String componentName) {
        return basePath + componentName + "/list/delete";
    }

    public String getComponentByCreatorAndText(String componentName) {
        return basePath + componentName + "/" + "bycreatorandtext";
    }

    public String getComponentByCreatorPath(String componentName, String creatorKey) {
        return basePath + componentName + "/" + bycreator + creatorKey;
    }
}
