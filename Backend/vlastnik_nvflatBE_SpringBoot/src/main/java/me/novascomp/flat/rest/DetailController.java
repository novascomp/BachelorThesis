package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.novascomp.flat.config.BeansInit;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.config.ScopeEnum;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.service.DetailService;
import me.novascomp.flat.service.UserService;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@RestController
@RequestMapping("/details")
public class DetailController extends GeneralController<Detail, DetailService> {

    private final FlatController flatController;

    @Autowired
    public DetailController(FlatController flatController) {
        this.flatController = flatController;
    }

    @GetMapping(value = "/{id}/residents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetResidents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} residents, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} residents, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Detail> detail = service.findById(id);

        if (detail.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} residents, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(detail.get().getResidentList(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} residents, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetFlat(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} flat, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Detail> detail = service.findById(id);

        if (detail.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(detail.get().getFlat(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetMessages(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} messages, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Detail> detail = service.findById(id);

        if (detail.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} messages, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(detail.get().getMessageRecordNvmList(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} messages, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Detail model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, Detail model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.DETAIL_ID, model.getDetailId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Detail model) {
        requirement.setAttribute(Attribute.SIZE, model.getSize());
        requirement.setAttribute(Attribute.COMMON_SHARE_SIZE, model.getCommonShareSize());

        if (Optional.ofNullable(model.getFlat()).isPresent()) {
            requirement.setAttribute(Attribute.FLAT_ID, model.getFlat().getFlatId());
        }
    }

    public void residentPostSecurityByScope(List<String> scopes, String userUid, String entityId) throws SecurityException {
        residentSecurityByScope(scopes, userUid, entityId, ScopeEnum.SCOPE_CREATE_RESIDENT);
    }

    public void residentDeleteSecurityByScope(List<String> scopes, String userUid, String entityId) throws SecurityException {
        residentSecurityByScope(scopes, userUid, entityId, ScopeEnum.SCOPE_DELETE_RESIDENT);
    }

    public void residentSecurityByScope(List<String> scopes, String userUid, String entityId, ScopeEnum scopeEnum) throws SecurityException {

        if (!scopes.contains(applicationMainScope)) {
            final Optional<Detail> detail = service.findById(entityId);
            if (detail.isPresent()) {
                Flat flat = detail.get().getFlat();
                if (Optional.ofNullable(flat).isPresent()) {
                    flatController.securityFlatByToken(scopes, userUid, flat.getFlatId(), scopeEnum);
                } else {
                    throw new ForbiddenException();
                }
            } else {
                throw new ForbiddenException();
            }
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {

        if (!scopes.contains(applicationMainScope)) {
            List<String> userDetails = service.getUserDetailsByTokenHolding(flatController.getTokenService(), userUid);
            for (String detail : userDetails) {
                if (detail.equals(entityId)) {
                    return;
                }
            }
            throw new ForbiddenException();
        }
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Detail model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Detail model) throws SecurityException {
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
