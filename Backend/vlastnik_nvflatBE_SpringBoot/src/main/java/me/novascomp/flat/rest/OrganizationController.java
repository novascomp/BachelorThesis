package me.novascomp.flat.rest;

import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import static me.novascomp.utils.rest.GeneralController.exceptionToHttpStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.flat.config.BeansInit;
import me.novascomp.flat.model.Detail;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.MessageRecordNvm;
import me.novascomp.flat.model.Organization;
import me.novascomp.flat.service.DetailService;
import me.novascomp.flat.service.MessageRecordNvmService;
import me.novascomp.flat.service.OrganizationService;
import me.novascomp.flat.service.UserService;
import me.novascomp.home.flat.uploader.FlatUploader;
import me.novascomp.microservice.nvm.model.CategoryHierarchy;
import me.novascomp.microservice.nvm.model.CreateDocumentSecurity;
import me.novascomp.microservice.nvm.model.LightweightCategory;
import me.novascomp.microservice.nvm.model.LightweightComponent;
import me.novascomp.microservice.nvm.model.LightweightMessage;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/organizations")
public class OrganizationController extends GeneralController<Organization, OrganizationService> {

    private final long MAX_DOCUMENT_UPLOAD_SIZE = 35000000; // 35 MB

    private final FlatController flatController;
    private final TokenController tokenController;
    private final ScopeController scopeController;
    private final DetailService detailService;
    private final MessageRecordNvmService documentService;
    private final Map<String, CreateDocumentSecurity> documentCreateSecurity;

    @Autowired
    public OrganizationController(FlatController flatController, TokenController tokenController, ScopeController scopeController, DetailService detailService, MessageRecordNvmService documentService) {
        this.flatController = flatController;
        this.tokenController = tokenController;
        this.scopeController = scopeController;
        this.detailService = detailService;
        this.documentService = documentService;
        this.documentCreateSecurity = new HashMap<>();
    }

