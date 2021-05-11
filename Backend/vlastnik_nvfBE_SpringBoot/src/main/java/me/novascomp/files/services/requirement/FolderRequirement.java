package me.novascomp.files.services.requirement;

import java.util.Optional;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.Server;
import me.novascomp.files.model.User;

public class FolderRequirement extends Requirement {

    private final Optional<String> folderName;
    private final Optional<Server> server;
    private final Optional<Folder> previousFolder;
    private final Optional<User> folderUser;

    public FolderRequirement(Optional<String> folderName, Optional<Server> server, Optional<Folder> previousFolder, Optional<User> folderUser) {
        this.folderName = folderName;
        this.server = server;
        this.previousFolder = previousFolder;
        this.folderUser = folderUser;
    }

    public Optional<String> getFolderName() {
        return folderName;
    }

    public Optional<Server> getServer() {
        return server;
    }

    public Optional<Folder> getPreviousFolder() {
        return previousFolder;
    }

    public Optional<User> getFolderUser() {
        return folderUser;
    }

    @Override
    public boolean isValid() {
        return folderName.isPresent() && server.isPresent() && folderUser.isPresent();
    }

}
