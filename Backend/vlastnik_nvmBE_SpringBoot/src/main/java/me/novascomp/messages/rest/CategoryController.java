package me.novascomp.messages.rest;

import com.sun.istack.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import me.novascomp.messages.model.Category;
import me.novascomp.messages.service.CategoryService;
import me.novascomp.messages.service.MessageService;
import me.novascomp.microservice.nvm.model.LightweightCategory;

@RestController
@RequestMapping("/categories")
public class CategoryController extends GeneralController<Category, CategoryService> {

    MessageService messageService;

    @Autowired
    public CategoryController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postCategory(Principal principal, @RequestBody @NotNull Category category) {
        category.setText(category.getText().toUpperCase());
        if (service.verifyCategoryRequest(category).isPresent()) {
            return service.verifyCategoryRequest(category).get();
        }
        service.addCategory(category);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", category.getCategoryId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postListOfCategories(Principal principal, @RequestBody @NotNull List<Category> categories) {
        categories.forEach((category) -> {
            postCategory(principal, category);
        });
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(value = "/list/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteListOfCategories(Principal principal, @RequestBody @NotNull List<Category> categories) {
        categories.forEach((category) -> {
            deleteEntityById(service.findByCreatorKeyAndText(category.getCreatorKey(), category.getText()).getCategoryId());
        });
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/bycreatorandtext", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCategoryByCreatorAndByText(Principal principal, @RequestBody @NotNull LightweightCategory category) {
        Category categoryRecord = service.findByCreatorKeyAndText(category.getCreatorKey(), category.getText());
        return new ResponseEntity<>(categoryRecord, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/bycreator/{creatorKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByCreatorKey(@PathVariable @NotNull String creatorKey, Pageable pageable) {
        return new ResponseEntity<>(service.findByCreatorKey(creatorKey, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@PathVariable @NotNull String id) {
        final Optional<Category> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<String> messagesToRemove = new ArrayList<>();
            entity.get().getMessageList().forEach((message) -> {
                messagesToRemove.add(message.getMessageId());
            });

            messagesToRemove.forEach((messageId) -> {
                messageService.removeCategoryFromMessage(messageId, id);
            });

            service.delete(entity.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
