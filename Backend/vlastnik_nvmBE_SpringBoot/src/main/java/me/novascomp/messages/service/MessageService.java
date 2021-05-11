package me.novascomp.messages.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.novascomp.messages.model.Category;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.model.General;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.model.Priority;
import me.novascomp.microservice.nvf.MicroserviceNvf;
import me.novascomp.messages.repository.MessageRepository;
import me.novascomp.microservice.nvf.FileViaMicroserviceResponse;
import me.novascomp.microservice.nvf.model.CategoryHierarchy;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MessageService extends GeneralService<Message, MessageRepository> {

    private final FileNvfService fileNvfService;
    private final PriorityService priorityService;
    private final CategoryService categoryService;
    private final MicroserviceNvf microserviceNvf;

    private static final Logger LOG = Logger.getLogger(MessageService.class.getName());

    @Autowired
    public MessageService(FileNvfService fileNvfService, PriorityService priorityService, CategoryService categoryService, MicroserviceNvf microserviceNvf) {
        this.fileNvfService = fileNvfService;
        this.priorityService = priorityService;
        this.categoryService = categoryService;
        this.microserviceNvf = microserviceNvf;
    }

    @Override
    public Optional<Message> findById(String id) {
        return repository.findById(id);
    }

    public Page<Message> findByCreatorKey(String creatorKey, Pageable pageable) {
        return repository.findByCreatorKey(creatorKey, pageable);
    }

    public Page<Message> getCategoryListInByCategoryHierarchy(CategoryHierarchy categoryHierarchy, Pageable pageable) {
        List<Message> finalMessages = new ArrayList<>();
        Page<Message> previewMessages = findDistinctByCategoryListIn(categoryHierarchy.getMainCategories(), PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort()));
        for (Message message : previewMessages.getContent()) {
            for (Category category : categoryHierarchy.getSecondaryCategories()) {
                if (categoryHierarchy.getMainCategories().contains(category) && message.getCategoryList().size() == 1) {
                    finalMessages.add(message);
                } else {
                    if (message.getCategoryList().contains(category)) {
                        finalMessages.add(message);
                    }
                }
            }
        }
        return (Page<Message>) getPage(finalMessages.stream().distinct().collect(Collectors.toList()), pageable);
    }

    public Page<Message> findDistinctByCategoryListIn(List<Category> categoryList, Pageable pageable) {
        List<Category> categoriesToFind = new ArrayList<>();
        categoryList.stream().map((category) -> categoryService.findById(category.getCategoryId())).filter((categoryRecord) -> (categoryRecord.isPresent())).forEachOrdered((categoryRecord) -> {
            categoriesToFind.add(categoryRecord.get());
        });
        List<Message> listWithDuplicates;
        if (pageable.getSort().toString().contains("general.date") && pageable.getSort().toString().contains("DESC")) {
            listWithDuplicates = repository.findByCategoryListIn(categoriesToFind, PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort().and(Sort.by("general.time").descending())));
        } else {
            if (pageable.getSort().get().findFirst().get().getProperty().contains("general.date") && pageable.getSort().get().findFirst().get().getProperty().contains("ASC")) {
                listWithDuplicates = repository.findByCategoryListIn(categoriesToFind, PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort().and(Sort.by("general.time").ascending())));
            } else {
                listWithDuplicates = repository.findByCategoryListIn(categoriesToFind, PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort()));
            }
        }

        List<Message> listWithoutDuplicates = listWithDuplicates.stream().distinct().collect(Collectors.toList());
        return (Page<Message>) getPage(listWithoutDuplicates, pageable);
    }

    private Page<Message> getPage(List<Message> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > list.size() ? list.size() : (start + pageable.getPageSize());
        return new PageImpl<>((List<Message>) list.subList(start, end), pageable, list.size());
    }

    public void addMessage(Message previewMessage) {
        Message message = new Message();
        String id = UUID.randomUUID().toString();
        previewMessage.setMessageId(id);

        message.setMessageId(id);
        General general = nvfUtils.getGeneral(id);
        message.setGeneral(general);
        message.setHeading(previewMessage.getHeading());
        message.setBody(previewMessage.getBody());
        message.setCreatorKey(previewMessage.getCreatorKey());

        message.setFileNvfList(new ArrayList<>());
        message.setPriorityList(new ArrayList<>());
        message.setCategoryList(new ArrayList<>());
        message.setReList(new ArrayList<>());

        loadNvfFiles(previewMessage, message);
        loadPriorities(previewMessage, message);
        loadCategories(previewMessage, message);

        repository.save(message);
    }

    public void removeMessage(Message message) {

        List<FileNvf> fileNvfsToDelete = new ArrayList<>();

        for (FileNvf fileNvf : message.getFileNvfList()) {
            fileNvfsToDelete.add(fileNvf);
        }

        message.getPriorityList().removeAll(message.getPriorityList());
        message.getCategoryList().removeAll(message.getCategoryList());

        for (FileNvf fileNvf : fileNvfsToDelete) {
            fileNvfService.deleteFile(fileNvf);
        }

        this.delete(message);
    }

    public void addPriorityToMessage(Message messsage, Priority priority) {
        messsage.getPriorityList().add(priorityService.findById(priority.getPriorityId()).get());
        repository.save(messsage);
    }

    public void removePriorityFromMessage(Message messsage, String priorityID) {
        messsage.getPriorityList().remove(priorityService.findById(priorityID).get());
        repository.save(messsage);
    }

    public void addCategoryToMessage(String messsageId, String categoryId) {
        Optional<Message> message = findById(messsageId);
        Optional<Category> category = categoryService.findById(categoryId);

        if (message.isPresent() && category.isPresent()) {
            if (!category.get().getMessageList().contains(message.get())) {
                if (message.get().getCategoryList().size() < 6) {
                    message.get().getCategoryList().add(categoryService.findById(categoryId).get());
                    repository.save(message.get());
                    return;
                }
            }
        }

        throw new BadRequestException("");
    }

    public void removeCategoryFromMessage(String messageId, String categoryID) {
        Optional<Message> message = findById(messageId);
        Optional<Category> category = categoryService.findById(categoryID);

        if (message.isPresent() && category.isPresent()) {
            message.get().getCategoryList().remove(category.get());
            repository.save(message.get());
            if (message.get().getCategoryList().isEmpty()) {
                removeMessage(message.get());
            }
        }
    }

    public Optional<String> addFileToMessage(String messageId, MultipartFile file) throws ServiceException, SecurityException {
        final Optional<Message> entity = findById(messageId);

        if (entity.isEmpty()) {
            throw new NotFoundException("Message ID: " + entity.get().getMessageId());
        } else {
            if (entity.get().getFileNvfList().size() <= 2) {
                FileViaMicroserviceResponse fileViaMicroserviceResponse = fileNvfService.addFileViaNvfMicroservice(file);
                if (fileViaMicroserviceResponse.getHttpStatus().is2xxSuccessful()) {
                    entity.get().getFileNvfList().add(fileNvfService.findById(fileViaMicroserviceResponse.getModel().get().getFileNvfId()).get());
                    repository.save(entity.get());
                    return Optional.ofNullable(fileViaMicroserviceResponse.getModel().get().getFileNvfId());
                } else {
                    httpStatusCodeToException(fileViaMicroserviceResponse.getHttpStatus());
                    return null;
                }
            } else {
                throw new ForbiddenException();
            }
        }
    }

    public void addFileToMessage(Message messsage, FileNvf fileNvf) {
        messsage.getFileNvfList().add(fileNvfService.findById(fileNvf.getFileNvfId()).get());
        repository.save(messsage);
    }

    public void removeFileFromMessage(Message messsage, String fileNvfID) {
        messsage.getFileNvfList().remove(fileNvfService.findById(fileNvfID).get());
        repository.save(messsage);
    }

    private void loadNvfFiles(Message previewMessage, Message message) {
        if (previewMessage.getFileNvfList() == null) {
            return;
        }
        previewMessage.getFileNvfList().forEach((fileNvf) -> {
            message.getFileNvfList().add(fileNvfService.findById(fileNvf.getFileNvfId()).get());
        });
    }

    private void loadPriorities(Message previewMessage, Message message) {
        if (previewMessage.getPriorityList() == null) {
            return;
        }
        previewMessage.getPriorityList().forEach((priority) -> {
            message.getPriorityList().add(priorityService.findById(priority.getPriorityId()).get());
        });
    }

    private void loadCategories(Message previewMessage, Message message) {
        if (previewMessage.getCategoryList() == null) {
            return;
        }
        previewMessage.getCategoryList().forEach((category) -> {
            message.getCategoryList().add(categoryService.findById(category.getCategoryId()).get());
        });
    }

    public Optional<ResponseEntity> verifyMessageRequest(Message message) {

        if (message.getHeading() == null) {
            return Optional.ofNullable(new ResponseEntity<>("heading IS EMPTY", HttpStatus.BAD_REQUEST));
        }

        if (message.getBody() == null) {
            return Optional.ofNullable(new ResponseEntity<>("body IS EMPTY", HttpStatus.BAD_REQUEST));
        }

        if (message.getBody() == null) {
            return Optional.ofNullable(new ResponseEntity<>("creatorKey IS EMPTY", HttpStatus.BAD_REQUEST));
        }

        if (message.getFileNvfList() != null) {
            for (FileNvf fileNvf : message.getFileNvfList()) {
                if (!fileNvfService.existsById(fileNvf.getFileNvfId())) {
                    return Optional.ofNullable(new ResponseEntity<>("file in NVF DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
                }
            }
        }

        if (message.getPriorityList() != null) {
            for (Priority priority : message.getPriorityList()) {
                if (!priorityService.existsById(priority.getPriorityId())) {
                    return Optional.ofNullable(new ResponseEntity<>("priority ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
                }
            }
        }

        if (message.getCategoryList() != null) {
            for (Category category : message.getCategoryList()) {
                if (!categoryService.existsById(category.getCategoryId())) {
                    return Optional.ofNullable(new ResponseEntity<>("category ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
                }
            }
        }
        return Optional.ofNullable(null);
    }

    public Optional<ResponseEntity> verifyAddCategoryRequest(Message message, Category category) {
        if (!categoryService.existsById(category.getCategoryId())) {
            return Optional.ofNullable(new ResponseEntity<>("category ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
        }

        if (categoryService.findById(category.getCategoryId()).get().getMessageList().contains(message)) {
            return Optional.ofNullable(new ResponseEntity<>("message has ALREADY CONTAINED this category", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }

    public Optional<ResponseEntity> verifyDeleteCategoryRequest(Message message, String categoryID) {
        if (!categoryService.existsById(categoryID)) {
            return Optional.ofNullable(new ResponseEntity<>("category ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
        }

        if (!categoryService.findById(categoryID).get().getMessageList().contains(message)) {
            return Optional.ofNullable(new ResponseEntity<>("message DOES NOT CONTAIN this category", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }

    public Optional<ResponseEntity> verifyAddPriorityRequest(Message message, Priority priority) {
        if (!priorityService.existsById(priority.getPriorityId())) {
            return Optional.ofNullable(new ResponseEntity<>("priority ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
        }

        if (priorityService.findById(priority.getPriorityId()).get().getMessageList().contains(message)) {
            return Optional.ofNullable(new ResponseEntity<>("message has ALREADY CONTAINED this priority", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }

    public Optional<ResponseEntity> verifyDeletePriorityRequest(Message message, String priorityID) {
        if (!priorityService.existsById(priorityID)) {
            return Optional.ofNullable(new ResponseEntity<>("priority ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
        }

        if (!priorityService.findById(priorityID).get().getMessageList().contains(message)) {
            return Optional.ofNullable(new ResponseEntity<>("message DOES NOT CONTAIN this priority", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }

    public Optional<ResponseEntity> verifyAddFileRequest(Message message, FileNvf fileNvf) {
        if (!fileNvfService.existsById(fileNvf.getFileNvfId())) {
            return Optional.ofNullable(new ResponseEntity<>("fileNvf ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
        }

        if (fileNvfService.findById(fileNvf.getFileNvfId()).get().getMessageList().contains(message)) {
            return Optional.ofNullable(new ResponseEntity<>("message has ALREADY CONTAINED this file", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }

    public Optional<ResponseEntity> verifyDeleteFileRequest(Message message, String fileNvfID) {
        if (!fileNvfService.existsById(fileNvfID)) {
            return Optional.ofNullable(new ResponseEntity<>("fileNvf ID DOES NOT EXISTS", HttpStatus.BAD_REQUEST));
        }

        if (!fileNvfService.findById(fileNvfID).get().getMessageList().contains(message)) {
            return Optional.ofNullable(new ResponseEntity<>("message DOES NOT CONTAIN this file", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }
}
