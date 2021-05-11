package me.novascomp.messages.rest;

import com.sun.istack.NotNull;
import java.util.Optional;
import me.novascomp.messages.model.Priority;
import me.novascomp.messages.service.PriorityService;
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

@RestController
@RequestMapping("/priorities")
public class PriorityController extends GeneralController<Priority, PriorityService> {

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postPriority(@RequestBody @NotNull Priority priority) {
        if (service.verifyPriorityRequest(priority).isPresent()) {
            return service.verifyPriorityRequest(priority).get();
        }
        service.addPriority(priority);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", priority.getPriorityId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/bycreator/{creatorKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByCreatorKey(@PathVariable @NotNull String creatorKey, Pageable pageable) {
        return new ResponseEntity<>(service.findByCreatorKey(creatorKey, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@PathVariable @NotNull String id) {
        final Optional<Priority> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.deletePriority(entity.get());
            return new ResponseEntity<>(entity.get(), HttpStatus.NO_CONTENT);
        }
    }
}
