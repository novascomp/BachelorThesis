package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import me.novascomp.flat.config.BeansInit;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.model.Scope;
import me.novascomp.flat.service.ScopeService;
import me.novascomp.flat.service.UserService;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scopes")
public class ScopeController extends GeneralController<Scope, ScopeService> {

    private final TokenController tokenController;

    @Autowired
    public ScopeController(TokenController tokenController) {
        this.tokenController = tokenController;
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<?> deleteEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0}, request", id);

        try {
            crudDeleteSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Scope> entity = service.findById(id);
        if (entity.isEmpty()) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            entity.get().getTokenList().forEach((token) -> {
                tokenController.getEntityByIdDeleteScope(principal, token.getTokenId(), id);
            });

            service.delete(entity.get());
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(entity.get(), HttpStatus.NO_CONTENT);
        }
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Scope model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, Scope model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.SCOPE_ID, model.getScopeId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Scope model) {
        requirement.setAttribute(Attribute.SCOPE, model.getScope());
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) {
        //available for all users
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Scope model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Scope model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        //available for all users
    }

}
