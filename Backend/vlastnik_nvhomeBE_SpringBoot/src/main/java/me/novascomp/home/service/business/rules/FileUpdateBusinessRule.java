package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class FileUpdateBusinessRule extends CrudUpdateBusinessRule {

    private final boolean organizationIdOk;

    public FileUpdateBusinessRule(boolean organizationIdOk, boolean conflict, boolean idOk) {
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
