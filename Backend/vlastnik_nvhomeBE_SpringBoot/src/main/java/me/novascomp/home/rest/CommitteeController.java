package me.novascomp.home.rest;

import com.sun.istack.NotNull;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import me.novascomp.home.config.BeansInit;
import me.novascomp.home.model.Committee;
import me.novascomp.home.model.Organization;
import me.novascomp.home.service.CommitteeService;
import me.novascomp.home.service.MemberService;
import me.novascomp.home.service.UserService;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@RestController
@RequestMapping("/committees")
public class CommitteeController extends GeneralController<Committee, CommitteeService> {

    private final OrganizationController organizationController;
    private final MemberService memberService;

    @Autowired
    public CommitteeController(OrganizationController organizationController, MemberService memberService) {
        this.organizationController = organizationController;
        this.memberService = memberService;
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {

        try {
            crudReadSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return super.getEntityById(principal, id);
    }

    @Override
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEntity(@AuthenticationPrincipal Jwt principal, @RequestBody @NotNull Committee model, @RequestParam(required = false) String recaptcha_token) {

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_COMMITTEE, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE UPDATE Entity, request http status: {0}", HttpStatus.LOCKED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            this.crudUpdateOrDeleteSecurity(principal, model.getCommitteeId());
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

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_COMMITTEE, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            this.crudUpdateOrDeleteSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0}, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return super.deleteEntityById(principal, id, recaptcha_token);
    }

    @GetMapping(value = "/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdGetResidents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} members, request", id);

        try {
            crudReadSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} members, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Committee> committee = service.findById(id);

        if (committee.isPresent()) {
            List list = new ArrayList<>();
            list.add(committee.get());
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} members, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(service.getCommitteMembers(list, pageable, memberService), HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} members, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    protected void fillInRequirementCrudCreate(Requirement requirement, Committee model) {
        fillInCommonCrudAttributes(requirement, model);
    }

    @Override
    protected void fillInRequirementCrudUpdate(Requirement requirement, Committee model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.COMMITTEE_ID, model.getCommitteeId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Committee model) {
        requirement.setAttribute(Attribute.EMAIL, model.getEmail());
        requirement.setAttribute(Attribute.PHONE_NUMBER, model.getPhone());

        if (Optional.ofNullable(model.getOrganization()).isPresent()) {
            requirement.setAttribute(Attribute.ORGANIZATION_ID, model.getOrganization().getOrganizationId());
        }
    }

    public void crudReadSecurity(@AuthenticationPrincipal Jwt principal, String committeeId) throws SecurityException {

        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            final Optional<Committee> committee = service.findById(committeeId);

            if (committee.isEmpty()) {
                throw new ForbiddenException("");
            } else {
                String organizationId = committee.get().getOrganization().getOrganizationId();

                if (Optional.ofNullable(organizationId).isPresent()) {
                    organizationController.byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, organizationId);
                } else {
                    throw new ForbiddenException("");
                }
            }
        }
    }

    public void crudCreateSecurity(@AuthenticationPrincipal Jwt principal, String organizationId) throws SecurityException {
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            final Optional<Organization> organization = organizationController.getService().findById(organizationId);
            if (organization.isEmpty()) {
                throw new ForbiddenException("");
            } else {
                if (organization.isPresent()) {
                    organizationController.byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), organization.get().getOrganizationId());
                } else {
                    throw new ForbiddenException("");
                }
            }
        }
    }

    public void crudUpdateOrDeleteSecurity(@AuthenticationPrincipal Jwt principal, String committeeId) {

        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            final Optional<Committee> committee = service.findById(committeeId);

            if (committee.isEmpty()) {
                throw new ForbiddenException("");
            } else {
                String organizationId = null;

                if (committee.isPresent()) {
                    organizationId = committee.get().getOrganization().getOrganizationId();
                }

                if (Optional.ofNullable(organizationId).isPresent()) {
                    organizationController.byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), organizationId);
                } else {
                    throw new ForbiddenException("");
                }
            }
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Committee model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Committee model) throws SecurityException {
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }
}
