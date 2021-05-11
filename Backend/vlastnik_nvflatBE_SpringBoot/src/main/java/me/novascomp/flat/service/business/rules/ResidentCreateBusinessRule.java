package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;

public class ResidentCreateBusinessRule extends CrudCreateBusinessRule {

    private final boolean requiredFlatDetailFound;

    public ResidentCreateBusinessRule(boolean foundInDatabase, boolean requiredFlatDetailFound) {
        super(foundInDatabase);
        this.requiredFlatDetailFound = requiredFlatDetailFound;
    }

    public boolean isRequiredFlatDetailFound() {
        return requiredFlatDetailFound;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && requiredFlatDetailFound;
    }
}
