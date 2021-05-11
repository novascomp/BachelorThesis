package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class CommitteeUpdateBusinessRule extends CrudUpdateBusinessRule {

    public CommitteeUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }

}
