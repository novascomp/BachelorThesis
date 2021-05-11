package me.novascomp.home.service;

import com.sun.istack.NotNull;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.home.service.business.rules.OrganizationCreateBusinessRule;
import me.novascomp.home.service.business.rules.OrganizationUpdateBusinessRule;
import me.novascomp.home.model.General;
import me.novascomp.home.model.Organization;
import me.novascomp.home.model.Token;
import me.novascomp.home.repository.OrganizationRepository;
import me.novascomp.home.flat.uploader.FlatUploader;
import me.novascomp.home.flat.uploader.NVHomeFlat;
import me.novascomp.microservice.nvflat.service.NVFlatService;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class OrganizationService extends GeneralService<Organization, OrganizationRepository, OrganizationCreateBusinessRule, OrganizationUpdateBusinessRule> {

    private final NVFlatService flatMicroservice;

    @Autowired
    public OrganizationService(NVFlatService flatMicroservice) {
        this.flatMicroservice = flatMicroservice;
    }

    public Optional<Organization> findByIco(String ico) {
        return repository.findByIco(ico);
    }

    public boolean existsByIco(String ico) {
        return repository.existsByIco(ico);
    }

    public Page<Organization> findDistinctByTokenListIn(List<Token> tokenList, Pageable pageable) {
        return repository.findDistinctByTokenListIn(tokenList, pageable);
    }

    public Page<?> getOrganizationFlats(@NotNull String organizationId, @NotNull String ico, @NotNull Pageable pageable) throws ServiceException {
        Requirement requirement = flatMicroservice.getRequirementOrganizationFlatsWithDetailsCrudRead();
        requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        requirement.setAttribute(Attribute.ICO, ico);
        return flatMicroservice.getPageOfElements(requirement, pageable);
    }

    public Optional<?> getNVHomeFlatById(@NotNull String organizationId, @NotNull String nvflatFlatId) throws ServiceException {
        //getElementById contains security
        Requirement requirement = flatMicroservice.getRequirementOrganizationFlatByIdCrudRead();
        requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        requirement.setAttribute(Attribute.FLAT_ID, nvflatFlatId);
        return flatMicroservice.getElementById(requirement);
    }

    public Page<?> getFlatByIdTokens(@NotNull String organizationId, @NotNull String flatId, @NotNull Pageable pageable) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);

        Requirement requirement = flatMicroservice.getRequirementFlatByIdTokensCrudRead();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);
        return flatMicroservice.getPageOfElements(requirement, pageable);
    }

    public Optional<?> getFlatByIdTokenById(@NotNull String organizationId, @NotNull String flatId, @NotNull String tokenId) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);
        flatMicroservice.tokenPartOfOrganizationSecurity(flatId, tokenId);

        Requirement requirement = flatMicroservice.getRequirementFlatTokenByIdCrudRead();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);
        requirement.setAttribute(Attribute.TOKEN_ID, tokenId);
        return flatMicroservice.getElementById(requirement);
    }

    public String createFlatByIdTokens(@NotNull String organizationId, @NotNull String flatId) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);

        Requirement requirement = flatMicroservice.getRequirementFlatByIdTokenCrudCreate();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);
        return flatMicroservice.createElement(requirement);
    }

    public void deleteFlatByIdTokenById(@NotNull String organizationId, @NotNull String flatId, @NotNull String tokenId) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);
        flatMicroservice.tokenPartOfOrganizationSecurity(flatId, tokenId);

        flatMicroservice.deleteFlatToken(flatId, tokenId);
    }

    public Page<?> getFlatResidents(@NotNull String organizationId, @NotNull String flatId, @NotNull Pageable pageable) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);

        Requirement requirement = flatMicroservice.getRequirementFlatByIdResidentsCrudRead();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);
        return flatMicroservice.getPageOfElements(requirement, pageable);
    }

    public String createFlatByIdTokensDefinedKey(@NotNull String organizationId, @NotNull String flatId, @NotNull String tokenKey) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);

        Requirement requirement = flatMicroservice.getRequirementFlatByIdTokenKeyDefinedCrudCreate();
        requirement.setAttribute(Attribute.FLAT_ID, flatId);
        requirement.setAttribute(Attribute.KEY, tokenKey);
        return flatMicroservice.createElement(requirement);
    }

    public void uploadOrganizationFlats(@NotNull String organizationId, @NotNull FlatUploader flats) throws ServiceException {
        checkUploadConstraints(flats);
        flatMicroservice.uploadFlats(organizationId, flats);
    }

    private void checkUploadConstraints(@NotNull FlatUploader flats) throws ServiceException {
        flats.getFlatsToUpload().forEach((flat) -> {
            if (Optional.ofNullable(flat.getIdentifier()).isPresent() && Optional.ofNullable(flat.getSize()).isPresent() && Optional.ofNullable(flat.getCommonShareSize()).isPresent()) {
                try {
                    if (flat.getSize().split(" ").length != 2 || flat.getCommonShareSize().split("/").length != 2) {
                        throw new BadRequestException("");
                    }
                    Integer.valueOf(flat.getSize().split(" ")[0]);
                    Double.valueOf(flat.getCommonShareSize().split("/")[0]);
                    Double.valueOf(flat.getCommonShareSize().split("/")[1]);
                } catch (NumberFormatException exception) {
                    throw new BadRequestException("");
                }
            } else {
                throw new BadRequestException("");
            }
        });
    }

    //FOR TESTING PURPOSES ONLY
