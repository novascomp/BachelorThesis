package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class FlatUpdateBusinessRule extends CrudUpdateBusinessRule {

    private final boolean organizationIdOk;

    public FlatUpdateBusinessRule(boolean organizationIdOk, boolean conflict, boolean idOk) {
        super(conflict, idOk);
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
