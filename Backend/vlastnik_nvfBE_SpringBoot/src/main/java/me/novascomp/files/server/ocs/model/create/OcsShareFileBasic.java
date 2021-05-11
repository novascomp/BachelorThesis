package me.novascomp.files.server.ocs.model.create;

public class OcsShareFileBasic {

    private String name;
    private String path;
    private int shareType;

    public OcsShareFileBasic(String name, String path, int shareType) {
        this.name = name;
        this.path = path;
        this.shareType = shareType;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getShareType() {
        return shareType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

}
