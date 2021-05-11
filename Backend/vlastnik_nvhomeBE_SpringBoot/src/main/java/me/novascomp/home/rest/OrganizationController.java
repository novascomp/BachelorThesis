package me.novascomp.home.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.home.config.BeansInit;
import me.novascomp.utils.rest.GeneralController;
import me.novascomp.home.flat.uploader.FlatGenerator;
import me.novascomp.home.model.Organization;
import me.novascomp.home.service.FileService;
import me.novascomp.home.service.OrganizationService;
import me.novascomp.home.service.RegistrationService;
import me.novascomp.home.service.TokenService;
import me.novascomp.home.service.UserService;
import me.novascomp.home.flat.uploader.FlatUploader;
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

@RestController
@RequestMapping("/organizations")
public class OrganizationController extends GeneralController<Organization, OrganizationService> {

    private final Integer MAX_FLATS_UPLOAD_LIMIT = 200;
    private final long MAX_FLAT_UPLOAD_SIZE = 15000000; // 15 MB
    private final long MAX_DOCUMENT_UPLOAD_SIZE = 35000000; // 35 MB

    private final TokenService tokenService;
    private final FileService documentService;
    private final ObjectMapper objectMapper;
    private final RegistrationService regitrationService;
    private final Map<String, CreateDocumentSecurity> documentCreateSecurity;

