package me.novascomp.messages.rest;

import com.sun.istack.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.messages.config.NVFUtils;
import me.novascomp.messages.model.Category;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.model.Priority;
import me.novascomp.messages.service.CategoryService;
import me.novascomp.messages.service.FileNvfService;
import me.novascomp.messages.service.MessageService;
import me.novascomp.messages.service.PriorityService;
import me.novascomp.microservice.nvf.model.CategoryHierarchy;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/messages")
public class MessageController extends GeneralController<Message, MessageService> {

    private final CategoryService categoryService;
    private final PriorityService priorityService;
    private final FileNvfService fileNvfService;

    @Autowired
    public MessageController(CategoryService categoryService, PriorityService priorityService, FileNvfService fileNvfService) {
        this.categoryService = categoryService;
        this.priorityService = priorityService;
        this.fileNvfService = fileNvfService;
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postMessage(@RequestBody @NotNull Message message) {
        if (service.verifyMessageRequest(message).isPresent()) {
            return service.verifyMessageRequest(message).get();
        }
        service.addMessage(message);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", message.getMessageId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<?> deleteEntityById(@PathVariable
            @NotNull String id) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.removeMessage(entity.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/bycategoryhierarchy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMessagesByMainCategory(@RequestBody @NotNull CategoryHierarchy categoryHierarchy, Pageable pageable) {
        return new ResponseEntity<>(service.getCategoryListInByCategoryHierarchy(categoryHierarchy, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/bycategories", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMessagesByCategories(@RequestBody @NotNull List<Category> categories, Pageable pageable) {
        return new ResponseEntity<>(service.findDistinctByCategoryListIn(categories, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/bycreator/{creatorKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByCreatorKey(@PathVariable @NotNull String creatorKey, Pageable pageable) {
        return new ResponseEntity<>(service.findByCreatorKey(creatorKey, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFiles(@PathVariable @NotNull String id, Pageable pageable) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List list = new ArrayList();
            list.add(entity.get());
            return new ResponseEntity<>(fileNvfService.findDistinctByMessageListIn(list, pageable), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/files/{fileid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFilesById(@PathVariable @NotNull String id, @PathVariable @NotNull String fileid) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final HttpHeaders headers = new HttpHeaders();
            String currentUrl = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
            headers.setLocation(URI.create(NVFUtils.getBaseUrl(currentUrl) + "NVM/files/" + fileid + "/"));
            return new ResponseEntity<>(HttpStatus.MOVED_PERMANENTLY);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/{id}/files", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEntityByIdPostFile(@PathVariable @NotNull String id, @RequestBody @NotNull FileNvf fileNvf) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (service.verifyAddFileRequest(entity.get(), fileNvf).isPresent()) {
                return service.verifyAddFileRequest(entity.get(), fileNvf).get();
            }
            service.addFileToMessage(entity.get(), fileNvf);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/{id}/files")
    public ResponseEntity<?> postEntityByIdPostFileMultipartForm(@PathVariable @NotNull String id, @RequestParam("file") @NotNull MultipartFile file) {
        try {
            Optional<String> fileId = service.addFileToMessage(id, file);
            final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}/files/{fileId}", id, fileId.get());
            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        } catch (me.novascomp.utils.standalone.service.exceptions.SecurityException exception) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}/files/{fileId}")
    public ResponseEntity<?> deleteEntityByIdDeleteFile(@PathVariable @NotNull String id, @PathVariable @NotNull String fileId) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (service.verifyDeleteFileRequest(entity.get(), fileId).isPresent()) {
                return service.verifyDeleteFileRequest(entity.get(), fileId).get();
            }
            service.removeFileFromMessage(entity.get(), fileId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/REs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdREs(@PathVariable
            @NotNull String id) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(entity.get().getReList(), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/priorities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPriorities(@PathVariable @NotNull String id, Pageable pageable) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List list = new ArrayList();
            list.add(entity.get());
            return new ResponseEntity<>(priorityService.findDistinctByMessageListIn(list, pageable), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/{id}/priorities", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEntityByIdPostPriority(@PathVariable
            @NotNull String id, @RequestBody
            @NotNull Priority priority) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (service.verifyAddPriorityRequest(entity.get(), priority).isPresent()) {
                return service.verifyAddPriorityRequest(entity.get(), priority).get();
            }
            service.addPriorityToMessage(entity.get(), priority);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/priorities/{priorityid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdPriorityById(@PathVariable @NotNull String id, @PathVariable @NotNull String priorityid) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isPresent()) {
            for (Priority priority : entity.get().getPriorityList()) {
                if (priority.getPriorityId().equals(priorityid)) {
                    return new ResponseEntity<>(priority, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}/priorities/{priorityId}")
    public ResponseEntity<?> deleteEntityByIdDeletePriority(@PathVariable
            @NotNull String id, @PathVariable
            @NotNull String priorityId) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (service.verifyDeletePriorityRequest(entity.get(), priorityId).isPresent()) {
                return service.verifyDeletePriorityRequest(entity.get(), priorityId).get();
            }
            service.removePriorityFromMessage(entity.get(), priorityId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdCategories(@PathVariable
            @NotNull String id, Pageable pageable) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List list = new ArrayList();
            list.add(entity.get());
            return new ResponseEntity<>(categoryService.findDistinctByMessageListIn(list, pageable), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/{id}/categories", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postEntityByIdPostCategory(@PathVariable @NotNull String id, @RequestBody @NotNull Category category) {
        try {
            service.addCategoryToMessage(id, category.getCategoryId());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/categories/{categoryid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdCategoryById(@PathVariable @NotNull String id, @PathVariable @NotNull String categoryid) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isPresent()) {
            for (Category category : entity.get().getCategoryList()) {
                if (category.getCategoryId().equals(categoryid)) {
                    return new ResponseEntity<>(category, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}/categories/{categoryId}")
    public ResponseEntity<?> deleteEntityByIdDeleteCategory(@PathVariable
            @NotNull String id, @PathVariable
            @NotNull String categoryId) {
        final Optional<Message> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (service.verifyDeleteCategoryRequest(entity.get(), categoryId).isPresent()) {
                return service.verifyDeleteCategoryRequest(entity.get(), categoryId).get();
            }
            service.removeCategoryFromMessage(entity.get().getMessageId(), categoryId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
