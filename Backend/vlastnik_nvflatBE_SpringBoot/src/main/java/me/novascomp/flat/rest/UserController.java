package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
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
import me.novascomp.flat.model.Token;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.model.User;
import me.novascomp.flat.service.UserService;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@RestController
@RequestMapping("/users")
public class UserController extends GeneralController<User, UserService> {

    @GetMapping("/uid")
    public String getUserUidByPrincipal(@AuthenticationPrincipal Jwt principal) {
        LOG.log(Level.INFO, "Starting GET Entity By Principal uid, request");
        LOG.log(Level.INFO, "DONE GET Entity By Principal uid, request");
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return UserService.getUserUidByPrincipal(principal);
    }

    @GetMapping("/scopes")
    public List<String> getUserScopesByPrincipal(@AuthenticationPrincipal Jwt principal) {
        LOG.log(Level.INFO, "Starting GET Entity By Principal scopes, request");
        LOG.log(Level.INFO, "DONE GET Entity By Principal scopes, request");
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return UserService.getUserScopesPrincipal(principal);
    }

    @GetMapping("/tokens")
    public ResponseEntity<?> getUserTokensByPrincipal(@AuthenticationPrincipal Jwt principal) {
        LOG.log(Level.INFO, "Starting GET Entity By Principal tokens, request");
        final Optional<User> user = service.findByUid(UserService.getUserUidByPrincipal(principal));
        LOG.log(Level.INFO, "DONE GET Entity By Principal tokens, request");
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(user.get().getTokenList(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdTokens(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} tokens, request", id);

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<User> user = service.findById(id);

        if (user.isPresent()) {
            List<Token> tokens = user.get().getTokenList();
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, User model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, User model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.USER_ID, model.getUserId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, User model) {
        requirement.setAttribute(Attribute.USER_UID, model.getUid());
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, User model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, User model) throws SecurityException {
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
