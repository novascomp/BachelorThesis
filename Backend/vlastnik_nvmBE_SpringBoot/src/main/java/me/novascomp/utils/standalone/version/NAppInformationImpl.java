package me.novascomp.utils.standalone.version;

public class NAppInformationImpl implements iNAppInformation {

    private final String contextRoot;
    private final String creator;
    private final String company;
    private final String email;
    private final String webPage;
    private final iNVersion version;

    public NAppInformationImpl(String contextRoot, String creator, String company, String email, String webPage, iNVersion version) {
        this.contextRoot = contextRoot;
        this.creator = creator;
        this.company = company;
        this.email = email;
        this.webPage = webPage;
        this.version = version;
    }

    @Override
    public String getContextRoot() {
        return contextRoot;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getWebPage() {
        return webPage;
    }

    @Override
    public iNVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "NAppInformationImpl{" + "contextRoot=" + contextRoot + ", creator=" + creator + ", company=" + company + ", email=" + email + ", webPage=" + webPage + ", version=" + version + '}';
    }

}
