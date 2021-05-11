package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;

public class DetailCreateBusinessRule extends CrudCreateBusinessRule {

    private final boolean flatIdOk;

    public DetailCreateBusinessRule(boolean flatIdOk, boolean foundInDatabase) {
        super(foundInDatabase);
        this.flatIdOk = flatIdOk;
    }

    public boolean isFlatIdOk() {
        return flatIdOk;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && flatIdOk;
    }

}
