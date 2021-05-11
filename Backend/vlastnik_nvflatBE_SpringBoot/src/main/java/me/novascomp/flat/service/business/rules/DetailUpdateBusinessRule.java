package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class DetailUpdateBusinessRule extends CrudUpdateBusinessRule {

    private final boolean flatIdOk;

    public DetailUpdateBusinessRule(boolean flatIdOk, boolean conflict, boolean idOk) {
        super(conflict, idOk);
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
