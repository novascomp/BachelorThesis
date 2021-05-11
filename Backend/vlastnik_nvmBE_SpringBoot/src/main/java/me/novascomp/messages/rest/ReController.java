package me.novascomp.messages.rest;

import com.sun.istack.NotNull;
import me.novascomp.messages.model.Re;
import me.novascomp.messages.service.ReService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/REs")
public class ReController extends GeneralController<Re, ReService> {

    public ReController() {
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postRe(@RequestBody @NotNull Re re) {
        if (service.verifyReRequest(re).isPresent()) {
            return service.verifyReRequest(re).get();
        }
        service.addRe(re);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", re.getReId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/bycreator/{creatorKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByCreatorKey(@PathVariable @NotNull String creatorKey, Pageable pageable) {
        return new ResponseEntity<>(service.findByCreatorKey(creatorKey, pageable), HttpStatus.OK);
    }

}
