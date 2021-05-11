package me.novascomp.messages.rest;

import com.sun.istack.NotNull;
import java.util.Optional;
import java.util.logging.Logger;
import me.novascomp.messages.service.GeneralService;
import me.novascomp.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.CreatedException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public abstract class GeneralController<T, Service extends GeneralService> {

    @Autowired
    protected Service service;

    protected static final Logger LOG = Logger.getLogger(GeneralController.class.getName());

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityById(@PathVariable @NotNull String id) {
        final Optional<T> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(entity.get(), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@PathVariable @NotNull String id) {
        final Optional<T> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.delete(entity.get());
            return new ResponseEntity<>(entity.get(), HttpStatus.NO_CONTENT);
        }
    }

    public HttpStatus exceptionToHttpStatusCode(ServiceException exception) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (exception instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        if (exception instanceof ConflictException) {
            httpStatus = HttpStatus.CONFLICT;
        }

        if (exception instanceof InternalException) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (exception instanceof CreatedException) {
            httpStatus = HttpStatus.CREATED;
        }

        if (exception instanceof MicroserviceConnectionException) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        }

        return httpStatus;
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<T> getAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    protected ResponseEntity<?> createdStatus(HttpStatus httpStatus, String id) {
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", id);
        return new ResponseEntity<>(headers, httpStatus);
    }

    public Service getService() {
        return service;
    }
}