    @GetMapping(value = "/{id}/flats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIcoFlats(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} flats, request", id);

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {
            try {
                Page<?> flats = flatController.getService().findByOrganization(organization.get(), FlatController.DEFAULT_FLAT_NAME, pageable);
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(flats, HttpStatus.OK);
            } catch (PropertyReferenceException exception) {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.BAD_REQUEST.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/flats")
    public ResponseEntity<?> getEntityByIdPostFlats(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull FlatUploader flats) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} flats, request", id);

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            service.createOrganizatinFlats(flatController.getService(), detailService, tokenController.getService(), scopeController.getService(), documentService, id, flats);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.CREATED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} flats, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }

    }

    @DeleteMapping(value = "/{id}/flats")
    public ResponseEntity<?> getEntityByIdDeleteFlats(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} flats, request", new Object[]{id});

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            flatController.getService().deleteAllOrganizationFlats(id, documentService);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/flats/byidentifier/{flatidentifier}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatByIdentifier(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatidentifier) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} flat By Identifier {1}, request", new Object[]{id, flatidentifier});

        try {
            forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat By Identifier {1}, request http status: {2}", new Object[]{id, flatidentifier, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Organization> organization = service.findById(id);
        if (organization.isPresent()) {
            Optional<Flat> flat = flatController.getService().findByIdentifierAndOrganization(flatidentifier, organization.get());
            if (flat.isPresent()) {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat By Identifier {1}, request http status: {2}", new Object[]{id, flatidentifier, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(flat.get(), HttpStatus.OK);
            }
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flat By Identifier {1}, request http status: {2}", new Object[]{id, flatidentifier, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/documents/byflats/{flatid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdAndComponentsPost(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid, @RequestBody @NotNull List<LightweightCategory> categories, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and documents By Flat Id: {1}, request", new Object[]{id, flatid});

        try {
            byOrganizationMembership(principal, id);
            flatController.securityFlatOwner(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), flatid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and documents By Flat Id: {1}, request http status:", new Object[]{id, flatid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Flat flat = flatController.getService().findById(flatid).get();
            List<LightweightCategory> primaryCategories = new ArrayList<>();
            primaryCategories.add(documentService.getComponentByText(id, flat.getIdentifier()));
            CategoryHierarchy categoryHierarchy = new CategoryHierarchy(primaryCategories, categories);
            Page<?> microserviceResponse = documentService.getDocumentsByCategoriesHierarchy(id, categoryHierarchy, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and documents By Flat Id: {1}, request http status:", new Object[]{id, flatid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(microserviceResponse, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and documents By Flat Id: {1}, request http status:", new Object[]{id, flatid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/{documentid}/contents/{contentid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdContentById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @PathVariable @NotNull String contentid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and document By Id: {1} and contents By Id: {2}, request", new Object[]{id, documentid, contentid});

        try {
            byOrganizationMembership(principal, id);
            messageReadByTokenHoldingSecurity(principal, documentid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} and contents By Id: {2}, request http status: {3}", new Object[]{id, documentid, contentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getDocumentByIdContentById(id, documentid, contentid, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} and contents By Id: {2}, request http status: {3}", new Object[]{id, documentid, contentid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} and contents By Id: {2}, request http status: {3}", new Object[]{id, documentid, contentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/components", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentsComponents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} documents components, request", id);

        try {
            byOrganizationMembership(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents components, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getDocumentsComponents(id, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents components, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents components, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/components/personal/{detailid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentsComponentsPersonal(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String detailid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} documents components personal, request", id);

        try {
            byOrganizationMembership(principal, id);
            userDetailSecurtiy(principal, detailid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents components personal, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            List<Flat> userFlats = documentService.getAvailableFlatIdentifiersUsedInDocuments(detailid);
            List<LightweightCategory> components = new ArrayList<>();
            userFlats.forEach((flat) -> {
                components.add(documentService.getComponentByText(id, flat.getIdentifier()));
            });
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents components personal, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(getPage(components, pageable), HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents components personal, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/{documentid}/components", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdComponents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} document By Id: {1} components, request", new Object[]{id, documentid});

        try {
            byOrganizationMembership(principal, id);
            messageReadByTokenHoldingSecurity(principal, documentid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getComponentsByDocumentId(id, documentid, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/{documentid}/contents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdContents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} document By Id: {1} contents, request", new Object[]{id, documentid});

        try {
            byOrganizationMembership(principal, id);
            messageReadByTokenHoldingSecurity(principal, documentid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getDocumentByIdContents(id, documentid, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPostDocument(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull LightweightMessage lightweightMessage, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} documents, request", id);

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_DOCUMENT, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationMembership(principal, id);
            userDetailSecurtiy(principal, lightweightMessage.getDetailId());
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            String documentId = service.createDocumentByFlatRequest(principal, detailService, documentService, id, lightweightMessage);
            this.documentCreateSecurity.put(recaptcha_token, new CreateDocumentSecurity(new Date(), documentId));
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request", id);
            return getResponseForPost(Optional.ofNullable(documentId));
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents/{documentid}/components", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdPostComponent(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @RequestBody @NotNull LightweightComponent componentBody, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} document By Id: {1} components, request", new Object[]{id, documentid});

        try {
            documentModifySecurity(UserService.getUserScopesPrincipal(principal), documentid, recaptcha_token);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            byOrganizationMembership(principal, id);
            messageReadByTokenHoldingSecurity(principal, documentid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            service.addRequestedCategory(flatController.getService(), detailService, documentService, id, documentid, componentBody);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} document By Id: {1} components, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }

    }

    @PostMapping(value = "/{id}/documents/{documentid}/contents")
    public ResponseEntity<?> getEntityByIdPostFileMultipartForm(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @RequestParam("file") @NotNull MultipartFile file, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} document By Id: {1} contents, request", new Object[]{id, documentid});

        if (file.getSize() > MAX_DOCUMENT_UPLOAD_SIZE) {
            LOG.log(Level.INFO, "Max upload file size exceeded: {0}", file.getSize());
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            documentModifySecurity(UserService.getUserScopesPrincipal(principal), documentid, recaptcha_token);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            byOrganizationMembership(principal, id);
            messageReadByTokenHoldingSecurity(principal, documentid);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Optional<String> contentId = documentService.addContentToDocument(documentid, file);

            if (contentId.isPresent()) {
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.CREATED.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return createdStatus(HttpStatus.CREATED, contentId.get());
            }

        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.INTERNAL_SERVER_ERROR.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(value = "/{id}/documents/{documentid}/details/{detailid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdDelete(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @PathVariable @NotNull String detailid, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} and document By Id: {1} and Detail By Id: {2}, request", new Object[]{id, documentid, detailid});

        boolean force = false;
        try {
            documentModifySecurity(UserService.getUserScopesPrincipal(principal), documentid, recaptcha_token);
            force = true;
        } catch (SecurityException exception) {
            try {
                RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_DOCUMENT, RestUtils.RECAPTCHA_SCORE_0_7);
            } catch (SecurityException securityException) {
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and document By Id: {1} and Detail By Id: {2}, request http status: {3}", new Object[]{id, documentid, detailid, HttpStatus.FORBIDDEN.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.LOCKED);
            }

            try {
                userDetailSecurtiy(principal, detailid);
            } catch (SecurityException exception2) {
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and document By Id: {1} and Detail By Id: {2}, request http status: {3}", new Object[]{id, documentid, detailid, HttpStatus.FORBIDDEN.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        try {
            documentService.deleteDocumentById(detailService, id, documentid, detailid, force);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and document By Id: {1} and Detail By Id: {2}, request http status: {3}", new Object[]{id, documentid, detailid, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and document By Id: {1} and Detail By Id: {2}, request http status: {3}", new Object[]{id, documentid, detailid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @Override
    public void fillInRequirementCrudCreate(Requirement requirement, Organization model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.ORGANIZATION_ID, model.getOrganizationId());
    }

    @Override
    public void fillInRequirementCrudUpdate(Requirement requirement, Organization model) {
        fillInCommonCrudAttributes(requirement, model);
        requirement.setAttribute(Attribute.ORGANIZATION_ID, model.getOrganizationId());
    }

    @Override
    protected void fillInCommonCrudAttributes(Requirement requirement, Organization model) {
        requirement.setAttribute(Attribute.ICO, model.getIco());
    }

    public void readOrganizationFlatsSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void userDetailSecurtiy(@AuthenticationPrincipal Jwt principal, String detailId) throws SecurityException {

        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            List<String> userDetails = detailService.getUserDetailsByTokenHolding(tokenController.getService(), UserService.getUserUidByPrincipal(principal));

            if (userDetails.contains(detailId)) {
                return;
            }

            throw new ForbiddenException();
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void byOrganizationMembership(@AuthenticationPrincipal Jwt principal, String organizationId) throws SecurityException {
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            for (Organization organization : flatController.getService().getUserOrganizationsByTokenHoldingPageable(UserService.getUserUidByPrincipal(principal), PageRequest.of(0, Integer.MAX_VALUE)).getContent()) {
                if (organization.getOrganizationId().equals(organizationId)) {
                    return;
                }
            }
            throw new ForbiddenException();
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void messageReadByTokenHoldingSecurity(Jwt principal, String messageid) throws SecurityException {

        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            Optional<MessageRecordNvm> document = documentService.findById(messageid);

            if (document.isPresent()) {
                List<String> userDetails = detailService.getUserDetailsByTokenHolding(tokenController.getService(), UserService.getUserUidByPrincipal(principal));
                List<MessageRecordNvm> messages = new ArrayList<>();
                messages.add(document.get());
                List<String> messageDetails = new ArrayList<>();
                for (Detail detail : detailService.findDistinctByMessageRecordNvmListIn(messages)) {
                    messageDetails.add(detail.getDetailId());
                }

                messageDetails.retainAll(userDetails);
                if (userDetails.isEmpty() == false) {
                    return;
                }
            }
            throw new ForbiddenException();
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Organization model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Organization model) throws SecurityException {
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

    private ResponseEntity<?> getResponseForPost(Optional<String> idOptional) {
        if (idOptional.isPresent()) {
            return createdStatus(HttpStatus.CREATED, idOptional.get());
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private void documentModifySecurity(List<String> scopes, String documentId, String recaptcha_token) {
        if (!scopes.contains(applicationMainScope)) {
            Optional<CreateDocumentSecurity> createDocumentSecurity = Optional.ofNullable(this.documentCreateSecurity.get(recaptcha_token));
            if (createDocumentSecurity.isPresent()) {
                if (createDocumentSecurity.get().getDocumentId().equals(documentId)) {
                    if (createDocumentSecurity.get().getCreateRequestDate().getTime() + 40000 > System.currentTimeMillis()) {
                        return;
                    }
                }
            }
            throw new ForbiddenException();
        }
    }
}
