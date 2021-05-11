package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;

public class TokenCreateBusinessRule extends CrudCreateBusinessRule {

    private final boolean organizationIdOk;

    public TokenCreateBusinessRule(boolean organizationIdOk, boolean foundInDatabase) {
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
