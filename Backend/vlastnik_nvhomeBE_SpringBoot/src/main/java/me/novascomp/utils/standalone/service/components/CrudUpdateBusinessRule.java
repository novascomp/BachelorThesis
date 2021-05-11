package me.novascomp.utils.standalone.service.components;

public abstract class CrudUpdateBusinessRule implements BusinessRule {

    private final boolean conflict;
    private final boolean idOk;

    public CrudUpdateBusinessRule(boolean conflict, boolean idOk) {
        this.conflict = conflict;
        this.idOk = idOk;
    }

    public boolean isConflict() {
        return conflict;
    }

    public boolean isIdOk() {
        return idOk;
    }

    @Override
    public boolean isValid() {
        return (conflict == false) && (idOk == true);
    }
}
