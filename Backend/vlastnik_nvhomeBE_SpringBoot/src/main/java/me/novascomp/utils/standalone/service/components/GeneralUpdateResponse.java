package me.novascomp.utils.standalone.service.components;

public class GeneralUpdateResponse<T extends Requirement, Rule extends CrudUpdateBusinessRule> implements GeneralResponse {

    protected final T requirement;
    protected final Rule businessRule;
    protected final boolean successful;

    public GeneralUpdateResponse(T requirement, Rule businessRule, boolean successful) {
        this.requirement = requirement;
        this.businessRule = businessRule;
        this.successful = successful;
    }

    public T getRequirement() {
        return requirement;
    }

    public Rule getBusinessRule() {
        return businessRule;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String toString() {
        return "GeneralUpdateResponse{" + "requirement=" + requirement + ", businessRule=" + businessRule + ", successful=" + successful + '}';
    }

}
