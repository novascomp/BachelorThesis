package me.novascomp.flat.rest;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.novascomp.flat.model.MessageRecordNvm;
import me.novascomp.flat.service.MessageRecordNvmService;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@RestController
@RequestMapping("/messages")
public class MessageRecordNvmController extends GeneralController<MessageRecordNvm, MessageRecordNvmService> {

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, MessageRecordNvm model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, MessageRecordNvm model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.MESSAGE_RECORD_NVM_ID, model.getMessageRecordNvmId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, MessageRecordNvm model) {
        requirement.setAttribute(Attribute.MESSAGE_ID_IN_NVM, model.getIdInNvm());
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, MessageRecordNvm model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, MessageRecordNvm model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }
}
