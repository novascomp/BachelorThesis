package me.novascomp.utils.rest;

import com.sun.istack.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import me.novascomp.home.config.BeansInit;
import me.novascomp.home.service.UserService;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.components.GeneralUpdateResponse;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.standalone.service.exceptions.CreatedException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.OKException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

public abstract class GeneralController<Model, Service extends GeneralService> {

    @Autowired
    protected Service service;

    @Autowired
    @Qualifier("applicationMainScope")
    protected String applicationMainScope;

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0}, request", id);

        try {
            crudReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Model> entity = service.findById(id);
        if (entity.isEmpty()) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(entity.get(), HttpStatus.OK);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Model model, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity, request");

        try {
            crudCreateSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), model);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Requirement requirement = service.getRequirementCrudCreate();
        fillInRequirementCrudCreate(requirement, model);
        GeneralCreateResponse response = service.createModel(requirement);
        LOG.log(Level.INFO, response.toString());

        if (response.isSuccessful()) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.CREATED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return createdStatus(HttpStatus.CREATED, service.getModelId((Model) response.getModel().get()));
        } else {
            if (response.getBusinessRule().isFoundInDatabase()) {
                LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.CONFLICT.toString());
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.BAD_REQUEST.toString());
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(response.getRequirement(), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Model model, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting UPDATE Entity, request");

        try {
            crudUpdateSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), model);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Requirement requirement = service.getRequirementCrudUdate();
        fillInRequirementCrudUpdate(requirement, model);
        GeneralUpdateResponse generalUpdateResponse = service.updateModel(requirement);
        LOG.log(Level.INFO, generalUpdateResponse.toString());

        if (generalUpdateResponse.isSuccessful()) {
            LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.NO_CONTENT.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            if (generalUpdateResponse.getBusinessRule().isIdOk() == false) {
                LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.NOT_FOUND.toString());
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(generalUpdateResponse.getRequirement(), HttpStatus.NOT_FOUND);
            }

            if (generalUpdateResponse.getBusinessRule().isConflict()) {
                LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.CONFLICT.toString());
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(generalUpdateResponse.getRequirement(), HttpStatus.CONFLICT);
            }
        }

        LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.BAD_REQUEST.toString());
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(generalUpdateResponse.getRequirement(), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0}, request http status: ", id);

        try {
            crudDeleteSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Model> entity = service.findById(id);
        if (entity.isEmpty()) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.delete(entity.get());
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(entity.get(), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@AuthenticationPrincipal Jwt principal, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET All, request");

        try {
            crudAllReadSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "Starting GET All, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LOG.log(Level.INFO, "Starting GET All, request http status: {0}", HttpStatus.OK.toString());
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(service.findAll(pageable), HttpStatus.OK);
    }

    public static HttpStatus exceptionToHttpStatusCode(ServiceException exception) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (exception instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        if (exception instanceof ConflictException) {
            httpStatus = HttpStatus.CONFLICT;
        }

        if (exception instanceof InternalException) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (exception instanceof CreatedException) {
            httpStatus = HttpStatus.CREATED;
        }

        if (exception instanceof OKException) {
            httpStatus = HttpStatus.OK;
        }

        if (exception instanceof MicroserviceConnectionException) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        }

        return httpStatus;
    }

    protected ResponseEntity<?> createdStatus(HttpStatus httpStatus, String id) {
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", id);
        return new ResponseEntity<>(headers, httpStatus);
    }

    public Service getService() {
        return service;
    }

    protected void forbiddenToAllExpectForAppliactionMainScope(List<String> scopes, String userUid) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            throw new ForbiddenException();
        }
    }

    protected Page<?> getPage(List<?> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > list.size() ? list.size() : (start + pageable.getPageSize());
        Page<?> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return page;
    }

    protected abstract void fillInRequirementCrudCreate(Requirement requirement, Model model);

    protected abstract void fillInRequirementCrudUpdate(Requirement requirement, Model model);

    protected abstract void fillInCommonCrudAttributes(Requirement requirement, Model model);

    protected abstract void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException;

    protected abstract void crudCreateSecurity(List<String> scopes, String userUid, Model model) throws SecurityException;

    protected abstract void crudUpdateSecurity(List<String> scopes, String userUid, Model model) throws SecurityException;

    protected abstract void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException;

    protected abstract void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException;

}
