package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;

public class UserCreateBusinessRule extends CrudCreateBusinessRule {

    public UserCreateBusinessRule(boolean foundInDatabase) {
        super(foundInDatabase);
    }

}
