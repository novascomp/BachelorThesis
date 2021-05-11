package me.novascomp.files.rest;

import com.sun.istack.NotNull;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.novascomp.files.model.Folder;
import static me.novascomp.files.rest.GeneralController.exceptionToHttpStatusCode;
import me.novascomp.files.server.webdav.response.WebDavFolderResponse;
import me.novascomp.files.services.FolderService;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@RestController
@RequestMapping("/folders")
public class FolderController extends GeneralController<Folder, FolderService> {

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postFolder(@RequestBody @NotNull Folder folder) {
        try {
            WebDavFolderResponse webDavFolderResponse = service.addFolder(folder);
            if (webDavFolderResponse.getHttpStatus() == HttpStatus.CREATED) {
                LOG.log(Level.INFO, webDavFolderResponse.toString());
                return createdStatus(webDavFolderResponse.getHttpStatus(), webDavFolderResponse.getModel().get().getFolderId());
            }
            return new ResponseEntity<>(webDavFolderResponse.getHttpStatus());
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
            final Optional<Folder> entity = service.findById(id);
            if (entity.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(service.removeFolder(entity.get()));
            }
        } catch (ServiceException serviceException) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(serviceException);
            return new ResponseEntity<>(httpStatus);
        }
    }
}
