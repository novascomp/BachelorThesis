package me.novascomp.files.rest;

import com.sun.istack.NotNull;
import java.util.Optional;
import java.util.logging.Level;
import me.novascomp.files.model.File;
import me.novascomp.files.server.webdav.response.WebDavFileResponse;
import me.novascomp.files.services.FileService;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController extends GeneralController<File, FileService> {

    @PostMapping
    public ResponseEntity<?> postFile(@RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("serverID") @NotNull String serverID, @RequestParam("folderID") @NotNull String folderID,
            @RequestParam("userID") @NotNull String userID) {
        try {
            WebDavFileResponse webDavFileResponse = service.addFile(file, serverID, folderID, userID);
            if (webDavFileResponse.getHttpStatus() == HttpStatus.CREATED) {
                LOG.log(Level.INFO, webDavFileResponse.toString());
                return createdStatus(webDavFileResponse.getHttpStatus(), webDavFileResponse.getModel().get().getFileId());
            }
            return new ResponseEntity<>(webDavFileResponse.getHttpStatus());
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
            final Optional<File> entity = service.findById(id);
            if (entity.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(service.removeFile(entity.get()));
            }
        } catch (ServiceException serviceException) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(serviceException);
            return new ResponseEntity<>(httpStatus);
        }
    }
}
