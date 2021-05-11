package me.novascomp.flat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.MessageRecordNvm;
import me.novascomp.flat.repository.MessageRecordNvmRepository;
import me.novascomp.flat.rest.FlatController;
import me.novascomp.flat.service.business.rules.MessageRecordNvmCreateBusinessRule;
import me.novascomp.flat.service.business.rules.MessageRecordNvmUpdateBusinessRule;
import me.novascomp.flat.sort.SortByIdentifier;
import me.novascomp.flat.sort.SortByText;
import me.novascomp.microservice.nvm.NVMMicroservice;
import me.novascomp.microservice.nvm.model.CategoryHierarchy;
import me.novascomp.microservice.nvm.model.LightweightCategory;
import me.novascomp.microservice.nvm.model.LightweightComponent;
import me.novascomp.microservice.nvm.model.LightweightFile;
import me.novascomp.microservice.nvm.model.LightweightFileShare;
import me.novascomp.microservice.nvm.model.LightweightMessage;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.microservice.communication.RestResponsePage;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.data.domain.PageRequest;

@Service
public class MessageRecordNvmService extends GeneralService<MessageRecordNvm, MessageRecordNvmRepository, MessageRecordNvmCreateBusinessRule, MessageRecordNvmUpdateBusinessRule> {

    @Autowired
    @Qualifier("nvflatPath")
    protected String nvhomePath;

    private final NVMMicroservice nVMMicroservice;
    private final DetailService detailService;
    private final OrganizationService organizationService;

    private final String DEFAULT_COMPONENT_NAME = "categories";
    private final String DEFAULT_CREATOR_NAME_SUFIX = "flats";

    @Autowired
    public MessageRecordNvmService(DetailService detailService, NVMMicroservice nVMMicroservice, OrganizationService organizationService) {
        this.detailService = detailService;
        this.nVMMicroservice = nVMMicroservice;
        this.organizationService = organizationService;
    }

