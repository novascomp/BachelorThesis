package me.novascomp.files.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import me.novascomp.files.server.webdav.FolderWebdavRequestor;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.General;
import me.novascomp.files.model.Server;
import me.novascomp.files.model.User;
import me.novascomp.files.repository.FolderRepository;
import me.novascomp.files.server.webdav.response.WebDavFolderResponse;
import me.novascomp.files.services.requirement.FolderRequirement;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class FolderService extends GeneralService<Folder, FolderRepository> {

    private final ServerService serverService;
    private final UserService userService;

    @Autowired
    public FolderService(ServerService serverService, UserService userService) {
        this.serverService = serverService;
        this.userService = userService;
    }

    public WebDavFolderResponse addFolder(Folder folder) throws ServiceException {

        FolderRequirement folderRequirement = getFolderRequirement(folder);

        HttpStatus httpStatus = null;
        if (folderRequirement.isValid()) {
            FolderWebdavRequestor folderRequestor = null;
            if (folderRequirement.getPreviousFolder().isPresent()) {
                folderRequestor = new FolderWebdavRequestor(folderRequirement.getPreviousFolder().get(), folderRequirement.getServer().get(), nvfUtils);
            } else {
                folderRequestor = new FolderWebdavRequestor(folderRequirement.getServer().get(), nvfUtils);
            }
            httpStatus = folderRequestor.createFolder(folderRequirement.getFolderName().get());
            if (httpStatus.is2xxSuccessful()) {
                Folder createdFolderRecord = null;
                if (httpStatus == HttpStatus.CREATED) {
                    createdFolderRecord = addFolder(folderRequirement);
                }
                return new WebDavFolderResponse(Optional.ofNullable(createdFolderRecord), folderRequirement, httpStatus);
            }
        }

        return new WebDavFolderResponse(Optional.ofNullable(null), folderRequirement, HttpStatus.BAD_REQUEST);
    }

    private Folder addFolder(FolderRequirement folderRequirement) {

        Folder folder = null;
        if (folderRequirement.isValid()) {
            folder = new Folder();
            String id = UUID.randomUUID().toString();
            folder.setFolderId(id);
            General general = nvfUtils.getGeneral(id);
            folder.setGeneral(general);
            folder.setName(folderRequirement.getFolderName().get());
            if (folderRequirement.getPreviousFolder().isPresent()) {
                folder.setPreviousFolderId(folderRequirement.getPreviousFolder().get());
            }
            folder.setServerId(folderRequirement.getServer().get());
            folder.setUserId(folderRequirement.getFolderUser().get());
            repository.save(folder);

        }

        return folder;
    }

    public HttpStatus removeFolder(Folder folder) throws ServiceException {
        FolderWebdavRequestor folderRequestor = new FolderWebdavRequestor(folder.getPreviousFolderId(), folder.getServerId(), nvfUtils);
        HttpStatus httpStatus = folderRequestor.deleteFolder(folder.getName());

        if (httpStatus == HttpStatus.NO_CONTENT) {
            repository.delete(folder);
        }

        return httpStatus;
    }

    public FolderRequirement getFolderRequirement(Folder folder) {
        Optional<Server> server = Optional.ofNullable(folder.getServerId());
        Optional<Folder> previousFolder = Optional.ofNullable(folder.getPreviousFolderId());
        Optional<User> folderUser = Optional.ofNullable(folder.getUserId());

        if (server.isPresent() && folderUser.isPresent()) {
            server = serverService.findById(server.get().getServerId());
            folderUser = userService.findById(folderUser.get().getUserId());
        }

        if (previousFolder.isPresent()) {
            previousFolder = findById(previousFolder.get().getFolderId());
        }
        return new FolderRequirement(Optional.ofNullable(folder.getName()), server, previousFolder, folderUser);
    }

    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    public List<Optional<Folder>> findByName(String name) {
        return repository.findByName(name);
    }
}
