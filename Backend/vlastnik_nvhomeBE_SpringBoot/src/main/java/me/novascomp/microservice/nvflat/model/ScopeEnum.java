package me.novascomp.microservice.nvflat.model;

public enum ScopeEnum {

    //SCOPER FOR APPLICATION USAGE
    SCOPE_FLAT_OWNER("FLAT_OWNER"),
    SCOPE_CREATE_TOKEN("CREATE_TOKEN"),
    SCOPE_CREATE_RESIDENT("CREATE_RESIDENT"),
    SCOPE_DELETE_RESIDENT("DELETE_RESIDENT"),
    //SCOPES FOR MESSAGES
    SCOPE_READ_BY_FLAT_OWNER("READ_BY_FLAT_OWNER"),
    SCOPE_READ_BY_FLAT_RESIDENT("READ_BY_FLAT_RESIDENT");

    private final String scopeName;

    ScopeEnum(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getScopeName() {
        return scopeName;
    }

}