//    public void uploadOrganizationFlats(@NotNull FlatUploader flats, @NotNull String organizationId, RegistrationService registrationService) throws ServiceException {
//        List<FlatUploadResponse> responses = new ArrayList<>();
//        flats.getFlatsToUpload().forEach((flatUpload) -> {
//            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
//            responses.add(new FlatUploadResponse(flatUpload, uploadFlat(flatUpload, organizationId)));
//        });
//
//        responses.forEach((flat) -> {
//            try {
//                flats.getGeneratedTokens().put(flat.getFlatUpload().getIdentifier(), flatMicroservice.getDefaultFLatToken(flat.getFlatFuture().get()));
//            } catch (InterruptedException | ExecutionException ex) {
//                //  throw new InternalException(ex.getMessage());
//            }
//        });
//
//        this.uploadDefaultDocument(flats, organizationId, registrationService);
//    }
    public Future<String> uploadFlat(@NotNull NVHomeFlat flatUpload, @NotNull String organizationId) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        Requirement requirement = flatMicroservice.getRequirementUploadFlatAndDetailCrudCreate();
        requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        requirement.setAttribute(Attribute.FLAT_IDENTIFIER, flatUpload.getIdentifier());
        requirement.setAttribute(Attribute.SIZE, flatUpload.getSize());
        requirement.setAttribute(Attribute.COMMON_SHARE_SIZE, flatUpload.getCommonShareSize());
        return flatMicroservice.uploadFlat(requirement);
    }

    public Page<?> getTokenByIdScopes(@NotNull String organizationId, @NotNull String flatId, @NotNull String tokenId, @NotNull Pageable pageable) throws ServiceException {
        flatMicroservice.flatPartOfOrganizationSecurity(organizationId, flatId);
        flatMicroservice.tokenPartOfOrganizationSecurity(flatId, tokenId);

        Requirement requirement = flatMicroservice.getRequirementTokenScopesCrudRead();
        requirement.setAttribute(Attribute.TOKEN_ID, tokenId);
        return flatMicroservice.getPageOfElements(requirement, pageable);
    }

    public void deleteOrganizationFlats(@NotNull String organizationId) throws ServiceException {
        flatMicroservice.deleteOrganizationFlats(organizationId);
    }

    @Override
    protected OrganizationCreateBusinessRule getBusinessRuleCrudCreate(Requirement<OrganizationCreateBusinessRule> requirement) {

        Optional<String> ico = requirement.getAttributeValue(Attribute.ICO);
        boolean foundInDatabase = false;

        if (ico.isPresent()) {
            foundInDatabase = repository.existsByIco(ico.get());
        }

        return new OrganizationCreateBusinessRule(foundInDatabase);
    }

    @Override
    protected Organization addModel(String id, Requirement<OrganizationCreateBusinessRule> requirement) {
        Organization organization = new Organization(id);
        fillInCommonCrudAttributes(requirement, organization);
        General general = restUtils.getGeneral(id);
        organization.setGeneral(general);
        repository.save(organization);
        return organization;
    }

    @Override
    public Requirement<OrganizationCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.ORGANIZATION_CRUD_CREATE);
    }

    @Override
    protected OrganizationUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<OrganizationUpdateBusinessRule> requirement) {

        Optional<String> ico = requirement.getAttributeValue(Attribute.ICO);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean idOk = false;
        boolean conflict = false;

        if (organizationId.isPresent()) {
            idOk = repository.existsById(organizationId.get());
        }

        if (ico.isPresent()) {
            conflict = repository.existsByIco(ico.get());
        }

        return new OrganizationUpdateBusinessRule(conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<OrganizationUpdateBusinessRule> requirement) {
        Optional<Organization> organizaton = findById(requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());
        if (organizaton.isPresent()) {
            fillInCommonCrudAttributes(requirement, organizaton.get());
            repository.save(organizaton.get());
        }
    }

    @Override
    public Requirement<OrganizationUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.ORGANIZATION_CRUD_UPDATE);
    }

    @Override
    public String getModelId(Organization model) {
        return model.getOrganizationId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Organization organization) {
        organization.setIco((String) requirement.getAttributeValue(Attribute.ICO).get());
    }

    private void organizationExistenceSecurity(String organizationId) throws ServiceException {
        if (existsById(organizationId) == false) {
            throw new NotFoundException("");
        }
    }

    public Optional<Page<Organization>> getOrganizationsContainingUserFlats(Jwt principal) throws ServiceException {

        Optional<Page<Organization>> organizations = Optional.ofNullable(null);
        try {
            organizations = flatMicroservice.getOrganizationsContainingUserFlats(principal.getTokenValue());
            for (Organization organization : organizations.get().getContent()) {
                Optional<Organization> nvhomeRecord = repository.findByIco(organization.getIco());
                if (nvhomeRecord.isPresent()) {
                    organization.setOrganizationId(nvhomeRecord.get().getOrganizationId());
                } else {
                    organization.setOrganizationId("-");
                }
            }
        } catch (ConnectException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (InterruptedException | IOException ex) {
            throw new InternalException("");
        }

        return organizations;
    }

}
