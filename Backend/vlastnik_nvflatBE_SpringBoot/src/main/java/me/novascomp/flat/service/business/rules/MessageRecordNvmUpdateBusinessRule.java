package me.novascomp.flat.service.business.rules;

import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;

public class MessageRecordNvmUpdateBusinessRule extends CrudUpdateBusinessRule {

    public MessageRecordNvmUpdateBusinessRule(boolean conflict, boolean idOk) {
        super(conflict, idOk);
    }

}
