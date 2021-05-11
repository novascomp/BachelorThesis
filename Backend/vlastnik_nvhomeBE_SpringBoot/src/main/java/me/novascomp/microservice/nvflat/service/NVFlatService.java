package me.novascomp.microservice.nvflat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.logging.Level;
import me.novascomp.home.flat.uploader.FlatUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import me.novascomp.home.model.Organization;
import me.novascomp.microservice.nvflat.http.NVFLatMicroservice;
import me.novascomp.microservice.nvflat.model.LightweightDetail;
import me.novascomp.microservice.nvflat.model.LightweightFlat;
import me.novascomp.microservice.nvflat.model.LightweightResident;
import me.novascomp.microservice.nvflat.model.LightweightScope;
import me.novascomp.microservice.nvflat.model.LightweightToken;
import me.novascomp.home.flat.uploader.NVHomeFlat;
import me.novascomp.microservice.nvflat.model.ScopeEnum;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.microservice.communication.MicroserviceResponse;
import me.novascomp.utils.microservice.communication.RestResponsePage;
import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.CreatedException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.OKException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

@Service
public class NVFlatService extends NVFLatMicroservice {

    @Autowired
    public NVFlatService(MicroserviceCredentials microserviceCredentials, ObjectMapper objectMapper, @Qualifier("nvflatPath") String nvflatPath) {
        super(microserviceCredentials, objectMapper, nvflatPath);
    }

