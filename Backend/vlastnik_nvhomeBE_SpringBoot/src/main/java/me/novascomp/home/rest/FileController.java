package me.novascomp.home.rest;

import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.novascomp.home.model.File;
import me.novascomp.home.service.FileService;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@RestController
@RequestMapping("/files")
public class FileController extends GeneralController<File, FileService> {

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, File model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, File model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.FILE_ID, model.getFileId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, File model) {
        requirement.setAttribute(Attribute.ID_NVM, model.getIdNvm());

        if (Optional.ofNullable(model.getOrganization()).isPresent()) {
            requirement.setAttribute(Attribute.ORGANIZATION_ID, model.getOrganization().getOrganizationId());
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, File model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, File model) throws SecurityException {
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
