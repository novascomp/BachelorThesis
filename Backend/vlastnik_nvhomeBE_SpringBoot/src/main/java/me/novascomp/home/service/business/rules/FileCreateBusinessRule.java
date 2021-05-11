package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;

public class FileCreateBusinessRule extends CrudCreateBusinessRule {

    private final boolean organizationIdOk;

    public FileCreateBusinessRule(boolean organizationIdOk, boolean foundInDatabase) {
        super(foundInDatabase);
        this.organizationIdOk = organizationIdOk;
    }

    public boolean isOrganizationIdOk() {
        return organizationIdOk;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && organizationIdOk;
    }

}
