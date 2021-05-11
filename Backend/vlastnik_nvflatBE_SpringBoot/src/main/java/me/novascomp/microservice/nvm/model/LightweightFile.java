package me.novascomp.microservice.nvm.model;

public class LightweightFile {

    private String fileNvfId;
    private String fileIdInNvf;
    private String sharesLink;

    public String getFileNvfId() {
        return fileNvfId;
    }

    public void setFileNvfId(String fileNvfId) {
        this.fileNvfId = fileNvfId;
    }

    public String getFileIdInNvf() {
        return fileIdInNvf;
    }

    public void setFileIdInNvf(String fileIdInNvf) {
        this.fileIdInNvf = fileIdInNvf;
    }

    public String getSharesLink() {
        return sharesLink;
    }

    public void setSharesLink(String sharesLink) {
        this.sharesLink = sharesLink;
    }

}
