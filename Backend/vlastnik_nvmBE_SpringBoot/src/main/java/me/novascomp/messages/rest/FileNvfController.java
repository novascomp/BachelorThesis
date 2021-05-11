package me.novascomp.messages.rest;

import com.sun.istack.NotNull;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.service.FileNvfService;
import me.novascomp.microservice.nvf.FileResponse;
import me.novascomp.microservice.nvf.FileViaMicroserviceResponse;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/files")
public class FileNvfController extends GeneralController<FileNvf, FileNvfService> {

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postNvfFile(@RequestBody @NotNull FileNvf fileNvf) {

        try {
            FileResponse fileResponse = service.addFileDirect(fileNvf.getFileIdInNvf());
            if (fileResponse.getHttpStatus() == HttpStatus.CREATED) {
                return createdStatus(fileResponse.getHttpStatus(), fileResponse.getModel().get().getFileNvfId());
            }
            return new ResponseEntity<>(fileResponse.getHttpStatus());
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @PostMapping
    public ResponseEntity<?> postNvfFileMultipartForm(@RequestParam("file") @NotNull MultipartFile file) {

        try {
            FileViaMicroserviceResponse fileViaMicroserviceResponse = service.addFileViaNvfMicroservice(file);
            if (fileViaMicroserviceResponse.getHttpStatus() == HttpStatus.CREATED) {
                return createdStatus(fileViaMicroserviceResponse.getHttpStatus(), fileViaMicroserviceResponse.getModel().get().getFileNvfId());
            }
            return new ResponseEntity<>(fileViaMicroserviceResponse.getHttpStatus());
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        }

    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/bycreator/{creatorKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByCreatorKey(@PathVariable @NotNull String creatorKey, Pageable pageable) {
        return new ResponseEntity<>(service.findByCreatorKey(creatorKey, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @GetMapping(value = "/{id}/shares", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEntityByIdFilesByIdShares(@PathVariable @NotNull String id, Pageable pageable) {

        try {
            Page<?> page = service.getFileShares(id, pageable);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (ServiceException exception) {
            return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_nvmScope')")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<?> deleteEntityById(@PathVariable @NotNull String id) {

        final Optional<FileNvf> entity = service.findById(id);
        if (entity.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            try {
                service.deleteFile(entity.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (ServiceException exception) {
                return new ResponseEntity<>(exceptionToHttpStatusCode(exception));
            }
        }
    }
}
