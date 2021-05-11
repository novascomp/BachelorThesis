package me.novascomp.home.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static me.novascomp.home.service.RegistrationService.ARES_CATEGORY_NAME;
import static me.novascomp.home.service.RegistrationService.CATEGORIES_COMPONENT;
import static me.novascomp.home.service.RegistrationService.DEFAULT_CATEGORY_NAME;
import me.novascomp.home.model.File;
import me.novascomp.home.model.General;
import me.novascomp.home.model.Organization;
import me.novascomp.home.repository.FileRepository;
import me.novascomp.home.service.business.rules.FileCreateBusinessRule;
import me.novascomp.home.service.business.rules.FileUpdateBusinessRule;
import me.novascomp.microservice.nvm.model.LightweightCategory;
import me.novascomp.microservice.nvm.model.LightweightFile;
import me.novascomp.microservice.nvm.model.LightweightMessage;
import me.novascomp.microservice.nvm.model.LightweightPriority;
import me.novascomp.microservice.nvm.NVMMicroservice;
import me.novascomp.microservice.nvm.model.LightweightComponent;
import me.novascomp.microservice.nvm.model.LightweightFileShare;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.microservice.communication.RestResponsePage;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class FileService extends GeneralService<File, FileRepository, FileCreateBusinessRule, FileUpdateBusinessRule> {

    @Autowired
    @Qualifier("nvhomePath")
    protected String nvhomePath;

    public static final String[] AVAILABLE_COMPONENT_NAMES = {"categories", "priorities"};

    private final OrganizationService organizationService;
    private final NVMMicroservice nVMMicroservice;

    @Autowired
    public FileService(OrganizationService organizationService, NVMMicroservice nVMMicroservice) {
        this.organizationService = organizationService;
        this.nVMMicroservice = nVMMicroservice;
    }

    public Page<?> getDocumentsByCategories(@NotNull String organizationId, @NotNull List<LightweightCategory> categories, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> response = nVMMicroservice.getMessagesByCategories(categories, pageable);

            if (isSuccessful(response)) {
                Page<LightweightMessage> messages = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightMessage>>() {
                });
                messages.getContent().forEach((message) -> {
                    Optional<File> file = repository.findByIdNvm(message.getMessageId());
                    if (file.isPresent()) {
                        setDocumentLinksAndId(message, organizationId, file.get().getFileId(), file.get().getGeneral());
                    }
                });
                return messages;
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
            throw new InternalException("");
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public void addFileViaNvfMicroservice(@NotNull String documentId, @NotNull java.io.File file) throws ServiceException {
        documentExistenceSecurity(documentId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.uploadFile(findById(documentId).get().getIdNvm(), file);
            HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.statusCode());

            if (httpStatus.is2xxSuccessful()) {
                return;
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new InternalException("");
        }
    }

    public void addComponentToDocument(@NotNull String organizationId, @NotNull String componentName, @NotNull String documentId, @NotNull String componentId) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);
        checkRightComponentNameSecurity(componentName);
        creatorComponentKeySecurity((Optional<LightweightComponent>) getComponentById(organizationId, componentName, componentId), organizationId);

        try {

            Optional< HttpResponse<String>> response = Optional.ofNullable(null);
            if (componentName.equals(AVAILABLE_COMPONENT_NAMES[0])) {
                response = Optional.ofNullable(nVMMicroservice.postComponentToMessage(componentName, findById(documentId).get().getIdNvm(), new LightweightCategory(componentId)));
            }

            if (componentName.equals(AVAILABLE_COMPONENT_NAMES[1])) {
                response = Optional.ofNullable(nVMMicroservice.postComponentToMessage(componentName, findById(documentId).get().getIdNvm(), new LightweightPriority(componentId)));
            }

            if (response.isPresent()) {
                if (isSuccessful(response.get())) {
                    return;
                } else {
                    httpStatusCodeToException(HttpStatus.valueOf(response.get().statusCode()));
                }
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }

        throw new InternalException("");
    }

    public Optional<String> addContentToDocument(@NotNull String organizationId, @NotNull String documentId, @NotNull MultipartFile file) throws ServiceException, SecurityException {
        documentPartOfOrganizationSecurity(organizationId, documentId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.uploadFile(findById(documentId).get().getIdNvm(), file);
            HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.statusCode());

            if (httpStatus.is2xxSuccessful()) {
                if (httpStatus == HttpStatus.CREATED) {
                    String[] path = httpResponse.headers().firstValue(HttpHeaders.LOCATION).get().split("/");
                    String nvfFileId = path[path.length - 1];
                    return Optional.ofNullable(nvfFileId);
                }
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
            }
            throw new InternalException("");
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new InternalException("");
        }
    }

    public Optional<?> getDocumentByIdComponentById(@NotNull String organizationId, @NotNull String componentName, @NotNull String documentId, @NotNull String componentid, @NotNull Pageable pageable) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);
        checkRightComponentNameSecurity(componentName);

        try {
            HttpResponse<String> response = nVMMicroservice.getFileComponentById(componentName, findById(documentId).get().getIdNvm(), componentid);

            if (isSuccessful(response)) {
                LightweightComponent component = objectMapper.readValue(response.body(), LightweightComponent.class);
                creatorComponentKeySecurity(Optional.ofNullable(component), organizationId);
                return processComponentResponse(response, componentName);
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }

        throw new InternalException("");
    }

    public HttpResponse<String> deleteDocumentByIdComponentById(@NotNull String organizationId, @NotNull String componentName, @NotNull String documentId, @NotNull String componentid) throws ServiceException {
        checkRightComponentNameSecurity(componentName);
        documentPartOfOrganizationSecurity(organizationId, documentId);

        try {
            HttpResponse<String> response = nVMMicroservice.deleteFileComponentById(componentName, findById(documentId).get().getIdNvm(), componentid);
            return response;
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public Page<?> getDocumentsComponents(@NotNull String organizationId, @NotNull String componentName, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        checkRightComponentNameSecurity(componentName);

        try {
            HttpResponse<String> response = nVMMicroservice.getComponentsByCreatorKey(organizationId, componentName, pageable);

            if (isSuccessful(response)) {
                Page<?> categories = processComponentsPageResponse(organizationId, response, componentName);
                if (componentName.equals(AVAILABLE_COMPONENT_NAMES[0])) {
                    if (checkDefaultCategories(organizationId, (List<LightweightCategory>) categories.getContent()) == false) {
                        response = nVMMicroservice.getComponentsByCreatorKey(organizationId, componentName, pageable);
                        return processComponentsPageResponse(organizationId, response, componentName);
                    }
                }

            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }

        throw new InternalException("");
    }

    public void deleteDocumentsComponentById(@NotNull String organizationId, @NotNull String componentName, @NotNull String categoryId) throws ServiceException, SecurityException {

        organizationExistenceSecurity(organizationId);
        checkRightComponentNameSecurity(componentName);
        creatorComponentKeySecurity((Optional<LightweightComponent>) getComponentById(organizationId, componentName, categoryId), organizationId);

        try {
            if (getCategoryByName(organizationId, RegistrationService.DEFAULT_CATEGORY_NAME).getCategoryId().equals(categoryId)
                    || getCategoryByName(organizationId, RegistrationService.ARES_CATEGORY_NAME).getCategoryId().equals(categoryId)) {
                throw new ForbiddenException("");
            }

            this.checkComponents(organizationId, componentName, categoryId);
            HttpResponse<String> response = nVMMicroservice.deleteMessageComponent(organizationId, componentName, categoryId);

            if (isSuccessful(response)) {
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public LightweightCategory getCategoryByName(@NotNull String organizationId, @NotNull String categoryName) throws ServiceException {
        List<LightweightCategory> components = (List<LightweightCategory>) getDocumentsComponents(organizationId, "categories", PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        for (LightweightCategory component : components) {
            if (component.getText().equals(categoryName)) {
                return component;
            }
        }

        throw new NotFoundException("");
    }

    public Optional<?> getComponentById(@NotNull String organizationId, @NotNull String componentName, @NotNull String componentId) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        checkRightComponentNameSecurity(componentName);

        try {
            HttpResponse<String> response = nVMMicroservice.getComponentById(componentName, componentId);

            if (isSuccessful(response)) {
                return processComponentResponse(response, componentName);
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }

        throw new InternalException("");
    }

    public Page<LightweightMessage> getDocuments(@NotNull String organizationId, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        Page<LightweightMessage> messages = null;
        try {
            HttpResponse<String> response = nVMMicroservice.getMessageByCreatorKey(organizationId, pageable);

            if (isSuccessful(response)) {
                messages = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightMessage>>() {
                });
                for (LightweightMessage message : messages.getContent()) {
                    Optional<File> file = repository.findByIdNvm(message.getMessageId());
                    if (file.isPresent()) {
                        setDocumentLinksAndId(message, organizationId, file.get().getFileId(), file.get().getGeneral());
                    }
                }
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }

        return messages;
    }

    public Page<?> geDocumentByIdContents(@NotNull String organizationId, @NotNull String documentId, Pageable pageable) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);

        try {
            LightweightMessage message = getDocumentByDocumentId(organizationId, documentId);
            HttpResponse<String> contentResponse = nVMMicroservice.getWhateverByLinkResponse(message.getFilesLink(), pageable);
            Page<LightweightFile> contents = objectMapper.readValue(contentResponse.body(), new TypeReference<RestResponsePage<LightweightFile>>() {
            });

            contents.stream().map((file) -> {
                return file;
            }).map((file) -> {
                file.setSharesLink(documentId);
                return file;
            }).forEachOrdered((file) -> {
                file.setSharesLink(nvhomePath + "organizations/" + organizationId + "/" + "documents" + "/" + documentId + "/contents/" + file.getFileNvfId());
            });

            return contents;
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public Page<?> getDocumentByIdContentById(@NotNull String organizationId, @NotNull String documentId, @NotNull String contentId, Pageable pageable) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);

        try {
            HttpResponse<String> contentResponse = nVMMicroservice.getFileShares(contentId, pageable);
            Page<LightweightFileShare> shares = objectMapper.readValue(contentResponse.body(), new TypeReference<RestResponsePage<LightweightFileShare>>() {
            });
            for (LightweightFileShare fileShare : shares) {
                if (!fileShare.getMessagesId().contains(findById(documentId).get().getIdNvm())) {
                    throw new BadRequestException("");
                }
            }
            return shares;
        } catch (ConnectException | InterruptedException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        }
    }

    public Page<?> getComponentsByDocumentId(@NotNull String organizationId, @NotNull String documentId, @NotNull String componentName, @NotNull Pageable pageable) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);

        try {
            LightweightMessage message = getDocumentByDocumentId(organizationId, documentId);

            HttpResponse<String> response = null;
            if (componentName.equals(AVAILABLE_COMPONENT_NAMES[0])) {
                response = nVMMicroservice.getWhateverByLinkResponse(message.getCategoriesLink(), pageable);
            } else if (componentName.equals(AVAILABLE_COMPONENT_NAMES[1])) {
                response = nVMMicroservice.getWhateverByLinkResponse(message.getPrioritiesLink(), pageable);
            } else {
                throw new BadRequestException("");
            }

            if (isSuccessful(response)) {
                return processComponentsPageResponse(organizationId, response, componentName);
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(response.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
        throw new InternalException("");
    }

    public LightweightMessage getDocumentByDocumentId(@NotNull String organizationId, @NotNull String documentId) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);

        LightweightMessage lightweightMessage;
        try {
            lightweightMessage = objectMapper.readValue(nVMMicroservice.getMessageById(findById(documentId).get().getIdNvm()).body(), LightweightMessage.class);
            setDocumentLinksAndId(lightweightMessage, organizationId, documentId, findById(documentId).get().getGeneral());
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }

        return lightweightMessage;
    }

    public Optional<String> createDocument(@NotNull String organizationId, @NotNull String heading, @NotNull String body) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.createMessage(organizationId, heading, body);
            if (HttpStatus.valueOf(httpResponse.statusCode()).is2xxSuccessful()) {
                String[] path = httpResponse.headers().firstValue(HttpHeaders.LOCATION).get().split("/");
                String nvmId = path[path.length - 1];
                return Optional.ofNullable(saveDocumentIdResponse(organizationId, nvmId));
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
                return null;
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException ex) {
            throw new InternalException(ex.toString());
        }
    }

    public void deleteDocumentById(@NotNull String organizationId, @NotNull String documentId) throws ServiceException {
        documentPartOfOrganizationSecurity(organizationId, documentId);

        try {
            File documentRecord = findById(documentId).get();
            nVMMicroservice.deleteMessageById(documentRecord.getIdNvm());
            repository.delete(documentRecord);
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public Optional<String> createComponent(@NotNull String organizationId, @NotNull String componentName, @NotNull String text) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        checkRightComponentNameSecurity(componentName);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.createMessageComponent(organizationId, componentName, text);
            if (isSuccessful(httpResponse)) {
                String[] path = httpResponse.headers().firstValue(HttpHeaders.LOCATION).get().split("/");
                String componentId = path[path.length - 1];
                return Optional.ofNullable(componentId);
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException ex) {
            throw new InternalException(ex.toString());
        }
        return Optional.ofNullable(null);
    }

    private String saveDocumentIdResponse(@NotNull String organizationId, @NotNull String nvmId) throws ServiceException {
        Requirement requirement = getRequirementCrudCreate();

        requirement.setAttribute(Attribute.ID_NVM, nvmId);
        requirement.setAttribute(Attribute.ORGANIZATION_ID, organizationId);

        GeneralCreateResponse response = createModel(requirement);
        if (!response.isSuccessful()) {
            if (response.getBusinessRule().isFoundInDatabase()) {
                throw new ConflictException("");
            } else {
                throw new BadRequestException("");
            }
        }
        return ((File) response.getModel().get()).getFileId();

    }

    private void setDocumentLinksAndId(@NotNull LightweightMessage message, @NotNull String organizationId, @NotNull String documentId, @NotNull General general) {
        message.setFileId(documentId);
        message.setMessageId(null);
        message.setCreatorKey(null);
        message.setCategoryComponentLink(nvhomePath + "organizations/" + organizationId + "/documents/" + documentId + "/components/categories");
        message.setPriorityComponentLink(nvhomePath + "organizations/" + organizationId + "/documents/" + documentId + "/components/priorities");
        message.setContentLink(nvhomePath + "organizations/" + organizationId + "/documents/" + documentId + "/contents");
        message.setGeneral(general);
    }

    private Optional<?> processComponentResponse(@NotNull HttpResponse<String> response, @NotNull String componentName) throws JsonProcessingException, ServiceException {
        if (componentName.equals(FileService.AVAILABLE_COMPONENT_NAMES[0])) {
            LightweightCategory category = objectMapper.readValue(response.body(), LightweightCategory.class);
            return Optional.ofNullable(category);
        } else {
            LightweightPriority priority = objectMapper.readValue(response.body(), LightweightPriority.class);
            return Optional.ofNullable(priority);
        }
    }

    private Page<?> processComponentsPageResponse(@NotNull String organizationId, @NotNull HttpResponse<String> response, @NotNull String componentName) throws JsonProcessingException, ServiceException {
        if (componentName.equals(AVAILABLE_COMPONENT_NAMES[0])) {
            Page<LightweightCategory> categories = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightCategory>>() {
            });

            categories.getContent().forEach((category) -> {
                category.setCreatorKey(null);
            });

            return categories;
        }

        if (componentName.equals(AVAILABLE_COMPONENT_NAMES[1])) {
            Page<LightweightPriority> priorities = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightPriority>>() {
            });
            priorities.getContent().forEach((priority) -> {
                priority.setCreatorKey(null);
            });
            return priorities;
        }
        throw new InternalException("");
    }

    private boolean checkDefaultCategories(@NotNull String organizationId, @NotNull List<LightweightCategory> categories) throws ServiceException {
        boolean defaultCategoryPresent = false;
        boolean aresCategoryPresent = false;

        if (categories.size() < 2) {
            for (LightweightCategory category : categories) {
                if (category.getText().equals(RegistrationService.DEFAULT_CATEGORY_NAME)) {
                    defaultCategoryPresent = true;
                }

                if (category.getText().equals(RegistrationService.ARES_CATEGORY_NAME)) {
                    aresCategoryPresent = true;
                }
            }
            if (defaultCategoryPresent == false) {
                registerUndefinedCategory(organizationId);
            }

            if (aresCategoryPresent == false) {
                registerAresCategory(organizationId);
            }
        }

        return defaultCategoryPresent && aresCategoryPresent;
    }

    private String registerUndefinedCategory(@NotNull String organizationId) throws ServiceException {
        Optional<String> undefinedCategory = createComponent(organizationId, CATEGORIES_COMPONENT, DEFAULT_CATEGORY_NAME);

        if (undefinedCategory.isPresent()) {
            return undefinedCategory.get();
        }

        throw new InternalException("");
    }

    private String registerAresCategory(@NotNull String organizationId) throws ServiceException {
        Optional<String> aresCategory = createComponent(organizationId, CATEGORIES_COMPONENT, ARES_CATEGORY_NAME);

        if (aresCategory.isPresent()) {
            return aresCategory.get();
        }

        throw new InternalException("");
    }

    private void checkComponents(@NotNull String organizationId, @NotNull String componentName, @NotNull String categoryId) {
        Page<LightweightMessage> messages = getDocuments(organizationId, PageRequest.of(0, Integer.MAX_VALUE));

        for (LightweightMessage message : messages) {
            String documentId = message.getFileId();
            Page<LightweightCategory> components = (Page<LightweightCategory>) getComponentsByDocumentId(organizationId, documentId, componentName, PageRequest.of(0, Integer.MAX_VALUE));
            if (components.getTotalElements() <= 1) {
                for (LightweightCategory component : components) {
                    if (component.getCategoryId().equals(categoryId)) {
                        addComponentToDocument(organizationId, componentName, documentId, getCategoryByName(organizationId, RegistrationService.DEFAULT_CATEGORY_NAME).getCategoryId());
                    }
                }
            }
        }
    }

    private boolean isSuccessful(@NotNull HttpResponse<String> httpResponse) {
        return (HttpStatus.valueOf(httpResponse.statusCode()) == HttpStatus.OK || HttpStatus.valueOf(httpResponse.statusCode()) == HttpStatus.CREATED || HttpStatus.valueOf(httpResponse.statusCode()) == HttpStatus.NO_CONTENT);
    }

    @Override
    protected FileCreateBusinessRule getBusinessRuleCrudCreate(Requirement<FileCreateBusinessRule> requirement) {

        Optional<String> idNvm = requirement.getAttributeValue(Attribute.ID_NVM);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean foundInDatabase = false;
        boolean organizationIdOk = true;

        if (idNvm.isPresent()) {
            foundInDatabase = repository.existsByIdNvm(idNvm.get());
        }

        if (organizationId.isPresent()) {
            organizationIdOk = organizationService.existsById(organizationId.get());
        }

        return new FileCreateBusinessRule(organizationIdOk, foundInDatabase);
    }

    @Override
    protected File addModel(String id, Requirement<FileCreateBusinessRule> requirement) {
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        File file = new File(id);
        fillInCommonCrudAttributes(requirement, file);

        if (organizationId.isPresent()) {
            Optional<Organization> organization = organizationService.findById(organizationId.get());
            if (organization.isPresent()) {
                file.setOrganization(organization.get());
            }
        }

        General general = restUtils.getGeneral(id);
        file.setGeneral(general);
        repository.save(file);
        return file;
    }

    @Override
    public Requirement<FileCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.NV_HOME_FILE_CREATE);
    }

    @Override
    protected FileUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<FileUpdateBusinessRule> requirement) {
        Optional<String> idNvm = requirement.getAttributeValue(Attribute.ID_NVM);
        Optional<String> fileId = requirement.getAttributeValue(Attribute.FILE_ID);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean idOk = false;
        boolean conflict = false;
        boolean organizationIdOk = false;

        if (fileId.isPresent()) {
            idOk = repository.existsById(fileId.get());
        }

        if (idNvm.isPresent()) {
            conflict = repository.existsByIdNvm(idNvm.get());
        }

        if (organizationId.isPresent()) {
            organizationIdOk = organizationService.existsById(organizationId.get());
        }

        return new FileUpdateBusinessRule(organizationIdOk, conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<FileUpdateBusinessRule> requirement) {
        Optional<File> file = findById(requirement.getAttributeValue(Attribute.FILE_ID).get());
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        if (file.isPresent()) {
            fillInCommonCrudAttributes(requirement, file.get());

            if (organizationId.isPresent()) {
                Optional<Organization> organization = organizationService.findById(organizationId.get());
                if (organization.isPresent()) {
                    file.get().setOrganization(organization.get());
                }
            }

            repository.save(file.get());
        }
    }

    @Override
    public Requirement<FileUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.NV_HOME_FILE_UPDATE);
    }

    @Override
    public String getModelId(File model) {
        return model.getFileId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, File file) {
        file.setIdNvm((String) requirement.getAttributeValue(Attribute.ID_NVM).get());
    }

    private void documentPartOfOrganizationSecurity(@NotNull String organizationId, @NotNull String documentId) throws ServiceException {
        Optional<File> file = findById(documentId);
        if (file.isPresent()) {
            if (file.get().getOrganization().getOrganizationId().equals(organizationId)) {
                return;
            } else {
                throw new BadRequestException("");
            }
        } else {
            throw new NotFoundException("");
        }

    }

    private void documentExistenceSecurity(@NotNull String documentId) throws ServiceException {
        if (existsById(documentId)) {
            return;
        }

        throw new NotFoundException("");
    }

    private void organizationExistenceSecurity(String organizationId) throws ServiceException {
        if (organizationService.existsById(organizationId) == false) {
            throw new NotFoundException("");
        }
    }

    private void checkRightComponentNameSecurity(String componentName) throws ServiceException {
        boolean error = true;

        for (String componentRecord : AVAILABLE_COMPONENT_NAMES) {
            if (componentRecord.equals(componentName)) {
                error = false;
            }
        }

        if (error) {
            throw new NotFoundException("");
        }
    }

    private void creatorComponentKeySecurity(@NotNull Optional<LightweightComponent> component, @NotNull String organizationId) throws ServiceException {
        if (component.isPresent()) {
            if (component.get().getCreatorKey().equals(organizationId)) {
                return;
            }
        }
        throw new BadRequestException("");
    }
}
