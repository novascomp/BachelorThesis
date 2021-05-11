package me.novascomp.home.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import me.novascomp.home.config.BeansInit;
import me.novascomp.home.model.Token;
import me.novascomp.home.model.User;
import me.novascomp.home.service.RegistrationService;
import me.novascomp.home.service.TokenService;
import me.novascomp.home.service.UserService;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@RestController
@RequestMapping("/tokens")
public class TokenController extends GeneralController<Token, TokenService> {

    private final OrganizationController organizationController;
    private final RegistrationController registrationController;

    @Autowired
    public TokenController(OrganizationController organizationController, RegistrationController registrationController) {
        this.organizationController = organizationController;
        this.registrationController = registrationController;
    }

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Token model, @RequestParam(required = false) String recaptcha_token) {

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_TOKEN, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.LOCKED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            crudCreateSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), model);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return super.postEntity(principal, model, recaptcha_token);
    }

    @Override
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0}, request", id);

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_TOKEN, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            crudDeleteSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> entity = service.findById(id);
        if (entity.isPresent()) {
            try {
                service.deleteOrganizationToken(entity.get(), UserService.getUserUidByPrincipal(principal), registrationController.getService());
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        } else {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/user/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyTokenKey(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Token token, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Token By Key to user, request");

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_PIN_TOKEN, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", HttpStatus.LOCKED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        if (!Attribute.KEY.checkConstraints(token.getKey()).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            String organizatinId = service.processTokenAddRequest(token, registrationController.getService(), principal);
            final HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, organizatinId);
            LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", HttpStatus.OK.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", httpStatus.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserTokens(@AuthenticationPrincipal Jwt principal) {
        LOG.log(Level.INFO, "Starting GET Entity By Principal user, request");

        try {
            LOG.log(Level.INFO, "DONE GET Entity By Principal user, request http status: {0}", HttpStatus.OK.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(service.getUserTokens(UserService.getUserUidByPrincipal(principal)), HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Principal user, request http status: {0}", httpStatus.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdUserById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} user, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} user, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} user, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(token.get().getUserId(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} user, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPostUser(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull User user) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} user, request", id);

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} user, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            try {
                service.addUserToToken(id, user.getUserId());
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} user, request http status: {1}", new Object[]{id, HttpStatus.CREATED.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.CREATED);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} user, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} user, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/{id}/user/{userid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDeleteUser(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String userid) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} and By User Id {1}, request", new Object[]{id, userid});

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and By User Id {1}, request {2}", new Object[]{id, userid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            try {
                service.removeUserFromToken(id, userid);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and By User Id {1}, request {2}", new Object[]{id, userid, HttpStatus.NO_CONTENT.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and By User Id {1}, request {2}", new Object[]{id, userid, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and By User Id {1}, request {2}", new Object[]{id, userid, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/organization", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdOrganizationById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id organization: {0}, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id organization: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id organization: {0}, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(token.get().getOrganization(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id organization: {0}, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Token model) {
        if (Optional.ofNullable(model.getKey()).isEmpty()) {
            model.setKey(service.generateTokenKey(RegistrationService.NHOME_TOKEN_PREFIX));
        }
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, Token model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.TOKEN_ID, model.getTokenId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Token model) {
        requirement.setAttribute(Attribute.KEY, model.getKey());

        if (Optional.ofNullable(model.getOrganization()).isPresent()) {
            requirement.setAttribute(Attribute.ORGANIZATION_ID, model.getOrganization().getOrganizationId());
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            final Optional<Token> token = service.findById(entityId);
            if (token.isPresent()) {
                organizationController.byOrganizationTokenOwnerSecurity(scopes, userUid, token.get().getOrganization().getOrganizationId());
                return;
            }
            throw new ForbiddenException();
        }
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Token model) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            organizationController.byOrganizationTokenOwnerSecurity(scopes, userUid, model.getOrganization().getOrganizationId());
        }
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Token model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            final Optional<Token> token = service.findById(entityId);
            if (token.isPresent()) {
                organizationController.byOrganizationTokenOwnerSecurity(scopes, userUid, token.get().getOrganization().getOrganizationId());
                return;
            }
            throw new ForbiddenException();
        }
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }
}