    public Page<LightweightMessage> getDocumentsByCategoriesHierarchy(@NotNull String organizationId, @NotNull CategoryHierarchy categoryHierarchy, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> response = nVMMicroservice.getMessagesByCategoryHierarchy(categoryHierarchy, pageable);

            if (isSuccessful(response)) {
                Page<LightweightMessage> messages = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightMessage>>() {
                });
                for (LightweightMessage message : messages.getContent()) {
                    Optional<MessageRecordNvm> messageRecordNvm = repository.findByIdInNvm(message.getMessageId());
                    if (messageRecordNvm.isPresent()) {
                        setDocumentLinksAndId(message, organizationId, messageRecordNvm.get().getMessageRecordNvmId(), messageRecordNvm.get().getGeneral());
                    }
                }
                return messages;
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

    public void addComponentToDocument(@NotNull String organizationId, @NotNull String detailId, @NotNull String messageId, @NotNull String componentId) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        documentExistenceSecurity(messageId);
        creatorComponentKeySecurity((Optional<LightweightComponent>) getComponentById(organizationId, componentId), organizationId + DEFAULT_CREATOR_NAME_SUFIX);

        try {
            Optional<HttpResponse<String>> response = Optional.ofNullable(nVMMicroservice.postComponentToMessage(DEFAULT_COMPONENT_NAME, findById(messageId).get().getIdInNvm(), new LightweightCategory(componentId)));
            if (response.isPresent()) {
                if (isSuccessful(response.get())) {
                    this.detailService.addMessageToDetail(detailId, messageId, this);
                } else {
                    httpStatusCodeToException(HttpStatus.valueOf(response.get().statusCode()));
                }
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public Optional<String> addContentToDocument(@NotNull String documentId, @NotNull java.io.File file) throws ServiceException, SecurityException {
        documentExistenceSecurity(documentId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.uploadFile(findById(documentId).get().getIdInNvm(), file);
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

    public Optional<String> addContentToDocument(@NotNull String documentId, @NotNull MultipartFile file) throws ServiceException, SecurityException {
        documentExistenceSecurity(documentId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.uploadFile(findById(documentId).get().getIdInNvm(), file);
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

    public Optional<?> getDocumentByIdComponentById(@NotNull String organizationId, @NotNull String documentId, @NotNull String componentid, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        documentExistenceSecurity(documentId);

        try {
            HttpResponse<String> response = nVMMicroservice.getFileComponentById(DEFAULT_COMPONENT_NAME, findById(documentId).get().getIdInNvm(), componentid);

            if (isSuccessful(response)) {
                return processComponentResponse(response);
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

    public LightweightCategory getComponentByText(@NotNull String organizationId, @NotNull String text) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            LightweightCategory lightweightCategory = new LightweightCategory(text, organizationId + DEFAULT_CREATOR_NAME_SUFIX);
            HttpResponse<String> response = nVMMicroservice.getComponentsByCreatorKeyAndText(DEFAULT_COMPONENT_NAME, lightweightCategory);

            if (isSuccessful(response)) {
                return (LightweightCategory) processComponentResponse(response).get();
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

    public void deleteAllComponentsByCreatorKey(@NotNull String organizationId) {
        Page<LightweightCategory> page = (Page<LightweightCategory>) getDocumentsComponents(organizationId, PageRequest.of(0, Integer.MAX_VALUE));
        List<String> textListToDelete = new ArrayList<>();
        page.getContent().stream().filter((category) -> (!FlatController.DEFAULT_FLAT_NAME.equals(category.getText()))).forEachOrdered((category) -> {
            textListToDelete.add(category.getText());
        });

        deleteComponentList(organizationId, textListToDelete);
    }

    public Page<?> getDocumentsComponents(@NotNull String organizationId, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> response = nVMMicroservice.getComponentsByCreatorKey(organizationId + DEFAULT_CREATOR_NAME_SUFIX, DEFAULT_COMPONENT_NAME, pageable);

            if (isSuccessful(response)) {
                return processComponentsPageResponse(response, DEFAULT_COMPONENT_NAME);
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

    public void deleteDocumentsComponentById(@NotNull String organizationId, @NotNull String categoryId) throws ServiceException, SecurityException {

        try {
            HttpResponse<String> response = nVMMicroservice.deleteMessageComponent(organizationId + DEFAULT_CREATOR_NAME_SUFIX, DEFAULT_COMPONENT_NAME, categoryId);

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

    public Optional<?> getComponentById(@NotNull String organizationId, @NotNull String componentId) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> response = nVMMicroservice.getComponentById(DEFAULT_COMPONENT_NAME, componentId);

            if (isSuccessful(response)) {
                return processComponentResponse(response);
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

    public Page<?> getDocumentByIdContents(@NotNull String organizationId, @NotNull String documentId, Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        documentExistenceSecurity(documentId);

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

    public Page<?> getComponentsByDocumentId(@NotNull String organizationId, @NotNull String documentId, @NotNull Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        documentExistenceSecurity(documentId);

        try {
            LightweightMessage message = getDocumentByDocumentId(organizationId, documentId);

            HttpResponse<String> response = null;
            response = nVMMicroservice.getWhateverByLinkResponse(message.getCategoriesLink(), pageable);

            if (isSuccessful(response)) {
                return processComponentsPageResponse(response, DEFAULT_COMPONENT_NAME);
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

    public Page<?> getDocumentByIdContentById(@NotNull String organizationId, @NotNull String documentId, @NotNull String contentId, Pageable pageable) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        documentExistenceSecurity(documentId);

        try {
            HttpResponse<String> contentResponse = nVMMicroservice.getFileShares(contentId, pageable);
            Page<LightweightFileShare> shares = objectMapper.readValue(contentResponse.body(), new TypeReference<RestResponsePage<LightweightFileShare>>() {
            });
            for (LightweightFileShare fileShare : shares) {
                if (!fileShare.getMessagesId().contains(findById(documentId).get().getIdInNvm())) {
                    throw new BadRequestException("");
                }
            }
            return shares;
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public LightweightMessage getDocumentByDocumentId(@NotNull String organizationId, @NotNull String documentId) throws ServiceException {
        organizationExistenceSecurity(organizationId);
        documentExistenceSecurity(documentId);

        LightweightMessage lightweightMessage;
        try {
            lightweightMessage = objectMapper.readValue(nVMMicroservice.getMessageById(findById(documentId).get().getIdInNvm()).body(), LightweightMessage.class);
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
            HttpResponse<String> httpResponse = nVMMicroservice.createMessage(organizationId + DEFAULT_CREATOR_NAME_SUFIX, heading, body);
            if (isSuccessful(httpResponse)) {
                String[] path = httpResponse.headers().firstValue(HttpHeaders.LOCATION).get().split("/");
                String nvmId = path[path.length - 1];
                return Optional.ofNullable(saveDocumentIdResponse(organizationId, nvmId));
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

    public void deleteDocumentById(@NotNull DetailService detailService, @NotNull String organizationId, @NotNull String documentId, @NotNull String detailId, boolean force) throws ServiceException {
        documentExistenceSecurity(documentId);

        try {
            int success = 0;

            Optional<Detail> flatDetail = detailService.findById(detailId);

            if (Optional.ofNullable(flatDetail.get()).isEmpty()) {
                throw new BadRequestException("");
            }

            String flatIdentifier = flatDetail.get().getFlat().getIdentifier();

            List<MessageRecordNvm> messages = new ArrayList<>();

            Optional<MessageRecordNvm> messageRecordNvmId = findById(documentId);
            if (messageRecordNvmId.isEmpty()) {
                throw new NotFoundException("");
            }

            messages.add(messageRecordNvmId.get());

            if (this.detailService.findByMessageRecordNvmListIn(messages, PageRequest.of(0, Integer.MAX_VALUE)).getContent().contains(detailService.findById(detailId).get()) == false) {
                throw new NotFoundException("");
            }

            if (!force) {
                HttpResponse<String> httpResponse = nVMMicroservice.deleteComponentFromMessage(messageRecordNvmId.get().getIdInNvm(), getComponentByText(organizationId, flatIdentifier).getCategoryId());
                if (HttpStatus.valueOf(httpResponse.statusCode()).is2xxSuccessful()) {
                    if (detailService.removeMessageFromDetail(detailId, documentId, this)) {
                        nVMMicroservice.deleteMessageById(messageRecordNvmId.get().getIdInNvm());
                        return;
                    }
                    success++;
                } else {
                    httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
                }

                if (success == 0) {
                    throw new NotFoundException("");
                }
            }

            if (force) {
                messageRecordNvmId = findById(documentId);
                String messageIdInNvm = messageRecordNvmId.get().getIdInNvm();
                messages.remove(messageRecordNvmId.get());
                messages.add(messageRecordNvmId.get());
                nVMMicroservice.deleteMessageById(messageIdInNvm);
                List<String> detailIds = new ArrayList<>();

                this.detailService.findByMessageRecordNvmListIn(messages, PageRequest.of(0, Integer.MAX_VALUE)).getContent().forEach((detail) -> {
                    detailIds.add(detail.getDetailId());
                });

                for (String detailIdRecord : detailIds) {
                    detailService.removeMessageFromDetail(detailIdRecord, documentId, this);
                }

            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
    }

    public void deleteComponentList(@NotNull String organizationId, @NotNull List<String> text) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.deleteMessageComponentList(organizationId + DEFAULT_CREATOR_NAME_SUFIX, DEFAULT_COMPONENT_NAME, text);
            if (isSuccessful(httpResponse)) {
                return;
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException ex) {
            throw new InternalException(ex.toString());
        }
    }

    public void createComponentList(@NotNull String organizationId, @NotNull List<String> text) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.createMessageComponentList(organizationId + DEFAULT_CREATOR_NAME_SUFIX, DEFAULT_COMPONENT_NAME, text);
            if (isSuccessful(httpResponse)) {
                return;
            } else {
                httpStatusCodeToException(HttpStatus.valueOf(httpResponse.statusCode()));
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException ex) {
            throw new InternalException(ex.toString());
        }
    }

    public Optional<String> createComponent(@NotNull String organizationId, @NotNull String text) throws ServiceException {
        organizationExistenceSecurity(organizationId);

        try {
            HttpResponse<String> httpResponse = nVMMicroservice.createMessageComponent(organizationId + DEFAULT_CREATOR_NAME_SUFIX, DEFAULT_COMPONENT_NAME, text);
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

    private String saveDocumentIdResponse(@NotNull String organizationId, @NotNull String nvmId) {
        Requirement requirement = getRequirementCrudCreate();

        requirement.setAttribute(Attribute.MESSAGE_ID_IN_NVM, nvmId);

        GeneralCreateResponse response = createModel(requirement);
        if (!response.isSuccessful()) {
            if (response.getBusinessRule().isFoundInDatabase()) {
                throw new ConflictException("");
            } else {
                throw new BadRequestException("");
            }
        }

        return ((MessageRecordNvm) response.getModel().get()).getMessageRecordNvmId();
    }

    private Optional<?> processComponentResponse(@NotNull HttpResponse<String> response) throws JsonProcessingException, ServiceException {
        LightweightCategory category = objectMapper.readValue(response.body(), LightweightCategory.class);
        return Optional.ofNullable(category);
    }

    private Page<?> processComponentsPageResponse(@NotNull HttpResponse<String> response, @NotNull String componentName) throws JsonProcessingException, ServiceException {
        Page<LightweightCategory> categories = objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<LightweightCategory>>() {
        });

        boolean flatIdentifiersInt = true;
        List<LightweightCategory> categoriesCopy = new ArrayList<>();
        categoriesCopy.addAll(categories.getContent());

        LightweightCategory committeCategory = null;
        for (LightweightCategory lightweightCategory : categories.getContent()) {
            lightweightCategory.setCreatorKey(null);

            if (FlatController.DEFAULT_FLAT_NAME.equals(lightweightCategory.getText())) {
                committeCategory = lightweightCategory;
            } else {
                try {
                    Integer.valueOf(lightweightCategory.getText());
                } catch (NumberFormatException exception) {
                    flatIdentifiersInt = false;
                }
            }
        }

        if (Optional.ofNullable(committeCategory).isPresent()) {
            categoriesCopy.remove(committeCategory);
        }
        
        if (flatIdentifiersInt) {
            Collections.sort(categoriesCopy, new SortByText());
        }

        if (Optional.ofNullable(committeCategory).isPresent()) {
            categoriesCopy.add(0, committeCategory);
        }

        return getPage(categoriesCopy, PageRequest.of(0, Integer.MAX_VALUE));
    }

    private boolean isSuccessful(@NotNull HttpResponse<String> httpResponse) {
        return (HttpStatus.valueOf(httpResponse.statusCode()) == HttpStatus.OK || HttpStatus.valueOf(httpResponse.statusCode()) == HttpStatus.CREATED || HttpStatus.valueOf(httpResponse.statusCode()) == HttpStatus.NO_CONTENT);
    }

    private void setDocumentLinksAndId(@NotNull LightweightMessage message, @NotNull String organizationId, @NotNull String messageRecordNvmId, @NotNull General general) {
        message.setFileId(messageRecordNvmId);
        message.setMessageId(null);
        message.setCreatorKey(null);
        message.setCategoryComponentLink(nvhomePath + "organizations/" + organizationId + "/documents/" + messageRecordNvmId + "/components");
        message.setContentLink(nvhomePath + "organizations/" + organizationId + "/documents/" + messageRecordNvmId + "/contents");
        message.setGeneral(general);
    }

    private void documentExistenceSecurity(String documentId) throws ServiceException {
        if (existsById(documentId) == false) {
            throw new NotFoundException("");
        }
    }

    private void organizationExistenceSecurity(String organizationId) throws ServiceException {
        if (organizationService.existsById(organizationId) == false) {
            throw new NotFoundException("");
        }
    }

    private void creatorComponentKeySecurity(@NotNull Optional<LightweightComponent> component, @NotNull String creatorKey) throws ServiceException {
        if (component.isPresent()) {
            if (component.get().getCreatorKey().equals(creatorKey)) {
                return;
            }
        }
        throw new BadRequestException("");
    }

    public List<Flat> getAvailableFlatIdentifiersUsedInDocuments(String detailid) {
        List<MessageRecordNvm> availableMessages = new ArrayList<>();
        List<Flat> availableFlats = new ArrayList<>();
        Flat flat = detailService.findById(detailid).get().getFlat();
        boolean flatIdentifiersInt = true;
        flat.getDetailList().get(0).getMessageRecordNvmList().forEach((messageRecordNvm) -> {
            availableMessages.add(messageRecordNvm);
        });

        for (MessageRecordNvm messageRecordNvm : availableMessages) {
            for (Detail detail : messageRecordNvm.getDetailList()) {
                availableFlats.add(detail.getFlat());
            }
        }

        availableFlats = availableFlats.stream().distinct().collect(Collectors.toList());

        Flat defaultFlat = null;
        for (Flat availableFlat : availableFlats) {
            if (FlatController.DEFAULT_FLAT_NAME.equals(availableFlat.getIdentifier())) {
                defaultFlat = availableFlat;
            } else {
                try {
                    Integer.valueOf(availableFlat.getIdentifier());
                } catch (NumberFormatException exception) {
                    flatIdentifiersInt = false;
                }
            }
        }

        if (flatIdentifiersInt) {
            if (Optional.ofNullable(defaultFlat).isPresent()) {
                availableFlats.remove(defaultFlat);
                Collections.sort(availableFlats, new SortByIdentifier());
                availableFlats.add(0, defaultFlat);
            } else {
                Collections.sort(availableFlats, new SortByIdentifier());
            }

            return availableFlats;
        }

        return availableFlats.stream().sorted(Comparator.comparing(Flat::getIdentifier)).distinct().collect(Collectors.toList());
    }

    @Override
    protected MessageRecordNvmCreateBusinessRule getBusinessRuleCrudCreate(Requirement<MessageRecordNvmCreateBusinessRule> requirement) {
        Optional<String> idInNvm = requirement.getAttributeValue(Attribute.MESSAGE_ID_IN_NVM);

        boolean foundInDatabase = false;

        if (idInNvm.isPresent()) {
            foundInDatabase = repository.existsByIdInNvm(idInNvm.get());
        }

        return new MessageRecordNvmCreateBusinessRule(foundInDatabase);
    }

    @Override
    protected MessageRecordNvm addModel(String id, Requirement<MessageRecordNvmCreateBusinessRule> requirement) {
        MessageRecordNvm recordNvm = new MessageRecordNvm(id);
        fillInCommonCrudAttributes(requirement, recordNvm);
        General general = restUtils.getGeneral(id);
        recordNvm.setGeneral(general);
        repository.save(recordNvm);
        return recordNvm;
    }

    @Override
    public Requirement<MessageRecordNvmCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.MESSAGE_RECORD_NVM_CREATE);
    }

    @Override
    protected MessageRecordNvmUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<MessageRecordNvmUpdateBusinessRule> requirement) {
        Optional<String> idInNvm = requirement.getAttributeValue(Attribute.MESSAGE_ID_IN_NVM);
        Optional<String> messageRecordNvmId = requirement.getAttributeValue(Attribute.MESSAGE_RECORD_NVM_ID);

        boolean idOk = false;
        boolean conflict = false;

        if (messageRecordNvmId.isPresent()) {
            idOk = repository.existsById(messageRecordNvmId.get());
        }

        if (idInNvm.isPresent()) {
            conflict = repository.existsByIdInNvm(idInNvm.get());
        }

        return new MessageRecordNvmUpdateBusinessRule(conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<MessageRecordNvmUpdateBusinessRule> requirement) {
        Optional<MessageRecordNvm> messageRecordNvmId = findById(requirement.getAttributeValue(Attribute.MESSAGE_RECORD_NVM_ID).get());

        if (messageRecordNvmId.isPresent()) {
            fillInCommonCrudAttributes(requirement, messageRecordNvmId.get());
            repository.save(messageRecordNvmId.get());
        }
    }

    @Override
    public Requirement<MessageRecordNvmUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.MESSAGE_RECORD_NVM_UPDATE);
    }

    @Override
    public String getModelId(MessageRecordNvm model) {
        return model.getMessageRecordNvmId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, MessageRecordNvm messageRecordNvm) {
        messageRecordNvm.setIdInNvm((String) requirement.getAttributeValue(Attribute.MESSAGE_ID_IN_NVM).get());
    }

}
