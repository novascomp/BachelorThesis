package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import me.novascomp.flat.config.BeansInit;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.config.ScopeEnum;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.Organization;
import me.novascomp.flat.service.FlatService;
import me.novascomp.flat.service.MessageRecordNvmService;
import me.novascomp.flat.service.ResidentService;
import me.novascomp.flat.service.TokenService;
import me.novascomp.flat.service.UserService;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/flats")
public class FlatController extends GeneralController<Flat, FlatService> {

    private final TokenService tokenService;
    private final ResidentService residentService;
    private final MessageRecordNvmService messageRecordNvmService;
    public static final String DEFAULT_FLAT_NAME = "VÝBOR";

    @Autowired
    public FlatController(TokenService tokenService, ResidentService residentService, MessageRecordNvmService messageRecordNvmService) {
        this.tokenService = tokenService;
        this.residentService = residentService;
        this.messageRecordNvmService = messageRecordNvmService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<?> postEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Flat flat, @RequestParam(required = false) String recaptcha_token) {

        try {
            crudCreateSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), flat);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResponseEntity<?> response = super.postEntity(principal, flat, recaptcha_token);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            return response;
        }

        try {
            LOG.log(Level.INFO, "Creating flat component in NVM: {0}", flat.getIdentifier());
            service.createFlatComponentInNvmMicroservice(flat.getOrganization().getOrganizationId(), flat.getIdentifier(), messageRecordNvmService);
            LOG.log(Level.INFO, "DONE Creating flat component in NVM: {0}", flat.getIdentifier());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return response;
        } catch (ServiceException exception) {
            //Rollback
            String[] path = response.getHeaders().getFirst((HttpHeaders.LOCATION)).split("/");
            String flatId = path[path.length - 1];
            LOG.log(Level.INFO, "Rollback POST entity ID: {0}", flat.getIdentifier());
            service.delete(service.findById(flatId).get());
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE Rollback POST entity ID: {0}", flat.getIdentifier());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<?> deleteEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0}, request http status: ", id);

        try {
            crudDeleteSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Flat> entity = service.findById(id);
        if (entity.isEmpty()) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            tokenService.removeAllTokensContainingFlat(Optional.ofNullable(id));
            service.delete(entity.get());
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(entity.get(), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping(value = "/{id}/organization", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetOrganization(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} organization, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} organization, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Flat> flat = service.findById(id);

        if (flat.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} organization, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(flat.get().getOrganization(), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} organization, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetDetail(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} detail, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} detail, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Flat> flat = service.findById(id);

        if (flat.isPresent()) {
            if (flat.get().getDetailList().size() == 1) {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} detail, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(flat.get().getDetailList().get(0), HttpStatus.OK);
            } else {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} detail, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(flat.get().getDetailList(), HttpStatus.OK);
            }
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} detail, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/residents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetResidents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} residents, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} residents, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Flat> flat = service.findById(id);

        if (flat.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} residents, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(residentService.findDistinctByDetailListIn(flat.get().getDetailList(), pageable), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} residents, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetTokens(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} tokens, request", id);

        try {
            securityFlatOwner(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Flat> flat = service.findById(id);

        if (flat.isPresent()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(tokenService.findByFlat(flat.get(), pageable), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Flat model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, Flat model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.FLAT_ID, model.getFlatId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Flat model) {
        requirement.setAttribute(Attribute.FLAT_IDENTIFIER, model.getIdentifier());

        if (Optional.ofNullable(model.getOrganization()).isPresent()) {
            requirement.setAttribute(Attribute.ORGANIZATION_ID, model.getOrganization().getOrganizationId());
        }
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void securityFlatOwner(List<String> scopes, String userUid, String flatId) throws SecurityException {
        securityFlatByToken(scopes, userUid, flatId, ScopeEnum.SCOPE_FLAT_OWNER);
    }

    public void securityFlatCreateToken(List<String> scopes, String userUid, String flatId) throws SecurityException {
        securityFlatByToken(scopes, userUid, flatId, ScopeEnum.SCOPE_CREATE_TOKEN);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void securityFlatByToken(List<String> scopes, String userUid, String flatId, ScopeEnum scope) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            List<Flat> userFlats = service.getUserFlatsByScope(userUid, scope.getScopeName());
            for (Flat flat : userFlats) {
                if (flat.getFlatId().equals(flatId)) {
                    return;
                }
            }
            throw new ForbiddenException();
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            List<Flat> flats = service.getUserFlatsByTokenHolding(userUid);
            for (Flat flat : flats) {
                if (flat.getFlatId().equals(entityId)) {
                    return;
                }
            }
            throw new ForbiddenException();
        }
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Flat model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Flat model) throws SecurityException {
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

    @GetMapping(value = "/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserFlats(@AuthenticationPrincipal Jwt principal, Pageable pageable) {
        try {
            Page<Flat> flats = service.getUserFlatsByTokenHoldingPageable(UserService.getUserUidByPrincipal(principal), pageable);
            return new ResponseEntity<>(flats, HttpStatus.OK);
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        }
    }

    @GetMapping(value = "/personal/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserOrganizations(@AuthenticationPrincipal Jwt principal, Pageable pageable) {
        try {
            Page<Organization> userOrganizations = service.getUserOrganizationsByTokenHoldingPageable(UserService.getUserUidByPrincipal(principal), pageable);
            return new ResponseEntity<>(userOrganizations, HttpStatus.OK);
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        }
    }
}
