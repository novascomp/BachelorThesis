package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class UserUpdateBusinessRule extends CrudUpdateBusinessRule {

    public UserUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }

}
