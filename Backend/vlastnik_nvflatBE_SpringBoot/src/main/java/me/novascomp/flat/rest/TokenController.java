package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.config.BeansInit;
import me.novascomp.flat.config.ScopeEnum;
import me.novascomp.flat.model.Resident;
import me.novascomp.flat.model.Scope;
import me.novascomp.flat.model.Token;
import me.novascomp.flat.model.User;
import me.novascomp.flat.service.TokenService;
import me.novascomp.flat.service.UserService;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/tokens")
public class TokenController extends GeneralController<Token, TokenService> {

    private final FlatController flatController;
    public static final String NFLAT_TOKEN_PREFIX = "NFLAT";

    @Autowired
    public TokenController(FlatController flatController) {
        this.flatController = flatController;
    }

    @PostMapping(value = "/user/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyTokenKey(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Token token, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Token By Key to user, request");

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_PIN_TOKEN, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            Optional<Token> tokenRecord = service.findByKey(token.getKey());
            boolean ok = false;
            if (tokenRecord.isPresent()) {
                if (tokenRecord.get().getFlat().getIdentifier().equals(FlatController.DEFAULT_FLAT_NAME)) {
                    ok = true;
                }
            }

            if (!ok) {
                LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", HttpStatus.LOCKED.toString());
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.LOCKED);
            }
        }

        if (!Attribute.KEY.checkConstraints(token.getKey()).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            String flatId = service.processTokenAddRequest(token.getKey(), principal);
            final HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, flatId);
            LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", HttpStatus.OK.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Token By Key to user, request http status: {0}", httpStatus.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserTokens(@AuthenticationPrincipal Jwt principal) {
        LOG.log(Level.INFO, "Starting GET Entity By Principal user, request");

        try {
            List<Token> tokens = service.getUserTokens(UserService.getUserUidByPrincipal(principal));
            LOG.log(Level.INFO, "DONE GET Entity By Principal user, request http status: {0}", HttpStatus.OK.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Principal user, request http status: {0}", httpStatus.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} flat, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(token.get().getFlat(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/scopes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdScopes(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} scopes, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            Page<?> page = service.getTokenByIdScopes(id, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/scopes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPostScope(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull Scope scope) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} scopes, request", id);

        try {
            forbiddenScopesToPostAndDelete(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id, scope.getScopeId());
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final boolean tokenExists = service.existsById(id);

        if (tokenExists) {
            try {
                service.addScopeToToken(id, scope.getScopeId());
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, HttpStatus.CREATED.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.CREATED);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scopes, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/scopes/{scopeid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdScopeById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String scopeid) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} scope By Id {1}, request", new Object[]{id, scopeid});

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final boolean tokenExists = service.existsById(id);

        if (tokenExists) {
            Optional<Scope> scope = service.getScopeIncludingToken(id, scopeid);
            if (scope.isPresent()) {
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(scope.get(), HttpStatus.OK);
            }
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/{id}/scopes/{scopeid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDeleteScope(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String scopeid) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} scope By Id {1}, request", new Object[]{id, scopeid});

        try {
            forbiddenScopesToPostAndDelete(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id, scopeid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final boolean tokenExists = service.existsById(id);

        if (tokenExists) {
            try {
                service.removeScopeFromToken(id, scopeid);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, HttpStatus.FORBIDDEN.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} scope By Id {1}, request http status: {2}", new Object[]{id, scopeid, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/resident", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetResident(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} resident, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} resident, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} resident, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(token.get().getResidentId(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} resident, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/resident", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPostResident(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull Resident resident) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} resident, request", id);

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} resident, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            try {
                service.addTokenToResident(id, resident.getResidentId());
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} resident, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} resident, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} resident, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/{id}/resident/{residentid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDeleteResident(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String residentid) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} resident By Id {1}, request", new Object[]{id, residentid});

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} resident By Id {1}, request http status: {2}", new Object[]{id, residentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            try {
                service.removeResidentFromToken(id, residentid);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} resident By Id {1}, request http status: {2}", new Object[]{id, residentid, HttpStatus.NO_CONTENT.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} resident By Id {1}, request http status: {2}", new Object[]{id, residentid, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} resident By Id {1}, request http status: {2}", new Object[]{id, residentid, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} user By Id {1}, request", new Object[]{id, userid});

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} user By Id {1}, request http status: {2}", new Object[]{id, userid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Token> token = service.findById(id);

        if (token.isPresent()) {
            try {
                service.removeUserFromToken(id, userid);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} user By Id {1}, request http status: {2}", new Object[]{id, userid, HttpStatus.NO_CONTENT.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} user By Id {1}, request http status: {2}", new Object[]{id, userid, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} user By Id {1}, request http status: {2}", new Object[]{id, userid, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Token model) {
        if (Optional.ofNullable(model.getKey()).isEmpty()) {
            model.setKey(service.generateTokenKey(NFLAT_TOKEN_PREFIX));
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

        if (Optional.ofNullable(model.getFlat()).isPresent()) {
            requirement.setAttribute(Attribute.FLAT_ID, model.getFlat().getFlatId());
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            for (Token token : service.getUserTokens(userUid)) {
                if (token.getTokenId().equals(entityId)) {
                    return;
                }
            }

            Optional<Token> token = service.findById(entityId);

            if (token.isPresent()) {
                if (Optional.ofNullable(token.get().getFlat()).isPresent()) {
                    flatController.securityFlatOwner(scopes, userUid, token.get().getFlat().getFlatId());
                }
            } else {
                throw new ForbiddenException();
            }
        }
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Token model) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            //Accessible if only if user owns scope (create token) for required flat
            if (Optional.ofNullable(model.getFlat()).isPresent()) {
                flatController.securityFlatCreateToken(scopes, userUid, model.getFlat().getFlatId());
            } else {
                throw new ForbiddenException();
            }
        }
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Token model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            flatController.securityFlatOwner(scopes, userUid, entityId);
        }
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    public void forbiddenScopesToPostAndDelete(List<String> scopes, String userUid, String tokenId, String scopeId) {

        if (!scopes.contains(applicationMainScope)) {
            Optional<Token> token = service.findById(tokenId);
            if (token.isPresent()) {
                if (Optional.ofNullable(token.get().getFlat()).isPresent()) {
                    crudCreateSecurity(scopes, userUid, token.get());
                }
            } else {
                throw new ForbiddenException();
            }

            if (Optional.ofNullable(scopeId).isPresent()) {
                Optional<Scope> scope = service.getScopeService().findById(scopeId);
                if (scope.isPresent()) {
                    for (ScopeEnum forbiddenScope : service.getScopeService().forbiddenScopesForToken()) {
                        if (scope.get().getScope().equals(forbiddenScope.getScopeName())) {
                            throw new ForbiddenException();
                        }
                    }

                    for (ScopeEnum forbiddenScope : service.forbiddenPostScopesForToken()) {
                        if (scope.get().getScope().equals(forbiddenScope.getScopeName())) {
                            throw new ForbiddenException();
                        }
                    }

                    try {
                        flatController.securityFlatOwner(scopes, userUid, token.get().getFlat().getFlatId());
                    } catch (SecurityException exception) {
                        if (scope.get().getScope().equals(ScopeEnum.SCOPE_CREATE_TOKEN.getScopeName())) {
                            throw new ForbiddenException();
                        }
                    }
                }
            }
        }
    }
}
