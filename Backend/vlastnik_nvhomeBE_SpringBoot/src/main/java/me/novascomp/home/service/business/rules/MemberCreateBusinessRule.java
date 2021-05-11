package me.novascomp.home.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;

public class MemberCreateBusinessRule extends CrudCreateBusinessRule {

    private final boolean requiredCommitteeFound;

    public MemberCreateBusinessRule(boolean foundInDatabase, boolean requiredCommitteeFound) {
        super(foundInDatabase);
        this.requiredCommitteeFound = requiredCommitteeFound;
    }

    public boolean isRequiredCommitteeFound() {
        return requiredCommitteeFound;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && requiredCommitteeFound;
    }

}
