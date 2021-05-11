package me.novascomp.files.rest.simple;

import com.sun.istack.NotNull;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.files.config.BeansInit;
import me.novascomp.files.config.DatabaseSimpleInit;
import me.novascomp.files.model.File;
import me.novascomp.files.model.FileShare;
import me.novascomp.files.rest.FileController;
import me.novascomp.files.rest.FileShareController;
import static me.novascomp.files.rest.GeneralController.exceptionToHttpStatusCode;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@RestController
@RequestMapping("/simple/extended")
public class SimpleFileAndShareUploadControllerExtendedAndProtected {

    public final FileController fileController;
    public final FileShareController fileShareController;
    private final DatabaseSimpleInit databaseSimpleInit;

    private static final Logger LOG = Logger.getLogger(SimpleFileAndShareUploadController.class.getName());

    @Autowired
    public SimpleFileAndShareUploadControllerExtendedAndProtected(FileController fileController, FileShareController fileShareController, DatabaseSimpleInit databaseSimpleInit) {
        this.fileController = fileController;
        this.fileShareController = fileShareController;
        this.databaseSimpleInit = databaseSimpleInit;
        this.databaseSimpleInit.initDataForSimpleRest();
    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @PostMapping
    public ResponseEntity<?> postFile(@RequestParam("file") @NotNull MultipartFile file, @RequestParam("folder") Optional<String> folderParamName) {

        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        LOG.log(Level.INFO, "Simple extended controller");
        LOG.log(Level.INFO, "Receiving file: {0}", file.getOriginalFilename());

        String serverID = databaseSimpleInit.getSimpleServer().get().getServerId();
        String folderID = databaseSimpleInit.getSimpleFolder().get().getFolderId();
        String userID = databaseSimpleInit.getSimpleUser().get().getUserId();

        if (folderParamName.isPresent()) {
            // if (folderParamName.get().equals(databaseSimpleInit.getENVFolder().get().getName())) {
            //     folderID = databaseSimpleInit.getENVFolder().get().getFolderId();
            //     LOG.log(Level.INFO, "Uploading to folder name: {0}", folderParamName.get() + ", folder id: " + folderID);
            // }
        }

        try {
            ResponseEntity<?> fileControllerResponseEntity = fileController.postFile(file, serverID, folderID, userID);
            HttpStatus httpStatusFileUpload = HttpStatus.valueOf(fileControllerResponseEntity.getStatusCodeValue());
            if (httpStatusFileUpload != HttpStatus.CREATED) {
                LOG.log(Level.INFO, httpStatusFileUpload.toString());
                return new ResponseEntity<>(httpStatusFileUpload);
            }

            File fileRecord = new SimpleUtil<File, FileController>(fileController).getModelFromControllerResponse(fileControllerResponseEntity);
            LOG.log(Level.INFO, fileRecord.toString());

            ResponseEntity<?> fileShareControllerResponseEntity = fileShareController.postFileShare(fileRecord);
            HttpStatus httpStatusShareUpload = HttpStatus.valueOf(fileShareControllerResponseEntity.getStatusCodeValue());
            if (httpStatusShareUpload != HttpStatus.CREATED) {
                LOG.log(Level.INFO, httpStatusShareUpload.toString());
                return new ResponseEntity<>(httpStatusShareUpload);
            }

            FileShare fileShareRecord = new SimpleUtil<FileShare, FileShareController>(fileShareController).getModelFromControllerResponse(fileShareControllerResponseEntity);
            LOG.log(Level.INFO, fileShareRecord.toString());
            final HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[0] + "//" + ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[2] + "/NVF/shares/files/" + fileShareRecord.getFileId().getFileId() + "/shares/" + fileShareRecord.getFileShareId() + "/download"));
            headers.add("fileId", fileShareRecord.getFileId().getFileId());
            LOG.log(Level.INFO, headers.getFirst("Location"));
            LOG.log(Level.INFO, HttpStatus.CREATED.toString());
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);
            return new ResponseEntity<>(headers, HttpStatus.CREATED);
        } catch (ServiceException serviceException) {
            HttpStatus httpStatus = exceptionToHttpStatusCode(serviceException);
            return new ResponseEntity<>(httpStatus);
        }
    }
}
