package me.novascomp.files.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.istack.NotNull;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.files.download.DownloadSessionService;
import me.novascomp.files.model.File;
import me.novascomp.files.model.FileShare;
import me.novascomp.files.model.General;
import me.novascomp.files.repository.FileShareRepository;
import me.novascomp.files.rest.FileShareController;
import me.novascomp.files.server.ocs.FileShareOcsRequestor;
import me.novascomp.files.server.ocs.model.create.OcsCreateResponse;
import me.novascomp.files.server.ocs.model.delete.OcsDeleteResponse;
import me.novascomp.files.server.ocs.response.OcsFileShareResponse;
import me.novascomp.files.services.requirement.FileShareDownloadRequirement;
import me.novascomp.files.services.requirement.FileShareRequirement;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class FileShareService extends GeneralService<FileShare, FileShareRepository> {

    private final FileService fileService;
    private final DownloadSessionService downloadSessionService;

    private static final Logger LOG = Logger.getLogger(FileShareService.class.getName());

    @Autowired
    public FileShareService(FileService fileService, DownloadSessionService downloadService) {
        this.fileService = fileService;
        this.downloadSessionService = downloadService;
    }

    public OcsFileShareResponse addFileShare(@NotNull File file) throws ServiceException {
        FileShareRequirement fileShareRequirement = getRequirement(file.getFileId());

        if (fileShareRequirement.isValid()) {
            FileShareOcsRequestor fileShareOcsRequestor = new FileShareOcsRequestor(nvfUtils, objectMapper, fileShareRequirement.getFile().get().getServerId());
            HttpResponse<String> httpResponse = fileShareOcsRequestor.createBasicPublicShare(fileShareRequirement.getFile().get());
            OcsCreateResponse ocsResponse = getOcsPojo(httpResponse);

            if (HttpStatus.valueOf(ocsResponse.getOcs().getMeta().getStatuscode()) == HttpStatus.CONTINUE) {
                return new OcsFileShareResponse(Optional.ofNullable(addFileShare(fileShareRequirement, ocsResponse)), fileShareRequirement, HttpStatus.CREATED);
            }
            return new OcsFileShareResponse(Optional.ofNullable(null), fileShareRequirement, getStatusCode(httpResponse));
        }
        return new OcsFileShareResponse(Optional.ofNullable(null), fileShareRequirement, HttpStatus.BAD_REQUEST);
    }

    public HttpStatus removeFileShare(FileShare fileShare) throws ServiceException {
        FileShareOcsRequestor fileShareOcsRequestor = new FileShareOcsRequestor(nvfUtils, objectMapper, fileShare.getFileId().getServerId());
        HttpResponse<String> httpResponse = fileShareOcsRequestor.deleteBasicPublicShare(fileShare.getFileShareId());

        OcsDeleteResponse ocsResponse = getDeleteOcsResponsePojo(httpResponse);
        if (HttpStatus.valueOf(ocsResponse.getOcs().getMeta().getStatuscode()) == HttpStatus.CONTINUE) {
            repository.delete(fileShare);
            return HttpStatus.NO_CONTENT;
        }

        return HttpStatus.valueOf(ocsResponse.getOcs().getMeta().getStatuscode());
    }

    public HttpHeaders createTokenForDownloadServlet(String fileShareID) {
        FileShareDownloadRequirement fileShareRequirement = getDownloadRequirement(fileShareID);
        HttpHeaders headers = new HttpHeaders();
        if (fileShareRequirement.isValid()) {
            headers.setLocation(URI.create(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[0] + "//" + ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString().split("/")[2] + "/NVF/DownloadServlet/byToken/" + downloadSessionService.addFileShare(fileShareRequirement.getFileShare().get())));
        }
        return headers;
    }

    public Page<FileShare> findByFileId(File file, Pageable pageable) {
        return repository.findByFileId(file, pageable);
    }

    private OcsCreateResponse getOcsPojo(HttpResponse<String> httpResponse) {
        OcsCreateResponse ocsResponse = null;
        try {
            ocsResponse = objectMapper.readValue(httpResponse.body(), OcsCreateResponse.class);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(FileShareController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ocsResponse;
    }

    private OcsDeleteResponse getDeleteOcsResponsePojo(HttpResponse<String> httpResponse) {
        OcsDeleteResponse ocsDeleteResponse = null;
        try {
            ocsDeleteResponse = objectMapper.readValue(httpResponse.body(), OcsDeleteResponse.class);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(FileShareService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ocsDeleteResponse;
    }

    private FileShare addFileShare(FileShareRequirement fileShareRequirement, OcsCreateResponse ocsResponse) {
        FileShare fileShare = null;
        if (fileShareRequirement.isValid()) {
            fileShare = new FileShare();
            String id = ocsResponse.getOcs().getData().getId();
            fileShare.setFileShareId(id);
            General general = nvfUtils.getGeneral(id);
            fileShare.setGeneral(general);
            fileShare.setFileId(fileShareRequirement.getFile().get());
            fileShare.setLink(ocsResponse.getOcs().getData().getUrl());
            repository.save(fileShare);
        }
        return fileShare;
    }

    private FileShareRequirement getRequirement(String fileID) {
        final Optional<File> file = fileService.findById(fileID);
        return new FileShareRequirement(file);
    }

    private FileShareDownloadRequirement getDownloadRequirement(String fileShareID) {
        final Optional<FileShare> fileShare = findById(fileShareID);
        return new FileShareDownloadRequirement(fileShare);
    }
}
