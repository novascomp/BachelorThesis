package me.novascomp.utils.standalone.version;

public class NVersionImpl extends aNVersion {

    public NVersionImpl(String dateOfRelease, String productName, String productVersion, String productBuild) {
        super(dateOfRelease, productName, productVersion, productBuild);
    }

    @Override
    public String getApplicationSignature() {
        return getProductName() + " / " + getProductVersion() + " / " + getProductBuild() + " / " + getDateOfRelease().toString();
    }
}
