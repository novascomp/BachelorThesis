package me.novascomp.utils.standalone.service.components;

import java.util.Optional;

public class GeneralCreateResponse<Model, T extends Requirement, Rule extends CrudCreateBusinessRule> implements GeneralResponse {

    protected final Optional<Model> model;
    protected final T requirement;
    protected final Rule businessRule;
    protected final boolean successful;

    public GeneralCreateResponse(Optional<Model> model, T requirement, Rule businessRule, boolean successful) {
        this.model = model;
        this.requirement = requirement;
        this.businessRule = businessRule;
        this.successful = successful;
    }

    public Optional<Model> getModel() {
        return model;
    }

    public T getRequirement() {
        return requirement;
    }

    public Rule getBusinessRule() {
        return businessRule;
    }

    public boolean isRequirementValid() {
        return requirement.isValid();
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String toString() {
        return "GeneralCreateResponse{" + "model=" + model + ", requirement=" + requirement + ", businessRule=" + businessRule + ", successful=" + successful + '}';
    }

}
