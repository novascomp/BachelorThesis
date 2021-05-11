package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class OrganizationUpdateBusinessRule extends CrudUpdateBusinessRule {

    public OrganizationUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }

}
