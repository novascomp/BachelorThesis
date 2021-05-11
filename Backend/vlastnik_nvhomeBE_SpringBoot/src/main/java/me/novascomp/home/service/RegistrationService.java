package me.novascomp.home.service;

import ares.vr.AresVrCachedAresResponse;
import ares.vr.AresVrResponse;
import ares.vr.AresVrCachedRawResponse;
import ares.vr.AresVrEndpoint;
import ares.vr.fe.AresVrForFEPruposes;
import ares.vr.json.VypisVR;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import me.novascomp.home.config.BeansInit;
import ares.vr.AresRecordGenerator;
import me.novascomp.home.model.Organization;
import me.novascomp.home.model.Token;
import me.novascomp.home.model.User;
import me.novascomp.home.service.business.rules.OrganizationCreateBusinessRule;
import me.novascomp.microservice.nvflat.model.LightweightToken;
import me.novascomp.home.flat.uploader.NVHomeFlat;
import me.novascomp.microservice.nvflat.service.NVFlatService;
import me.novascomp.microservice.nvm.model.LightweightCategory;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class RegistrationService {

    private final NVFlatService flatMicroservice;
    private final OrganizationService organizationService;
    private final TokenService tokenService;
    private final FileService fileService;
    private final MemberService memberService;
    private final CommitteeService committeeService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(RegistrationService.class.getName());
    private final Map<String, String> keys;

    private final Map<String, AresVrCachedAresResponse> aresVrResponse;
    private final Map<String, AresVrCachedRawResponse> aresVrRawResponse;

    public static final String NHOME_TOKEN_PREFIX = "NHOME";
    public static final String DEFAULT_FLAT_IDENTIFIER = "VÝBOR";
    public static final String DEFAULT_CATEGORY_NAME = "NEZAŘAZENO";
    public static final String ARES_CATEGORY_NAME = "ARES";
    public static final String DEFAULT_COMMITTEE_EMAIL = "NEVYPLNĚNO";
    public static final String DEFAULT_COMMITTEE_PHONE = "NEVYPLNĚNO";
    public static final String DEFAULT_MEMBERS_FIRST_NAME = "NEVYPLNĚNO";
    public static final String DEFAULT_MEMBERS_LAST_NAME = "NEVYPLNĚNO";
    public static final String DEFAULT_MEMBERS_EMAIL = "NEVYPLNĚNO";
    public static final String DEFAULT_MEMBERS_PHONE = "NEVYPLNĚNO";
    public static final String DEFAULT_MEMBERS_DATE_OF_BIRTH = "NEVYPLNĚNO";
    public static final String CATEGORIES_COMPONENT = "categories";
    public static final String SVJ_REGISTER = "RSVJ";
    public static final String DEFAULT_ARES_DOCUMENT_HEADING = "Informace z ARES";
    public static final String DEFAULT_ARES_DOCUMENT_BODY = "Základní informace z ARES ke dni: ";

    @Autowired
    public RegistrationService(NVFlatService flatMicroservice, OrganizationService organizationService, TokenService tokenService, FileService fileService, MemberService memberService, CommitteeService committeeService, UserService userService, ObjectMapper objectMapper) {
        this.flatMicroservice = flatMicroservice;
        this.organizationService = organizationService;
        this.tokenService = tokenService;
        this.fileService = fileService;
        this.memberService = memberService;
        this.committeeService = committeeService;
        this.userService = userService;
        this.keys = new HashMap<>();
        this.objectMapper = objectMapper;
        this.aresVrResponse = new HashMap<>();
        this.aresVrRawResponse = new HashMap<>();
    }

    public boolean existOrganization(@NotNull String ico) {
        return organizationService.existsByIco(ico);
    }

    public synchronized String getAresVrRawResponse(@NotNull String ico) throws ServiceException {
        Optional<AresVrCachedRawResponse> rawResponse = Optional.ofNullable(aresVrRawResponse.get(ico));
        if (rawResponse.isPresent()) {
            if (rawResponse.get().getResponseDate().getTime() + 3600000 < System.currentTimeMillis()) {
                rawResponse = Optional.ofNullable(null);
                aresVrRawResponse.remove(ico);
            }
        }
        if (rawResponse.isEmpty()) {
            aresVrRawResponse.put(ico, new AresVrCachedRawResponse(new Date(), AresVrEndpoint.getAresVerejnyRejstrikByIcoRawResponse(ico)));
        }
        return aresVrRawResponse.get(ico).getRawResponse();
    }

    public AresVrResponse getAresResponse(@NotNull String ico) throws ServiceException {
        Optional<AresVrCachedAresResponse> response = Optional.ofNullable(aresVrResponse.get(ico));
        if (response.isPresent()) {
            if (response.get().getDate().getTime() + 3600000 < System.currentTimeMillis()) {
                response = Optional.ofNullable(null);
                aresVrResponse.remove(ico);
            }
        }
        if (response.isEmpty()) {
            aresVrResponse.put(ico, new AresVrCachedAresResponse(new Date(), new AresVrResponse(getAresVrRawResponse(ico), objectMapper)));
        }
        return aresVrResponse.get(ico).getAresResponse();
    }

    public boolean checkOrganizationValidity(@NotNull String ico) throws ServiceException {
        return getAresVrRawResponse(ico).contains(SVJ_REGISTER);
    }

    public static void maxSVJCountCheck(@AuthenticationPrincipal Jwt principal, UserService userService) throws SecurityException {
        User user = userService.findByUid(UserService.getUserUidByPrincipal(principal)).get();
        if (Optional.ofNullable(user.getTokenList()).isPresent()) {
            if (user.getTokenList().size() >= 5) {
                throw new ForbiddenException("");
            }
        }
    }

    public synchronized String registerNewOrganization(@NotNull String ico, @AuthenticationPrincipal Jwt principal) throws ServiceException, SecurityException {

        maxSVJCountCheck(principal, userService);

        String organizationId = null;
        try {
            if ((existOrganization(ico) == false) && checkOrganizationValidity(ico)) {
                organizationId = registerOrganization(ico);

                Requirement requirement = flatMicroservice.getRequirementOrganizationCrudCreate();
                requirement.setAttribute(Attribute.ICO, ico);
                requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
                flatMicroservice.createElement(requirement);

                final String tokenId = registerFirstOrganizationToken(organizationId);
                final Optional<Token> token = tokenService.findById(tokenId);

                String flatId = registerDefaultFlat(organizationId);
                String tokenKey = getDefaultFlatToken(organizationId, flatId, token.get().getKey());
                addUserToDefaultFlat(principal, tokenKey);

                this.committeeService.addDefaultCommittee(organizationService.findById(organizationId).get());

                final String randomId = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
                if (token.isPresent()) {
                    keys.put(randomId, token.get().getTokenId());
                }

                applyRegistration(randomId, principal);
            } else {
                if (existOrganization(ico)) {
                    throw new ConflictException("");
                } else {
                    throw new BadRequestException("");
                }
            }
        } catch (ServiceException ex) {
            if (Optional.ofNullable(organizationId).isPresent()) {
                LOG.log(Level.INFO, "Rollbacking organization registration in NVFLAT ICO: {0}", ico);
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                try {
                    flatMicroservice.deleteOrganization(organizationId);
                } catch (ServiceException ex2) {
                }
            }
            throw ex;
        }

        return organizationId;
    }

    public void getMembersFromAres(@NotNull Organization organization) throws ServiceException {
        List<String> membersToDeleteIds = new ArrayList<>();
        organization.getCommitteeList().get(0).getMemberList().forEach((member) -> {
            membersToDeleteIds.add(member.getMemberId());
        });
        membersToDeleteIds.forEach((memberId) -> {
            memberService.removeMemberFromCommittee(memberId);
        });
        memberService.addPersonFromAres(organization.getIco(), organization.getCommitteeList().get(0).getCommitteeId(), getAresResponse(organization.getIco()).getVypisVRs(), this);
    }

    public void appendAresData(@NotNull Organization organization) {
        AresVrForFEPruposes aresVrForFEPruposes = new AresVrForFEPruposes();
        try {
            aresVrForFEPruposes = new AresVrForFEPruposes(getAresResponse(organization.getIco()).getOrganizationName());
        } catch (ServiceException exception) {
            LOG.log(Level.INFO, "Failed to get organization name for organization {0}", organization.getIco());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        }
        organization.setAresVrForFEPruposes(aresVrForFEPruposes);
    }

    public void applyRegistration(@NotNull String randomId, @AuthenticationPrincipal Jwt principal) {
        final Optional<String> tokenId = Optional.ofNullable(keys.get(randomId));
        final Optional<User> user = tokenService.getUserService().findByUid(UserService.getUserUidByPrincipal(principal));

        if (tokenId.isPresent() && user.isPresent()) {
            tokenService.addUserToToken(tokenId.get(), user.get().getUserId());
        } else {
            throw new NotFoundException("");
        }
    }

    public Optional<NVHomeFlat> getDefaultFlat(@NotNull String organizationId) {
        Requirement requirement = flatMicroservice.getRequirementOrganizationFlatByIdentifierCrudRead();
        requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        requirement.setAttribute(Attribute.FLAT_IDENTIFIER, DEFAULT_FLAT_IDENTIFIER);
        return (Optional<NVHomeFlat>) flatMicroservice.getElementById(requirement);
    }

    public String getDefaultFlatToken(@NotNull String organizationId, @NotNull String flatId, @NotNull String tokenKey) throws ServiceException {
        String createdTokenId = createDefaulFlatToken(organizationId, tokenKey);
        Optional<LightweightToken> token = (Optional<LightweightToken>) organizationService.getFlatByIdTokenById(organizationId, flatId, createdTokenId);
        return token.get().getKey();
    }

    public void addUserToDefaultFlat(Jwt principal, @NotNull String tokenKey) throws ServiceException {
        Requirement requirement = flatMicroservice.getRequirementUserToTokenCrudCreate();
        requirement.setAttribute(Attribute.KEY, tokenKey);
        requirement.setAttribute(Attribute.PRINCIPAL_TOKEN, principal.getTokenValue());
        flatMicroservice.postElementToElement(requirement);
    }

    public void uploadInformationFromAres(@NotNull String organizationId) throws ServiceException {
        Optional<Organization> organization = organizationService.findById(organizationId);
        if (organization.isPresent()) {
            List<VypisVR> AresVrs = getAresResponse(organization.get().getIco()).getVypisVRs();
            Date date = new Date();
            DateFormat dateFormatDefault = new SimpleDateFormat(BeansInit.DEFAULT_DATE_FORMAT);
            String strDateDefault = dateFormatDefault.format(date);
            DateFormat dateFormatOnlyDate = new SimpleDateFormat(BeansInit.DATE_FORMAT_ONLY_DATE);
            String strDateOnlyDate = dateFormatOnlyDate.format(date);

            Optional<String> fileId = fileService.createDocument(organizationId, DEFAULT_ARES_DOCUMENT_HEADING + " " + strDateOnlyDate, DEFAULT_ARES_DOCUMENT_BODY + strDateDefault);
            LightweightCategory lightweightCategory = fileService.getCategoryByName(organizationId, RegistrationService.ARES_CATEGORY_NAME);
            fileService.addComponentToDocument(organizationId, CATEGORIES_COMPONENT, fileId.get(), lightweightCategory.getCategoryId());
            int count = 1;

            for (VypisVR vypisVR : AresVrs) {
                String fileName = AresRecordGenerator.generateAresVrBasicDocument(organization.get().getIco(), vypisVR, count);
                File file = new File(fileName);
                if (file.exists()) {
                    try {
                        fileService.addFileViaNvfMicroservice(fileId.get(), file);
                        file.delete();
                    } catch (ServiceException ex) {
                        file.delete();
                        fileService.deleteDocumentById(organizationId, fileId.get());
                        throw ex;
                    }
                }
                count++;
            }
        } else {
            throw new BadRequestException("");
        }
    }

    public void deleteDefaulFlatToken(@NotNull String organizationId, @NotNull String tokenKey) throws ServiceException {
        String defaultFlatId = getDefaultFlat(organizationId).get().getFlatId();
        Page<LightweightToken> tokens = (Page<LightweightToken>) organizationService.getFlatByIdTokens(organizationId, defaultFlatId, PageRequest.of(0, Integer.MAX_VALUE));
        for (LightweightToken token : tokens) {
            if (token.getKey().equals(tokenKey)) {
                organizationService.deleteFlatByIdTokenById(organizationId, defaultFlatId, token.getTokenId());
                break;
            }
        }
    }

    private String registerOrganization(@NotNull String ico) throws ServiceException {
        Requirement<OrganizationCreateBusinessRule> requirement = organizationService.getRequirementCrudCreate();
        requirement.setAttribute(Attribute.ICO, ico);
        GeneralCreateResponse response = organizationService.createModel(requirement);

        LOG.log(Level.INFO, requirement.toString());
        LOG.log(Level.INFO, response.toString());

        if (response.isSuccessful()) {
            return ((Organization) response.getModel().get()).getOrganizationId();
        } else {
            if (response.getBusinessRule().isFoundInDatabase()) {
                throw new ConflictException("");
            }
        }
        throw new BadRequestException("");
    }

    private String registerFirstOrganizationToken(@NotNull String organizationId) throws ServiceException {
        Requirement requirement = tokenService.getRequirementCrudCreate();
        requirement.setAttribute(Attribute.KEY, tokenService.generateTokenKey(NHOME_TOKEN_PREFIX));
        requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        GeneralCreateResponse response = tokenService.createModel(requirement);

        LOG.log(Level.INFO, requirement.toString());
        LOG.log(Level.INFO, response.toString());

        if (response.isSuccessful()) {
            return ((Token) response.getModel().get()).getTokenId();
        }

        throw new BadRequestException("");
    }

    private String registerDefaultFlat(@NotNull String organizationId) throws ServiceException {
        NVHomeFlat flatUpload = new NVHomeFlat();
        flatUpload.setIdentifier(DEFAULT_FLAT_IDENTIFIER);
        flatUpload.setSize("-");
        flatUpload.setCommonShareSize("-");
        try {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
            return organizationService.uploadFlat(flatUpload, organizationId).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new MicroserviceConnectionException(ex.getMessage().toString());
        }
    }

    private String createDefaulFlatToken(@NotNull String organizationId, @NotNull String tokenKey) throws ServiceException {
        Optional<NVHomeFlat> homeFlat = getDefaultFlat(organizationId);
        if (homeFlat.isPresent()) {
            return organizationService.createFlatByIdTokensDefinedKey(organizationId, homeFlat.get().getFlatId(), tokenKey);
        } else {
            throw new NotFoundException("");
        }
    }

}
