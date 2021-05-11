package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.novascomp.flat.config.BeansInit;
import me.novascomp.flat.model.Detail;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.model.Resident;
import me.novascomp.flat.service.ResidentService;
import me.novascomp.flat.service.UserService;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@RestController
@RequestMapping("/residents")
public class ResidentController extends GeneralController<Resident, ResidentService> {

    private final FlatController flatController;
    private final DetailController detailController;

    @Autowired
    public ResidentController(FlatController flatController, DetailController detailController) {
        this.flatController = flatController;
        this.detailController = detailController;
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {

        try {
            crudReadOrUpdateOrDeleteSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return super.getEntityById(principal, id);
    }

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Resident model, @RequestParam(required = false) String recaptcha_token) {

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_PERSON, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.LOCKED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            this.crudCreateSecurity(principal, model.getRequiredFlatDetail());
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return super.postEntity(principal, model, recaptcha_token);
    }

    @Override
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Resident model, @RequestParam(required = false) String recaptcha_token) {

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_PERSON, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.LOCKED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            this.crudReadOrUpdateOrDeleteSecurity(principal, model.getResidentId());
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.FORBIDDEN.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return super.updateEntity(principal, model, recaptcha_token);
    }

    @Override
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0}, request", id);

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_PERSON, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            this.crudReadOrUpdateOrDeleteSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            service.removeResidentFromAllDetails(id);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ServiceException ex) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(ex);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }

    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetResidents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Resident> resident = service.findById(id);

        if (resident.isPresent()) {
            return new ResponseEntity<>(resident.get().getTokenList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Resident model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.DETAIL_ID, model.getRequiredFlatDetail());
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, Resident model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.RESIDET_ID, model.getResidentId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Resident model) {
        requirement.setAttribute(Attribute.FIRST_NAME, model.getFirstName());
        requirement.setAttribute(Attribute.LAST_NAME, model.getLastName());
        requirement.setAttribute(Attribute.EMAIL, model.getEmail());
        requirement.setAttribute(Attribute.PHONE_NUMBER, model.getPhone());
        requirement.setAttribute(Attribute.DATE_OF_BIRTH, model.getDateOfBirth());
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void crudCreateSecurity(@AuthenticationPrincipal Jwt principal, String detailId) throws SecurityException {
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            if (detailController.getService().getUserDetailsByTokenHolding(flatController.getTokenService(), UserService.getUserUidByPrincipal(principal)).contains(detailId)) {
                return;
            }
            throw new ForbiddenException("");
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void crudReadOrUpdateOrDeleteSecurity(@AuthenticationPrincipal Jwt principal, String residentId) throws SecurityException {

        boolean sucess = false;
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            final Optional<Resident> entity = service.findById(residentId);
            List<Resident> residents = new ArrayList<>();

            if (entity.isPresent()) {
                residents.add(entity.get());
            }

            for (Detail detail : detailController.getService().findByResidentListIn(residents)) {
                try {
                    this.crudCreateSecurity(principal, detail.getDetailId());
                    sucess = true;
                } catch (SecurityException exception) {

                }
            }

            if (!sucess) {
                throw new ForbiddenException("");
            }
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Resident model) throws SecurityException {
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Resident model) throws SecurityException {
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }
}
