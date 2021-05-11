package me.novascomp.home.flat.uploader;

import me.novascomp.flat.model.Organization;

public class LightweightFlat {

    private String flatId;

    private String identifier;

    private Organization organization;

    private String flatOrganizationLink;

    private String flatTokensLink;

    private String flatDetailLink;

    private String organizationId;

    public LightweightFlat() {
    }

    public String getFlatId() {
        return flatId;
    }

    public void setFlatId(String flatId) {
        this.flatId = flatId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getFlatOrganizationLink() {
        return flatOrganizationLink;
    }

    public void setFlatOrganizationLink(String flatOrganizationLink) {
        this.flatOrganizationLink = flatOrganizationLink;
    }

    public String getFlatTokensLink() {
        return flatTokensLink;
    }

    public void setFlatTokensLink(String flatTokensLink) {
        this.flatTokensLink = flatTokensLink;
    }

    public String getFlatDetailLink() {
        return flatDetailLink;
    }

    public void setFlatDetailLink(String flatDetailLink) {
        this.flatDetailLink = flatDetailLink;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
