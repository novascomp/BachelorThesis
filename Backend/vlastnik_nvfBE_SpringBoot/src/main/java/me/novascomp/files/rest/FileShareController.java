package me.novascomp.files.rest;

import com.sun.istack.NotNull;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import me.novascomp.files.model.File;
import me.novascomp.files.model.FileShare;
import static me.novascomp.files.rest.GeneralController.LOG;
import static me.novascomp.files.rest.GeneralController.exceptionToHttpStatusCode;
import me.novascomp.files.server.ocs.response.OcsFileShareResponse;
import me.novascomp.files.services.FileService;
import me.novascomp.files.services.FileShareService;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@RestController
@RequestMapping("/shares/files")
public class FileShareController extends GeneralController<FileShare, FileShareService> {

    private final FileService fileService;

    @Autowired
    public FileShareController(FileService fileService) {
        this.fileService = fileService;
    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @GetMapping(value = "/{id}/forbidden", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<?> getEntityById(@PathVariable @NotNull String id) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @GetMapping(value = "/{fileId}/shares/{fileShareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityById(@PathVariable @NotNull String fileId, @PathVariable @NotNull String fileShareId) {

        final Optional<FileShare> entity = service.findById(fileShareId);
        if (entity.isEmpty() || !fileService.existsById(fileId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            if (!fileService.findById(fileId).get().getFileShareList().contains(entity.get())) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        entity.get().setLink(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[0] + "//" + ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[2] + "/NVF/shares/files/" + fileId + "/shares/" + fileShareId + "/download");
        return new ResponseEntity<>(entity.get(), HttpStatus.OK);

    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @GetMapping(value = "/{fileId}")
    public ResponseEntity<?> getFileShares(@PathVariable @NotNull String fileId, Pageable pageable) {

        final Optional<File> entity = fileService.findById(fileId);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Page<FileShare> page = service.findByFileId(entity.get(), pageable);
            page.getContent().forEach((FileShare share) -> {
                share.setLink(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[0] + "//" + ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[2] + "/NVF/shares/files/" + fileId + "/shares/" + share.getFileShareId() + "/download");
            });
            return new ResponseEntity<>(page, HttpStatus.OK
            );
        }
    }

    @GetMapping(value = "/{fileId}/shares/{fileShareId}/download")
    public ResponseEntity<?> startDownload(@PathVariable @NotNull String fileId, @PathVariable @NotNull String fileShareId) {
        if (!service.existsById(fileShareId) || !fileService.existsById(fileId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(service.createTokenForDownloadServlet(fileShareId), HttpStatus.MOVED_PERMANENTLY);
        }
    }

    @PostMapping
    public ResponseEntity<?> postFileShare(@RequestBody @NotNull File file) {
        try {
            OcsFileShareResponse ocsFileShareResponse = service.addFileShare(file);
            if (ocsFileShareResponse.getHttpStatus() == HttpStatus.CREATED) {
                LOG.log(Level.INFO, ocsFileShareResponse.toString());
                return createdStatus(ocsFileShareResponse.getHttpStatus(), ocsFileShareResponse.getModel().get().getFileShareId());
            }
            return new ResponseEntity<>(ocsFileShareResponse.getHttpStatus());
        } catch (ServiceException serviceException) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(serviceException);
            return new ResponseEntity<>(httpStatus);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @Override
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteEntityById(@PathVariable @NotNull String id) {
        try {
            final Optional<FileShare> entity = service.findById(id);
            if (entity.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(service.removeFileShare(entity.get()));
            }
        } catch (ServiceException serviceException) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(serviceException);
            return new ResponseEntity<>(httpStatus);
        }
    }
}
