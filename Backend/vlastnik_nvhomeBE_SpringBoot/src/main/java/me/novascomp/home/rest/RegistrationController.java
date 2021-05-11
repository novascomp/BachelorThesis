package me.novascomp.home.rest;

import ares.vr.fe.AresVrForFEPruposes;
import com.sun.istack.NotNull;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Qualifier;
import me.novascomp.home.config.BeansInit;
import me.novascomp.home.model.Organization;
import me.novascomp.home.service.RegistrationService;
import me.novascomp.home.service.UserService;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import static me.novascomp.utils.rest.GeneralController.exceptionToHttpStatusCode;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@RestController
@RequestMapping("/registration")
public class RegistrationController {

    private final RegistrationService service;

    private static final Logger LOG = Logger.getLogger(RegistrationController.class.getName());

    @Autowired
    @Qualifier("applicationMainScope")
    protected String applicationMainScope;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.service = registrationService;
    }

    @PostMapping(value = "/ares", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrganizationNameFromAres(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Organization organization) {
        LOG.log(Level.INFO, "Starting GET Ares data, request");

        if (Optional.ofNullable(organization.getIco()).isEmpty()) {
            LOG.log(Level.INFO, "DONE GET Ares data, request http status: {0}", HttpStatus.BAD_REQUEST.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        AresVrForFEPruposes aresVrForFEPruposes = new AresVrForFEPruposes();

        try {
            aresVrForFEPruposes = new AresVrForFEPruposes(service.getAresResponse(organization.getIco()).getOrganizationName());
            LOG.log(Level.INFO, "DONE GET Ares data, request http status: {0}", HttpStatus.OK.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(aresVrForFEPruposes, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Ares data, exception http status: {0}", httpStatus.toString());
            LOG.log(Level.INFO, "DONE GET Ares data, request http status: {0}", HttpStatus.OK);
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(aresVrForFEPruposes, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/verifyico", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyIco(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Organization organization, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting Verifying ICO: {0}, request", organization.getIco());

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_VERIFY_ICO_ACTION, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Verifying ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        if (Optional.ofNullable(organization.getIco()).isEmpty()) {
            LOG.log(Level.INFO, "DONE Verifying ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.BAD_REQUEST.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (service.existOrganization(organization.getIco())) {
            LOG.log(Level.INFO, "DONE Verifying ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.CONFLICT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        try {
            if (service.checkOrganizationValidity(organization.getIco()) == false) {
                LOG.log(Level.INFO, "DONE Verifying ICO: {0} not in SVJ register, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.FORBIDDEN.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            LOG.log(Level.INFO, "DONE Verifying ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE Verifying ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerOrganization(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Organization organization, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting Organization registration ICO: {0}, request", organization.getIco());

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_REGISTER_ORGANIZATION, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Organization registration ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.LOCKED.toString()});
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            String organizationId = service.registerNewOrganization(organization.getIco(), principal);
            final HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, organizationId);
            LOG.log(Level.INFO, "Organization registration ICO: {0} successfully", organization.getIco());
            LOG.log(Level.INFO, "Organization registration new organization ID: {0}", organizationId);
            LOG.log(Level.INFO, "DONE Organization registration ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "Registration ICO: {0} unsuccessfully", organization.getIco());
            LOG.log(Level.INFO, "DONE Organization registration ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Organization registration ICO: {0}, request http status: {1}", new Object[]{organization.getIco(), HttpStatus.FORBIDDEN.toString()});
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    public RegistrationService getService() {
        return service;
    }

}
