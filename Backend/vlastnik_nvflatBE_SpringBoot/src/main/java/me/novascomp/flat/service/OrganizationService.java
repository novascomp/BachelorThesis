package me.novascomp.flat.service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import me.novascomp.flat.config.BeansInit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.flat.config.ScopeEnum;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.Organization;
import me.novascomp.flat.model.Token;
import me.novascomp.flat.repository.OrganizationRepository;
import me.novascomp.flat.rest.FlatController;
import static me.novascomp.flat.rest.TokenController.NFLAT_TOKEN_PREFIX;
import me.novascomp.flat.service.business.rules.OrganizationCreateBusinessRule;
import me.novascomp.flat.service.business.rules.OrganizationUpdateBusinessRule;
import me.novascomp.home.flat.uploader.FlatUploader;
import me.novascomp.home.flat.uploader.LightweightToken;
import me.novascomp.home.flat.uploader.NVHomeFlat;
import me.novascomp.home.generator.DefaultFlatAccessDocumentPDFGenerator;
import me.novascomp.microservice.nvm.model.LightweightCategory;
import me.novascomp.microservice.nvm.model.LightweightComponent;
import me.novascomp.microservice.nvm.model.LightweightMessage;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService extends GeneralService<Organization, OrganizationRepository, OrganizationCreateBusinessRule, OrganizationUpdateBusinessRule> {

    public static final String DEFAULT_TOKEN_DOCUMENT_HEADING = "Přístupové kódy pro jednotky";
    public static final String DEFAULT_TOKEN_DOCUMENT_BODY = "Přístupové kódy pro jednotky nahrané ke dni a času: ";

    @Autowired
    @Qualifier("applicationMainScope")
    protected String applicationMainScope;

    public Optional<Organization> findByIco(String ico) {
        return repository.findByIco(ico);
    }

    public Page<Organization> getUserOrganizationByFlatTokenHoldingPageable(List<Flat> flatList, Pageable pageable) {
        return repository.findDistinctByFlatListIn(flatList, pageable);
    }

    public void createOrganizatinFlats(FlatService flatService, DetailService detailService, TokenService tokenService, ScopeService scopeService, MessageRecordNvmService documentService, String organizationId, FlatUploader flatUploader) throws ServiceException {
        flatUploader.getFlatsToUpload().forEach((nVHomeFlat) -> {
            String flatId = createFlat(organizationId, nVHomeFlat, flatService, detailService, tokenService, scopeService);
            addDetailToFlat(detailService, flatId, nVHomeFlat);
            LightweightToken lightweightToken = addTokenToFlat(tokenService, scopeService, flatId);
            flatUploader.getGeneratedTokens().put(nVHomeFlat.getIdentifier(), lightweightToken);
        });

        //NVM MICROSERVICE
        flatService.createFlatComponentInNvmMicroservice(organizationId, getDefaultComponentTextList(flatUploader), documentService);
        try {
            addFlatTokensFile(documentService, detailService, organizationId, flatService.findByIdentifierAndOrganization(FlatController.DEFAULT_FLAT_NAME, findById(organizationId).get()).get(), flatUploader);
        } catch (ServiceException ex) {
            try {
                flatService.deleteAllOrganizationFlats(organizationId, documentService);
            } catch (ServiceException ex2) {
            }
            throw ex;
        }
    }

    private List<String> getDefaultComponentTextList(FlatUploader flatUploader) {
        List<String> identifiersToAdd = new ArrayList<>();
        flatUploader.getFlatsToUpload().forEach((nVHomeFlat) -> {
            identifiersToAdd.add(nVHomeFlat.getIdentifier());
        });
        return identifiersToAdd;
    }

    private void addFlatTokensFile(MessageRecordNvmService documentService, DetailService detailService, String organizationId, Flat flat, FlatUploader flatUploader) throws ServiceException {

        String fileName = DefaultFlatAccessDocumentPDFGenerator.generateFlatsDefaultTokenDocument(findById(organizationId).get().getIco(), flatUploader);
        String detilId = flat.getDetailList().get(0).getDetailId();
        LightweightMessage message = new LightweightMessage();
        message.setHeading(DEFAULT_TOKEN_DOCUMENT_HEADING);

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat(BeansInit.DEFAULT_DATE_FORMAT);
        String strDate = dateFormat.format(date);
        StringBuilder body = new StringBuilder();
        body.append(DEFAULT_TOKEN_DOCUMENT_BODY).append(strDate);

        message.setBody(body.toString());
        message.setDetailId(detilId);

        String messageId = createDocument(documentService, organizationId, flat.getIdentifier(), message);

        File file = new File(fileName);
        if (file.exists()) {
            try {
                documentService.addContentToDocument(messageId, file);
                file.delete();
            } catch (ServiceException ex) {
                file.delete();
                documentService.deleteDocumentById(detailService, organizationId, messageId, detilId, true);
                throw ex;
            }
        }
    }

    public void addRequestedCategory(FlatService flatService, DetailService detailService, MessageRecordNvmService documentService, String organizationId, String documentid, LightweightComponent componentBody) throws ServiceException {

        final Optional<Organization> organization = findById(organizationId);

        if (organization.isEmpty()) {
            throw new NotFoundException("");
        }

        LightweightComponent component = (LightweightComponent) documentService.getComponentById(organizationId, componentBody.getComponentId()).get();
        Optional<Flat> flat = flatService.findByIdentifierAndOrganization(component.getText(), findById(organizationId).get());
        if (flat.isEmpty()) {
            throw new NotFoundException("");
        }

        Detail detail = detailService.findByFlat(flat.get(), PageRequest.of(0, 1)).getContent().get(0);

        if (Optional.ofNullable(detail).isEmpty()) {
            throw new NotFoundException("");
        }

        documentService.addComponentToDocument(organizationId, detail.getDetailId(), documentid, componentBody.getComponentId());
    }

    public String createDocumentByFlatRequest(Jwt principal, DetailService detailService, MessageRecordNvmService documentService, String organizationId, LightweightMessage message) throws ServiceException {
        if (existsById(organizationId) == false) {
            throw new NotFoundException("");
        }

        String flatIdentifier = detailService.findById(message.getDetailId()).get().getFlat().getIdentifier();

        String body;
        if (UserService.getUserScopesPrincipal(principal).contains(applicationMainScope)) {
            body = message.getBody();
        } else {
            body = message.getBody() + "#" + flatIdentifier;
        }
        message.setBody(body);

        return createDocument(documentService, organizationId, flatIdentifier, message);
    }

    public String createDocument(MessageRecordNvmService documentService, String organizationId, String flatIdentifier, LightweightMessage message) throws ServiceException {
        Optional<String> messageId = documentService.createDocument(organizationId, message.getHeading(), message.getBody());
        if (messageId.isPresent()) {
            List<LightweightCategory> components = (List<LightweightCategory>) documentService.getDocumentsComponents(organizationId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            components.stream().filter((component) -> (component.getText().equals(flatIdentifier))).forEachOrdered((component) -> {
                documentService.addComponentToDocument(organizationId, message.getDetailId(), messageId.get(), component.getCategoryId());
            });
        }
        if (messageId.isPresent()) {
            return messageId.get();
        }

        throw new BadRequestException("");
    }

    public String createFlat(String organizationId, NVHomeFlat nVHomeFlat, FlatService flatService, DetailService detailService, TokenService tokenService, ScopeService scopeService) throws ServiceException {

        Requirement flatRequirement = flatService.getRequirementCrudCreate();
        flatRequirement.setAttribute(Attribute.FLAT_IDENTIFIER, nVHomeFlat.getIdentifier());
        flatRequirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);
        GeneralCreateResponse<Flat, ?, ?> flatCreateResponse = flatService.createModel(flatRequirement);

        if (flatCreateResponse.isSuccessful() && flatCreateResponse.getModel().isPresent()) {
            String flatId = flatCreateResponse.getModel().get().getFlatId();
            return flatId;
        }

        throw new BadRequestException("");
    }

    public void addDetailToFlat(DetailService detailService, String flatId, NVHomeFlat nVHomeFlat) {
        Requirement detailRequirement = detailService.getRequirementCrudCreate();
        detailRequirement.setAttribute(Attribute.FLAT_ID, flatId);
        detailRequirement.setAttribute(Attribute.SIZE, nVHomeFlat.getSize());
        detailRequirement.setAttribute(Attribute.COMMON_SHARE_SIZE, nVHomeFlat.getCommonShareSize());

        GeneralCreateResponse<Detail, ?, ?> detailCreateResponse = detailService.createModel(detailRequirement);

        if (detailCreateResponse.isSuccessful()) {
            return;
        }

        throw new BadRequestException("");
    }

    public LightweightToken addTokenToFlat(TokenService tokenService, ScopeService scopeService, String flatId) {
        Requirement tokenRequirement = tokenService.getRequirementCrudCreate();
        tokenRequirement.setAttribute(Attribute.FLAT_ID, flatId);
        tokenRequirement.setAttribute(Attribute.KEY, tokenService.generateTokenKey(NFLAT_TOKEN_PREFIX));
        GeneralCreateResponse<Token, ?, ?> tokenCreateResponse = tokenService.createModel(tokenRequirement);

        if (tokenCreateResponse.isSuccessful() && tokenCreateResponse.getModel().isPresent()) {
            String tokenId = tokenCreateResponse.getModel().get().getTokenId();
            tokenService.addScopeToToken(tokenId, scopeService.findByScope(ScopeEnum.SCOPE_FLAT_OWNER.getScopeName()).get().getScopeId());
            return new LightweightToken(tokenId, tokenService.findById(tokenId).get().getKey());
        }

        throw new BadRequestException("");
    }

    public List<Flat> getAllOrganizationFlatsToRemove(String organizationId) throws ServiceException {
        Optional<Organization> organization = repository.findById(organizationId);
        List<Flat> flatsToRemove = new ArrayList<>();
        if (organization.isPresent()) {
            organization.get().getFlatList().forEach((flat) -> {
                flatsToRemove.add(flat);
            });
            return flatsToRemove;
        } else {
            throw new NotFoundException("");
        }
    }

    @Override
    public GeneralCreateResponse<Organization, Requirement, OrganizationCreateBusinessRule> createModel(Requirement<OrganizationCreateBusinessRule> requirement) {
        OrganizationCreateBusinessRule businessRule = getBusinessRuleCrudCreate(requirement);

        GeneralCreateResponse<Organization, Requirement, OrganizationCreateBusinessRule> response;
        if (requirement.isValid(businessRule)) {
            String id = requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get();
            Organization model = addModel(id, requirement);
            response = new GeneralCreateResponse<>(Optional.ofNullable(model), requirement, businessRule, true);
        } else {
            response = new GeneralCreateResponse<>(Optional.ofNullable(null), requirement, businessRule, false);
        }
        return response;
    }

    @Override
    protected OrganizationCreateBusinessRule getBusinessRuleCrudCreate(Requirement<OrganizationCreateBusinessRule> requirement) {

        Optional<String> requiredId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);
        Optional<String> ico = requirement.getAttributeValue(Attribute.ICO);
        boolean foundInDatabase = false;

        if (ico.isPresent()) {
            foundInDatabase = repository.existsByIco(ico.get());
        }

        if (foundInDatabase) {
            return new OrganizationCreateBusinessRule(foundInDatabase);
        }

        if (requiredId.isPresent()) {
            foundInDatabase = repository.existsById(requiredId.get());
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

}
