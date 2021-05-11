package me.novascomp.utils.standalone.service.components;

public abstract class CrudCreateBusinessRule implements BusinessRule {

    private final boolean foundInDatabase;

    public CrudCreateBusinessRule(boolean foundInDatabase) {
        this.foundInDatabase = foundInDatabase;
    }

    public boolean isFoundInDatabase() {
        return foundInDatabase;
    }

    @Override
    public boolean isValid() {
        return (foundInDatabase == false);
    }

}
