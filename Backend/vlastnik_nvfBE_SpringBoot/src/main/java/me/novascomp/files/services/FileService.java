package me.novascomp.files.services;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.files.model.File;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.General;
import me.novascomp.files.model.Server;
import me.novascomp.files.model.User;
import me.novascomp.files.repository.FileRepository;
import me.novascomp.files.server.webdav.response.WebDavFileResponse;
import me.novascomp.files.server.webdav.FileWebdavRequestor;
import me.novascomp.files.services.requirement.FileRequirement;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class FileService extends GeneralService<File, FileRepository> {

    private final ServerService serverService;
    private final UserService userService;
    private final FolderService folderService;

    @Autowired
    public FileService(ServerService serverService, UserService userService, FolderService folderService) {
        this.serverService = serverService;
        this.userService = userService;
        this.folderService = folderService;
    }

    public WebDavFileResponse addFile(MultipartFile file, String serverID, String folderID, String userID) throws ServiceException{

        String futureFileID = UUID.randomUUID().toString();
        FileRequirement fileRequirement = getRequirement(file.getOriginalFilename(), serverID, folderID, userID);
        if (fileRequirement.isValid()) {
            FileWebdavRequestor fileRequestor = new FileWebdavRequestor(nvfUtils, fileRequirement.getServer().get());
            HttpStatus httpStatus = fileRequestor.createFile(fileRequirement.getFolder().get(), file, futureFileID);
            if (httpStatus.is2xxSuccessful()) {
                File createdFileRecord = null;
                if (httpStatus == HttpStatus.CREATED) {
                    createdFileRecord = addFile(futureFileID, fileRequirement);
                }
                return new WebDavFileResponse(Optional.ofNullable(createdFileRecord), fileRequirement, httpStatus);
            }
        }
        return new WebDavFileResponse(Optional.ofNullable(null), fileRequirement, HttpStatus.BAD_REQUEST);
    }

    public HttpStatus removeFile(File file) throws ServiceException{
        FileWebdavRequestor fileRequestor = new FileWebdavRequestor(nvfUtils, file.getServerId());
        HttpStatus httpStatus = fileRequestor.deleteFile(file);

        if (httpStatus == HttpStatus.NO_CONTENT) {
            repository.delete(file);
        }

        return httpStatus;
    }

    private File addFile(String id, FileRequirement fileRequirement) {
        File file = null;
        if (fileRequirement.isValid()) {
            file = new File();
            file.setFileId(id);
            General general = nvfUtils.getGeneral(id);
            file.setGeneral(general);
            file.setName(fileRequirement.getOriginalFilename().get());
            file.setFolderId(fileRequirement.getFolder().get());
            file.setServerId(fileRequirement.getServer().get());
            file.setUserId(fileRequirement.getUser().get());
            repository.save(file);
        }
        return file;
    }

    private FileRequirement getRequirement(String originalFilename, String serverID, String folderID, String userID) {
        final Optional<Server> server = serverService.findById(serverID);
        final Optional<Folder> folder = folderService.findById(folderID);
        final Optional<User> user = userService.findById(userID);

        return new FileRequirement(Optional.ofNullable(originalFilename), server, folder, user);
    }

}
