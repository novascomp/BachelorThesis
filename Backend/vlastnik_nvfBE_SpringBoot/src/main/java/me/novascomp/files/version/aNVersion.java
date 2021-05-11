package me.novascomp.files.version;

import org.springframework.stereotype.Component;

@Component
public abstract class aNVersion implements iNVersion {

    private final String dateOfRelease;
    private final String productName;
    private final String productVersion;
    private final String productBuild;

    public aNVersion(String dateOfRelease, String productName, String productVersion, String productBuild) {
        this.dateOfRelease = dateOfRelease;
        this.productName = productName;
        this.productVersion = productVersion;
        this.productBuild = productBuild;
    }

    @Override
    public String getDateOfRelease() {
        return this.dateOfRelease;
    }

    @Override
    public String getProductName() {
        return this.productName;
    }

    @Override
    public String getProductVersion() {
        return this.productVersion;
    }

    @Override
    public String getProductBuild() {
        return this.productBuild;
    }

    @Override
    public String toString() {
        return "aNVersion{" + "dateOfRelease=" + dateOfRelease + ", productName=" + productName + ", productVersion=" + productVersion + ", productBuild=" + productBuild + '}';
    }

}
