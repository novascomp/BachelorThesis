package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class MemberUpdateBusinessRule extends CrudUpdateBusinessRule {

    public MemberUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }
}
