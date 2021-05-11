package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class ScopeUpdateBusinessRule extends CrudUpdateBusinessRule {

    public ScopeUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }

}
