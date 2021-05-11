
package me.novascomp.files.server.ocs.model.create;

import java.util.HashMap;
import java.util.Map;

public class Data {

    private String id;
    private Integer shareType;
    private String uidOwner;
    private String displaynameOwner;
    private Integer permissions;
    private Integer stime;
    private Object parent;
    private Object expiration;
    private String token;
    private String uidFileOwner;
    private String displaynameFileOwner;
    private Object additionalInfoOwner;
    private Object additionalInfoFileOwner;
    private String path;
    private String itemType;
    private String mimetype;
    private String storageId;
    private Integer storage;
    private Integer itemSource;
    private Integer fileSource;
    private Integer fileParent;
    private String fileTarget;
    private String name;
    private String url;
    private Integer mailSend;
    private Object attributes;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getShareType() {
        return shareType;
    }

    public void setShareType(Integer shareType) {
        this.shareType = shareType;
    }

    public String getUidOwner() {
        return uidOwner;
    }

    public void setUidOwner(String uidOwner) {
        this.uidOwner = uidOwner;
    }

    public String getDisplaynameOwner() {
        return displaynameOwner;
    }

    public void setDisplaynameOwner(String displaynameOwner) {
        this.displaynameOwner = displaynameOwner;
    }

    public Integer getPermissions() {
        return permissions;
    }

    public void setPermissions(Integer permissions) {
        this.permissions = permissions;
    }

    public Integer getStime() {
        return stime;
    }

    public void setStime(Integer stime) {
        this.stime = stime;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public Object getExpiration() {
        return expiration;
    }

    public void setExpiration(Object expiration) {
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUidFileOwner() {
        return uidFileOwner;
    }

    public void setUidFileOwner(String uidFileOwner) {
        this.uidFileOwner = uidFileOwner;
    }

    public String getDisplaynameFileOwner() {
        return displaynameFileOwner;
    }

    public void setDisplaynameFileOwner(String displaynameFileOwner) {
        this.displaynameFileOwner = displaynameFileOwner;
    }

    public Object getAdditionalInfoOwner() {
        return additionalInfoOwner;
    }

    public void setAdditionalInfoOwner(Object additionalInfoOwner) {
        this.additionalInfoOwner = additionalInfoOwner;
    }

    public Object getAdditionalInfoFileOwner() {
        return additionalInfoFileOwner;
    }

    public void setAdditionalInfoFileOwner(Object additionalInfoFileOwner) {
        this.additionalInfoFileOwner = additionalInfoFileOwner;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public Integer getItemSource() {
        return itemSource;
    }

    public void setItemSource(Integer itemSource) {
        this.itemSource = itemSource;
    }

    public Integer getFileSource() {
        return fileSource;
    }

    public void setFileSource(Integer fileSource) {
        this.fileSource = fileSource;
    }

    public Integer getFileParent() {
        return fileParent;
    }

    public void setFileParent(Integer fileParent) {
        this.fileParent = fileParent;
    }

    public String getFileTarget() {
        return fileTarget;
    }

    public void setFileTarget(String fileTarget) {
        this.fileTarget = fileTarget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getMailSend() {
        return mailSend;
    }

    public void setMailSend(Integer mailSend) {
        this.mailSend = mailSend;
    }

    public Object getAttributes() {
        return attributes;
    }

    public void setAttributes(Object attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