    public Optional<?> getElementById(@NotNull Requirement requirement) throws ServiceException {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (requirement.areAttributesValid() == false) {
            httpStatusCodeToException(httpStatus);
        }

        try {
            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID) {
                return getNVHomeFlatById((String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get(), (String) requirement.getAttributeValue(Attribute.FLAT_ID).get());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_FLAT_TOKEN_BY_ID) {
                return getFlatTokenById((String) requirement.getAttributeValue(Attribute.FLAT_ID).get(), (String) requirement.getAttributeValue(Attribute.TOKEN_ID).get());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_ORGANIZATION_FLAT_BY_IDENTIFIER) {
                return getOrganizationsFlatByIdentifier((String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get(), (String) requirement.getAttributeValue(Attribute.FLAT_IDENTIFIER).get());
            }

        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
        throw new InternalException("");
    }

    public String createElement(@NotNull Requirement requirement) throws ServiceException {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (requirement.areAttributesValid() == false) {
            httpStatusCodeToException(httpStatus);
        }

        try {
            HttpResponse<String> response = null;

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_FLAT_CREATE) {
                response = postFlat((String) requirement.getAttributeValue(Attribute.FLAT_IDENTIFIER).get(), (String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_DETAIL_CREATE) {
                response = postDetail((String) requirement.getAttributeValue(Attribute.SIZE).get(), (String) requirement.getAttributeValue(Attribute.COMMON_SHARE_SIZE).get(), (String) requirement.getAttributeValue(Attribute.FLAT_ID).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID_TOKEN_CREATE) {
                response = postFlatTokens((String) requirement.getAttributeValue(Attribute.FLAT_ID).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID_TOKEN_KEY_DEFINED_CREATE) {
                response = postFlatTokensKeyDefined((String) requirement.getAttributeValue(Attribute.FLAT_ID).get(), (String) requirement.getAttributeValue(Attribute.KEY).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_ORGANIZATION_CREATE) {
                response = postOrganization((String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get(), (String) requirement.getAttributeValue(Attribute.ICO).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (httpStatus.is2xxSuccessful()) {
                String[] path = response.headers().firstValue(HttpHeaders.LOCATION).get().split("/");
                String id = path[path.length - 1];
                return id;
            } else {
                httpStatusCodeToException(httpStatus);
            }
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException(ex.getMessage().toString());
        }
        throw new InternalException("");

    }

    public void postElementToElement(@NotNull Requirement requirement) throws ServiceException {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (requirement.areAttributesValid() == false) {
            httpStatusCodeToException(httpStatus);
        }

        try {
            HttpResponse<String> response = null;
            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_SCOPE_TO_TOKEN_DETAIL) {
                response = postScopeToToken((String) requirement.getAttributeValue(Attribute.TOKEN_ID).get(), (String) requirement.getAttributeValue(Attribute.SCOPE_ID).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_ADD_USER_TO_TOKEN_BY_TOKEN_KEY) {
                response = postUserToTokenByTokenKey((String) requirement.getAttributeValue(Attribute.KEY).get(), (String) requirement.getAttributeValue(Attribute.PRINCIPAL_TOKEN).get());
                httpStatus = HttpStatus.valueOf(response.statusCode());
            }

            if (httpStatus.is2xxSuccessful()) {
                return;
            }

        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
        throw new InternalException("");

    }

    public Page<?> getPageOfElements(@NotNull Requirement requirement, @NotNull Pageable pageable) throws ServiceException {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (requirement.areAttributesValid() == false) {
            httpStatusCodeToException(httpStatus);
        }

        try {
            HttpResponse<String> response = null;

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_ORGANIZATION_FLATS_WITH_DETAILS) {
                response = getOrganizationFlats((String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get(), pageable);
                httpStatus = HttpStatus.valueOf(response.statusCode());

                if (httpStatus.is2xxSuccessful()) {
                    return getOrganiationFlatsWithDetails(response, (String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get(), pageable);
                }
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_GET_FLAT_BY_ID_TOKENS) {
                response = getFlatTokens((String) requirement.getAttributeValue(Attribute.FLAT_ID).get(), pageable);
                httpStatus = HttpStatus.valueOf(response.statusCode());

                if (httpStatus.is2xxSuccessful()) {
                    return objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightToken>>() {
                    });
                }
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_PAGE_SCOPES) {
                response = getAllScopesResponse();
                httpStatus = HttpStatus.valueOf(response.statusCode());

                if (httpStatus.is2xxSuccessful()) {
                    return objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightFlat>>() {
                    });
                }
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID_RESIDENTS) {
                response = getFlatResidents((String) requirement.getAttributeValue(Attribute.FLAT_ID).get(), pageable);
                httpStatus = HttpStatus.valueOf(response.statusCode());

                if (httpStatus.is2xxSuccessful()) {
                    return objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightResident>>() {
                    });
                }
            }

            if (requirement.getnTag() == AttributeTag.NV_MICROSERVICE_FLAT_TOKEN_BY_ID_SCOPES) {
                response = getFlatTokenScopes((String) requirement.getAttributeValue(Attribute.TOKEN_ID).get(), pageable);
                httpStatus = HttpStatus.valueOf(response.statusCode());

                if (httpStatus.is2xxSuccessful()) {
                    return objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightScope>>() {
                    });
                }
            }

            httpStatusCodeToException(httpStatus);
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
        throw new InternalException("");
    }

    @Async
    public Future<String> uploadFlat(@NotNull Requirement requirement) throws ServiceException {

        HttpStatus httpStatusFlat = HttpStatus.BAD_REQUEST;

        if (requirement.areAttributesValid() == false) {
            httpStatusCodeToException(httpStatusFlat);
        }

        try {

            //CREATE FLAT
            Requirement flatRequirement = getRequirementFlatCrudCreate();
            flatRequirement.setAttribute(Attribute.FLAT_IDENTIFIER, (String) requirement.getAttributeValue(Attribute.FLAT_IDENTIFIER).get());
            flatRequirement.setAttribute(Attribute.ORGANIZATION_ID, (String) requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());
            String flatId = createElement(flatRequirement);

            // CREATE DETAIL
            Requirement detailRequirement = getRequirementDetailCrudCreate();
            detailRequirement.setAttribute(Attribute.FLAT_ID, flatId);
            detailRequirement.setAttribute(Attribute.SIZE, (String) requirement.getAttributeValue(Attribute.SIZE).get());
            detailRequirement.setAttribute(Attribute.COMMON_SHARE_SIZE, (String) requirement.getAttributeValue(Attribute.COMMON_SHARE_SIZE).get());
            String detailId = createElement(detailRequirement);

            // CRETATE TOKEN;
            Requirement tokenRequirement = getRequirementFlatByIdTokenCrudCreate();
            tokenRequirement.setAttribute(Attribute.FLAT_ID, flatId);
            String tokenId = createElement(tokenRequirement);

            // POST SCOPE TO OWNER
            Requirement scopeToTokenRequirement = getRequirementScopeToTokenCrudCreate();
            scopeToTokenRequirement.setAttribute(Attribute.SCOPE_ID, getFlatOwnerScope().getScopeId());
            scopeToTokenRequirement.setAttribute(Attribute.TOKEN_ID, tokenId);
            postElementToElement(scopeToTokenRequirement);

            return new AsyncResult<>(flatId);
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
    }

    @Async
    public Future<LightweightToken> getDefaultFLatToken(@NotNull String flatId) {
        Requirement requirement = getRequirementFlatByIdTokensCrudRead();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);
        return new AsyncResult<>((LightweightToken) getPageOfElements(requirement, PageRequest.of(0, 1)).getContent().get(0));
    }

    public void uploadFlats(@NotNull String organizationId, @NotNull FlatUploader flats) throws ServiceException {
        try {
            HttpResponse<String> response = uploadOrganizationFlats(organizationId, flats);
            HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());
            if (httpStatus.is2xxSuccessful()) {
            } else {
                httpStatusCodeToException(httpStatus);
            }
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
    }

    public void deleteOrganization(@NotNull String organizationId) throws ServiceException {

        try {
            HttpResponse<String> response = deleteOrganizationById(organizationId);
            HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());
            if (httpStatus.is2xxSuccessful()) {
            } else {
                httpStatusCodeToException(httpStatus);
            }
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
    }

    public void deleteFlat(@NotNull String flatId) throws ServiceException {

        try {
            HttpResponse<String> response = deleteOrganizationFlat(flatId);
            HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());
            if (httpStatus.is2xxSuccessful()) {
            } else {
                httpStatusCodeToException(httpStatus);
            }
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
    }

    public void deleteOrganizationFlats(@NotNull String organizationId) throws ServiceException {

        try {
            HttpResponse<String> response = deleteAllOrganizationFlats(organizationId);
            HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());
            if (httpStatus.is2xxSuccessful()) {
            } else {
                httpStatusCodeToException(httpStatus);
            }
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
    }

    public void deleteFlatToken(@NotNull String flatId, @NotNull String tokenId) throws ServiceException {

        Requirement requirement = getRequirementFlatByIdTokensCrudRead();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);

        Page<LightweightToken> flatTokens = (Page<LightweightToken>) getPageOfElements(requirement, PageRequest.of(0, Integer.MAX_VALUE));

        for (LightweightToken token : flatTokens.getContent()) {
            if (token.getTokenId().equals(tokenId)) {
                try {
                    deleteFlatToken(tokenId);
                    return;
                } catch (ConnectException ex) {
                    throw new MicroserviceConnectionException(ex.toString());
                } catch (InterruptedException | IOException ex) {
                    throw new InternalException("");
                }
            }
        }

        throw new BadRequestException("");
    }

    public Requirement getRequirementPageScopesCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_PAGE_SCOPES);
    }

    public Requirement getRequirementFlatByIdTokensCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_GET_FLAT_BY_ID_TOKENS);
    }

    public Requirement getRequirementFlatByIdResidentsCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID_RESIDENTS);
    }

    public Requirement getRequirementFlatByIdTokenCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID_TOKEN_CREATE);
    }

    public Requirement getRequirementFlatByIdTokenKeyDefinedCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID_TOKEN_KEY_DEFINED_CREATE);
    }

    public Requirement getRequirementOrganizationFlatsWithDetailsCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_ORGANIZATION_FLATS_WITH_DETAILS);
    }

    public Requirement getRequirementOrganizationFlatByIdCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_FLAT_BY_ID);
    }

    public Requirement getRequirementUploadFlatAndDetailCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_UPLOAD_FLAT_AND_DETAIL_CREATE);
    }

    public Requirement getRequirementOrganizationCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_ORGANIZATION_CREATE);
    }

    public Requirement getRequirementFlatCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_FLAT_CREATE);
    }

    public Requirement getRequirementDetailCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_DETAIL_CREATE);
    }

    public Requirement getRequirementScopeToTokenCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_SCOPE_TO_TOKEN_DETAIL);
    }

    public Requirement getRequirementUserToTokenCrudCreate() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_ADD_USER_TO_TOKEN_BY_TOKEN_KEY);
    }

    public Requirement getRequirementTokenScopesCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_TOKEN_BY_ID_SCOPES);
    }

    public Requirement getRequirementFlatTokenByIdCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_FLAT_TOKEN_BY_ID);
    }

    public Requirement getRequirementOrganizationFlatByIdentifierCrudRead() {
        return new Requirement(AttributeTag.NV_MICROSERVICE_FLAT_ORGANIZATION_FLAT_BY_IDENTIFIER);
    }

    private Optional<NVHomeFlat> getNVHomeFlatById(@NotNull String homeOrganizationId, @NotNull String nvflatFlatId) throws IOException, InterruptedException, ServiceException {

        NVHomeFlat homeFlat = null;
        Optional<LightweightFlat> lightweightFlat = getFlatById(nvflatFlatId);

        if (lightweightFlat.isPresent()) {
            if (lightweightFlat.get().getOrganizationId().equals(homeOrganizationId)) {
                homeFlat = createNVHomeFlat(homeOrganizationId, lightweightFlat.get());
            } else {
                throw new BadRequestException("");
            }
        }

        return Optional.ofNullable(homeFlat);
    }

    private Optional<LightweightToken> getFlatTokenById(@NotNull String flatId, @NotNull String tokenId) throws IOException, InterruptedException {

        HttpResponse response = getFlatToken(tokenId);
        HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());

        LightweightToken lightweightToken = null;
        if (httpStatus.is2xxSuccessful()) {
            lightweightToken = new MicroserviceResponse<LightweightToken>(objectMapper).parseSingleResponse(response, LightweightToken.class);
            if (!lightweightToken.getFlatId().equals(flatId)) {
                throw new BadRequestException("");
            }
        }

        return Optional.ofNullable(lightweightToken);

    }

    private Optional<NVHomeFlat> getOrganizationsFlatByIdentifier(@NotNull String homeOrganizationId, @NotNull String nvflatFlatId) throws IOException, InterruptedException {

        HttpResponse<String> response = getOrganizationsFlatByIdentifierEndpoint(homeOrganizationId, nvflatFlatId);
        HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());

        NVHomeFlat homeFlat = null;
        if (httpStatus.is2xxSuccessful()) {
            LightweightFlat flat = new MicroserviceResponse<LightweightFlat>(objectMapper).parseSingleResponse(response, LightweightFlat.class);
            homeFlat = createNVHomeFlat(homeOrganizationId, flat);
        }

        return Optional.ofNullable(homeFlat);
    }

    private Page<?> getOrganiationFlatsWithDetails(@NotNull HttpResponse<String> flats, @NotNull String homeOrganizationId, @NotNull Pageable pageable) throws ServiceException, IOException, InterruptedException {

        Page<NVHomeFlat> pages = objectMapper.readValue(flats.body(), new TypeReference<RestResponsePage<NVHomeFlat>>() {
        });

        pages.getContent().forEach((flat) -> {
            flat.setResidentsLink(nvhomePath + "organizations/" + homeOrganizationId + "/flats/" + flat.getFlatId() + "/residents");
        });

        return pages;
    }

    private NVHomeFlat createNVHomeFlat(@NotNull String homeOrganizationId, @NotNull LightweightFlat flat) throws ServiceException, IOException, InterruptedException {
        Optional<LightweightDetail> detail = getFlatDetail(flat.getFlatDetailLink());

        NVHomeFlat homeFlat = new NVHomeFlat();
        homeFlat.setFlatId(flat.getFlatId());
        homeFlat.setIdentifier(flat.getIdentifier());

        if (detail.isPresent()) {
            homeFlat.setDetailId(detail.get().getDetailId());
            homeFlat.setCommonShareSize(detail.get().getCommonShareSize());
            homeFlat.setSize(detail.get().getSize());
            homeFlat.setResidentsLink(nvhomePath + "organizations/" + homeOrganizationId + "/flats/" + homeFlat.getFlatId() + "/residents");
        } else {
            homeFlat.setDetailId("-");
            homeFlat.setCommonShareSize("-");
            homeFlat.setSize("-");
            homeFlat.setResidentsLink("-");
        }

        homeFlat.setTokensLink(nvhomePath + "organizations/" + homeOrganizationId + "/flats/" + flat.getFlatId() + "/tokens");

        return homeFlat;
    }

    private Optional<LightweightDetail> getFlatDetail(@NotNull String flatDetailLink) throws ServiceException, IOException, InterruptedException {
        HttpResponse<String> response = getWhateverByLinkResponse(flatDetailLink);
        HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());

        LightweightDetail lightweightDetail = null;

        if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
            lightweightDetail = new MicroserviceResponse<LightweightDetail>(objectMapper).parseSingleResponse(response, LightweightDetail.class);
        } else {
            httpStatusCodeToException(httpStatus);
        }

        LOG.log(Level.INFO, response.toString());
        return Optional.ofNullable(lightweightDetail);
    }

    public LightweightScope getFlatOwnerScope() throws IOException, InterruptedException {

        HttpResponse<String> response = getAllScopesResponse();
        HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());

        if (httpStatus.is2xxSuccessful()) {
            Page<LightweightScope> scopes = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightScope>>() {
            });

            for (LightweightScope scope : scopes) {
                if (scope.getScope().equals(ScopeEnum.SCOPE_FLAT_OWNER.getScopeName())) {
                    return scope;
                }
            }
        }
        return null;
    }

    public void addDefaultFlatsTokensFile(@NotNull String organizationId, @NotNull String flatId, @NotNull java.io.File file) throws ServiceException {

        try {
            HttpResponse<String> httpResponse = uploadFile(organizationId, flatId, file);
            LOG.log(Level.INFO, httpResponse.toString());
            HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.statusCode());
            if (httpStatus.is2xxSuccessful()) {
                return;
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
            }
            throw new InternalException("");
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new InternalException("");
        }
    }

    public Optional<Page<Organization>> getOrganizationsContainingUserFlats(String bearerToken) throws IOException, InterruptedException {

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = getOrganizationsContainingUserFlatsRequest(bearerToken);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode());

            Optional<Page<Organization>> organizations = Optional.ofNullable(null);

            if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
                organizations = Optional.ofNullable(objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<Organization>>() {
                }));
            } else {
                httpStatusCodeToException(httpStatus);
            }
            return organizations;
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }
    }

    private void httpStatusCodeToException(@NotNull HttpStatus httpStatus) throws ServiceException {
        switch (httpStatus) {
            case OK:
                throw new OKException("");
            case CREATED:
                throw new CreatedException("");
            case CONFLICT:
                throw new ConflictException("");
            case FORBIDDEN:
                throw new ForbiddenException("");
            case NOT_FOUND:
                throw new NotFoundException("");
            case INTERNAL_SERVER_ERROR:
                throw new InternalException("");
            case SERVICE_UNAVAILABLE:
                throw new MicroserviceConnectionException("");
            case BAD_REQUEST:
                throw new BadRequestException("");
            default:
                throw new MicroserviceConnectionException("");
        }
    }

    public void flatPartOfOrganizationSecurity(@NotNull String organizationId, @NotNull String flatId) throws ServiceException {
        Requirement requirementFindFlatById = getRequirementOrganizationFlatByIdCrudRead();
        requirementFindFlatById.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        requirementFindFlatById.setAttribute(Attribute.FLAT_ID, flatId);
        Optional<NVHomeFlat> homeFlat = (Optional<NVHomeFlat>) getElementById(requirementFindFlatById);
        if (homeFlat.isPresent()) {
            return;
        }
        throw new BadRequestException("");
    }

    public void tokenPartOfOrganizationSecurity(@NotNull String flatId, @NotNull String tokenId) throws ServiceException {
        Requirement requirementFlatToken = getRequirementFlatTokenByIdCrudRead();
        requirementFlatToken.setAttribute(Attribute.TOKEN_ID, tokenId);
        requirementFlatToken.setAttribute(Attribute.FLAT_ID, flatId);
        if (getElementById(requirementFlatToken).isPresent()) {
            return;
        }
        throw new BadRequestException("");
    }
}