    @Autowired
    public OrganizationController(TokenService tokenService, FileService documentService, ObjectMapper objectMapper, RegistrationService regitrationService) {
        this.tokenService = tokenService;
        this.documentService = documentService;
        this.objectMapper = objectMapper;
        this.regitrationService = regitrationService;
        this.documentCreateSecurity = new HashMap<>();
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

        ResponseEntity responseEntity = super.getEntityById(principal, id);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            Organization organization = (Organization) responseEntity.getBody();
            regitrationService.appendAresData(organization);
        }
        return responseEntity;
    }

    @GetMapping(value = "/personal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserOrganizations(@AuthenticationPrincipal Jwt principal, Pageable pageable) {
        LOG.log(Level.INFO, "Starting Organization personal, request");

        Page<Organization> myOrganizations = service.findDistinctByTokenListIn(tokenService.getUserTokens(UserService.getUserUidByPrincipal(principal)), pageable);
        myOrganizations.getContent().forEach((organization) -> {
            regitrationService.appendAresData(organization);
        });

        LOG.log(Level.INFO, "DONE GET Organization personal, request http status: {0}", HttpStatus.OK.toString());
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(myOrganizations, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/committee", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdCommittee(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} committee, request", id);

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} committee, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {
            if (organization.get().getCommitteeList().size() == 1) {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} committee, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(organization.get().getCommitteeList().get(0), HttpStatus.OK);
            }
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} committee, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdTokens(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} tokens, request", id);

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {
            Page<?> page = tokenService.findByOrganization(organization.get(), pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} tokens, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //NVM MICROSERVICE / DOCUMENTS
    @GetMapping(value = "/{id}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocuments(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} documents, request", id);

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getDocuments(id, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents/bycategories", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentsByIdAndByCategoriesPost(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull List<LightweightCategory> categories, Pageable pageable) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} documents and By Categories, request", id);

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and By Categories, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<LightweightMessage> page = (Page<LightweightMessage>) documentService.getDocumentsByCategories(id, categories, pageable);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and By Categories, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and By Categories, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPostDocument(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull LightweightMessage lightweightMessage, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} documents, request", id);

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_DOCUMENT, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {
            try {
                Optional<String> documentId = documentService.createDocument(id, lightweightMessage.getHeading(), lightweightMessage.getBody());
                if (documentId.isPresent()) {
                    this.documentCreateSecurity.put(recaptcha_token, new CreateDocumentSecurity(new Date(), documentId.get()));
                }
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request", id);
                return getResponseForPost(documentId);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/documents/{documentid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} document By Id: {1}, request", new Object[]{id, documentid});

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            LightweightMessage document = documentService.getDocumentByDocumentId(id, documentid);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(document, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @DeleteMapping(value = "/{id}/documents/{documentid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdDelete(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} document By Id: {1}, request", new Object[]{id, documentid});

        try {
            documentModifySecurity(UserService.getUserScopesPrincipal(principal), documentid, recaptcha_token);
        } catch (SecurityException exception) {
            try {
                RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_DOCUMENT, RestUtils.RECAPTCHA_SCORE_0_7);
            } catch (SecurityException securityException) {
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, HttpStatus.NO_CONTENT.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.LOCKED);
            }
            try {
                byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
            } catch (SecurityException exception2) {
                LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        try {
            documentService.deleteDocumentById(id, documentid);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} document By Id: {1}, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    //NVM MICROSERVICE / DOCUMENTS COMPONENTS
    @GetMapping(value = "/{id}/documents/components/{component}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentsComponents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String component, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} documents and components By Name: {1}, request", new Object[]{id, component});

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents and components By Name: {1}, request http status: {2}", new Object[]{id, component, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getDocumentsComponents(id, component, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents and components By Name: {1}, request http status: {2}", new Object[]{id, component, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} documents and components By Name: {1}, request http status: {2}", new Object[]{id, component, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents/components/{component}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentsPostComponent(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String component, @RequestBody @NotNull LightweightComponent componentBody, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} documents and components By Name: {1}, request", new Object[]{id, component});

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_COMPONENT, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and components By Name: {1}, request http status: {2}", new Object[]{id, component, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and components By Name: {1}, request http status: {2}", new Object[]{id, component, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and components By Name: {1}", new Object[]{id, component});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return getResponseForPost(documentService.createComponent(id, component, componentBody.getText()));
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} documents and components By Name: {1}, request http status: {2}", new Object[]{id, component, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @DeleteMapping(value = "/{id}/documents/components/{component}/{componentid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentsComponentByIdDelete(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String component, @PathVariable @NotNull String componentid, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} documents and components By Name: {1} and Component By Id: {2}, request", new Object[]{id, component, componentid});

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_COMPONENT, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} documents and components By Name: {1} and Component By Id: {2}, request http status: {3}", new Object[]{id, component, componentid, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} documents and components By Name: {1} and Component By Id: {2}, request http status: {3}", new Object[]{id, component, componentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            documentService.deleteDocumentsComponentById(id, component, componentid);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} documents and components By Name: {1} and Component By Id: {2}, request http status: {3}", new Object[]{id, component, componentid, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} documents and components By Name: {1} and Component By Id: {2}, request http status: {3}", new Object[]{id, component, componentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} documents and components By Name: {1} and Component By Id: {2}, request http status: {3}", new Object[]{id, component, componentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/{documentid}/components/{component}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdComponents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @PathVariable @NotNull String component, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request", new Object[]{id, documentid, component});

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.getComponentsByDocumentId(id, documentid, component, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents/{documentid}/components/{component}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdPostComponent(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @PathVariable @NotNull String component, @RequestBody @NotNull LightweightComponent componentBody, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request", new Object[]{id, documentid, component});

        try {
            documentModifySecurity(UserService.getUserScopesPrincipal(principal), documentid, recaptcha_token);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            documentService.addComponentToDocument(id, component, documentid, componentBody.getComponentId());
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} and components By Name: {2}, request http status: {3}", new Object[]{id, documentid, component, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/documents/{documentid}/contents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdContents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and document By Id: {1} contents, request", new Object[]{id, documentid});

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = documentService.geDocumentByIdContents(id, documentid, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/documents/{documentid}/contents")
    public ResponseEntity<?> getEntityByIdPostFileMultipartForm(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @RequestParam("file") @NotNull MultipartFile file, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} and document By Id: {1} contents, request", new Object[]{id, documentid});

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
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Optional<String> contentId = documentService.addContentToDocument(id, documentid, file);
            if (contentId.isPresent()) {
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.CREATED.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return createdStatus(HttpStatus.CREATED, contentId.get());
            } else {
                LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and document By Id: {1} contents, request http status: {2}", new Object[]{id, documentid, HttpStatus.INTERNAL_SERVER_ERROR.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
    }

    @GetMapping(value = "/{id}/documents/{documentid}/contents/{contentid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdDocumentByIdContentById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String documentid, @PathVariable @NotNull String contentid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and document By Id: {1} and contents By Id: {2}, request", new Object[]{id, documentid, contentid});

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
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

    //NVFLAT MICROSERVICE / FLATS
    @GetMapping(value = "/{id}/default/flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdOrganizationFlat(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} default flat, request", id);

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} default flat, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {
            try {
                Optional<?> nvHome = regitrationService.getDefaultFlat(organization.get().getOrganizationId());
                if (nvHome.isPresent()) {
                    LOG.log(Level.INFO, "DONE GET Entity By Id: {0} default flat, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                    LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                    return new ResponseEntity<>(nvHome.get(), HttpStatus.OK);
                }
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} default flat, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(httpStatus);
            }
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} default flat, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{id}/flats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlats(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} flats, request", id);

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {
            try {
                Page<?> page = service.getOrganizationFlats(organization.get().getOrganizationId(), organization.get().getIco(), pageable);
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(page, HttpStatus.OK);
            } catch (ServiceException exception) {
                HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, httpStatus.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
            }
        }

        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/flats", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPostFlats(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestBody @NotNull FlatUploader flats) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} flats, request", id);

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Optional<Organization> organization = service.findById(id);

        if (organization.isPresent()) {

            if (Optional.ofNullable(flats.getFlatsToUpload()).isPresent()) {
                if (flats.getFlatsToUpload().size() > MAX_FLATS_UPLOAD_LIMIT) {
                    LOG.log(Level.INFO, "DONE POST Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN});
                    LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }

                try {
                    service.uploadOrganizationFlats(id, flats);
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
        }

        LOG.log(Level.INFO, "DONE POST Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.NOT_FOUND.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/{id}/upload/flats")
    public ResponseEntity<?> getEntityByIdPostFlatsByMultipartForm(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam("file") @NotNull MultipartFile file, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} upload flats by multipart form, request", id);

        if (file.getSize() > MAX_FLAT_UPLOAD_SIZE) {
            LOG.log(Level.INFO, "Max upload file size exceeded: {0}", file.getSize());
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} upload flats by multipart form, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_FLATS_UPLOAD, RestUtils.RECAPTCHA_SCORE_0_8);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} upload flats by multipart form, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} upload flats by multipart form, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            FlatUploader flatUploader = objectMapper.readValue(file.getBytes(), FlatUploader.class);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} upload flats by multipart form, request", new Object[]{id});
            return this.getEntityByIdPostFlats(principal, id, flatUploader);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} upload flats by multipart form, request http status: {1}", new Object[]{id, HttpStatus.BAD_REQUEST.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/{id}/flats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatsDelete(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} flats, request", id);

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_FLATS_DELETE, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            service.deleteOrganizationFlats(id);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status: {1}", new Object[]{id, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} flats, request http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/flats/{flatid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and flat By Id: {1}, request", new Object[]{id, flatid});

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1}, request http status: {2}", new Object[]{id, flatid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Optional<?> element = service.getNVHomeFlatById(id, flatid);
            if (element.isPresent()) {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1}, request http status: {2}", new Object[]{id, flatid, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(element.get(), HttpStatus.OK);
            } else {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1}, request http status: {2}", new Object[]{id, flatid, HttpStatus.NOT_FOUND.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1}, request http status: {2}", new Object[]{id, flatid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/flats/{flatid}/residents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatsByIdResidents(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and flat By Id: {1} residents, request", new Object[]{id, flatid});

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} residents, request http status: {2}", new Object[]{id, flatid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = service.getFlatResidents(id, flatid, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} residents, request http status: {2}", new Object[]{id, flatid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} residents, request http status: {2}", new Object[]{id, flatid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/flats/{flatid}/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatByIdTokens(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and flat By Id: {1} tokens, request", new Object[]{id, flatid});

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} tokens, request http status: {2}", new Object[]{id, flatid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Page<?> page = service.getFlatByIdTokens(id, flatid, pageable);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} tokens, request http status: {2}", new Object[]{id, flatid, HttpStatus.OK.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} tokens, request http status: {2}", new Object[]{id, flatid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/flats/{flatid}/tokens", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatByIdTokensPost(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting POST Entity By Id: {0} and flat By Id: {1} tokens, request", new Object[]{id, flatid});

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_TOKEN, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and flat By Id: {1} tokens, request http status: {2}", new Object[]{id, flatid, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and flat By Id: {1} tokens, request http status: {2}", new Object[]{id, flatid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and flat By Id: {1} tokens, request", new Object[]{id, flatid});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return createdStatus(HttpStatus.CREATED, service.createFlatByIdTokens(id, flatid));
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE POST Entity By Id: {0} and flat By Id: {1} tokens, request http status: {2}", new Object[]{id, flatid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @GetMapping(value = "/{id}/flats/{flatid}/tokens/{tokenid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatByIdTokenById(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid, @PathVariable @NotNull String tokenid, Pageable pageable) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request", new Object[]{id, flatid, tokenid});

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Optional<?> element = service.getFlatByIdTokenById(id, flatid, tokenid);
            if (element.isPresent()) {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, HttpStatus.OK.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(element.get(), HttpStatus.OK);
            } else {
                LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, HttpStatus.NOT_FOUND.toString()});
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @DeleteMapping(value = "/{id}/flats/{flatid}/tokens/{tokenid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFlatByIdTokenByIdDelete(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @PathVariable @NotNull String flatid, @PathVariable @NotNull String tokenid, Pageable pageable, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting DELETE Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request", new Object[]{id, flatid, tokenid});

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_TOKEN, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException exception) {
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            service.deleteFlatByIdTokenById(id, flatid, tokenid);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, HttpStatus.NO_CONTENT.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE DELETE Entity By Id: {0} and flat By Id: {1} and token By Id: {2}, request http status: {3}", new Object[]{id, flatid, tokenid, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/generate/ares", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateAres(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting Generating ARES record, request");

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_ARES, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Generating ARES record, http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, id);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Generating ARES record, http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            regitrationService.uploadInformationFromAres(id);
            LOG.log(Level.INFO, "DONE Generating ARES record, http status: {1}", new Object[]{id, HttpStatus.CREATED.toString()});
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE Generating ARES record, http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/members/ares", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMemberFromAres(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting Receiving members from ARES, request");

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_PERSON, RestUtils.RECAPTCHA_SCORE_0_9);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Receiving members from ARES, http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE Receiving members from ARES, http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            regitrationService.getMembersFromAres(service.findById(id).get());
            LOG.log(Level.INFO, "DONE Receiving members from ARES, http status: {1}", new Object[]{id, HttpStatus.CREATED.toString()});
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ServiceException exception) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(exception);
            LOG.log(Level.INFO, "DONE Receiving members from ARES, http status: {1}", new Object[]{id, httpStatus.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PostMapping(value = "/{id}/random/flats", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdRandomFlats(@AuthenticationPrincipal Jwt principal, @PathVariable @NotNull String id, @RequestParam(required = false) String recaptcha_token) {
        LOG.log(Level.INFO, "Starting GET Entity By Id: {0} Generating random flats, request", id);

        try {
            RestUtils.verifyRecaptcha(applicationMainScope, UserService.getUserScopesPrincipal(principal), recaptcha_token, RestUtils.RECAPTCHA_FLATS_UPLOAD, RestUtils.RECAPTCHA_SCORE_0_7);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} Generating random flats, request http status: {1}", new Object[]{id, HttpStatus.LOCKED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }

        try {
            byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), id);
        } catch (SecurityException securityException) {
            LOG.log(Level.INFO, "DONE GET Entity By Id: {0} Generating random flats, request http status: {1}", new Object[]{id, HttpStatus.FORBIDDEN.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        FlatGenerator flatGenerator = new FlatGenerator(47, objectMapper);
        LOG.log(Level.INFO, "DONE GET Entity By Id: {0} Generating random flats, request", new Object[]{id});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return getEntityByIdPostFlats(principal, id, flatGenerator.getFlatUploader());
    }

    @GetMapping(value = "/random/flats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRandomFlats(@AuthenticationPrincipal Jwt principal) {
        LOG.log(Level.INFO, "Starting Generating random flats");
        forbiddenToAllExpectForAppliactionMainScope(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal));
        FlatGenerator flatGenerator = new FlatGenerator(7, objectMapper);
        LOG.log(Level.INFO, "DONE Generating random flats");
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(flatGenerator.getJSON(), HttpStatus.OK);
    }

    private ResponseEntity<?> getResponseForPost(Optional<String> idOptional) {
        if (idOptional.isPresent()) {
            LOG.log(Level.INFO, "DONE POST, request http status: {0}", new Object[]{HttpStatus.CREATED.toString()});
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return createdStatus(HttpStatus.CREATED, idOptional.get());
        }
        LOG.log(Level.INFO, "DONE POST, request http status: {0}", new Object[]{HttpStatus.BAD_REQUEST.toString()});
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    @Override
    public void fillInRequirementCrudCreate(Requirement requirement, Organization model) {
        fillInCommonCrudAttributes(requirement, model);
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

    public void readByOrganizationFlatOwnersSecurity(Jwt principal, String entityId) throws SecurityException {
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            final Optional<Organization> entity = service.findById(entityId);
            if (entity.isPresent()) {
                try {
                    final Optional<Page<Organization>> organizations = service.getOrganizationsContainingUserFlats(principal);
                    if (organizations.isPresent()) {
                        for (Organization organization : organizations.get().getContent()) {
                            if (organization.getIco().equals(entity.get().getIco())) {
                                return;
                            }
                        }
                    }
                } catch (ServiceException exception) {
                    throw new ForbiddenException();
                }
            }
            throw new ForbiddenException();
        }
    }

    public void byOrganizationTokenOwnerSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            if (tokenService.checkOrganizationTokenOwnerByOrganizationId(userUid, entityId) == false) {
                throw new ForbiddenException();
            }
        }
    }

    protected void crudReadSecurity(Jwt principal, String entityId) throws SecurityException {
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            byOrganizationTokenOwnerOrFlatOwnerSecurity(principal, entityId);
        }
    }

    @Override
    protected void crudReadSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        //SECURITY OVERRIDEN
    }

    @Override
    protected void crudCreateSecurity(List<String> scopes, String userUid, Organization model) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    @Override
    protected void crudUpdateSecurity(List<String> scopes, String userUid, Organization model) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            if (Optional.ofNullable(model).isPresent()) {
                byOrganizationTokenOwnerSecurity(scopes, userUid, model.getOrganizationId());
            } else {
                throw new ForbiddenException("");
            }
        }
    }

    @Override
    protected void crudDeleteSecurity(List<String> scopes, String userUid, String entityId) throws SecurityException {
        byOrganizationTokenOwnerSecurity(scopes, userUid, entityId);
    }

    @Override
    protected void crudAllReadSecurity(List<String> scopes, String userUid) throws SecurityException {
        forbiddenToAllExpectForAppliactionMainScope(scopes, userUid);
    }

    public void byOrganizationTokenOwnerOrFlatOwnerSecurity(Jwt principal, String entityId) throws SecurityException {
        int error = 0;
        if (!UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            try {
                byOrganizationTokenOwnerSecurity(UserService.getUserScopesPrincipal(principal), UserService.getUserUidByPrincipal(principal), entityId);
            } catch (SecurityException exception) {
                error++;
            }

            try {
                readByOrganizationFlatOwnersSecurity(principal, entityId);
            } catch (SecurityException exception) {
                error++;
            }

            if (error == 2) {
                throw new ForbiddenException("");
            }
        }
    }

    private void documentModifySecurity(List<String> scopes, String documentId, String recaptcha_token) {
        if (!scopes.contains(applicationMainScope)) {
            Optional<CreateDocumentSecurity> createDocumentSecurity = Optional.ofNullable(this.documentCreateSecurity.get(recaptcha_token));
            if (createDocumentSecurity.isPresent()) {
                if (createDocumentSecurity.get().getDocumentId().equals(documentId)) {
                    if (createDocumentSecurity.get().getCreateRequestDate().getTime() + 30000 > System.currentTimeMillis()) {
                        return;
                    }
                }
            }
            throw new ForbiddenException();
        }
    }
}
