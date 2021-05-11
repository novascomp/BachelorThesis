package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class ResidentUpdateBusinessRule extends CrudUpdateBusinessRule {

    public ResidentUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }
}
