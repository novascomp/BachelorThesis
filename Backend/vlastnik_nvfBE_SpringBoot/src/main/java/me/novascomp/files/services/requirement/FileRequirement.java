package me.novascomp.files.services.requirement;

import java.util.Optional;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.Server;
import me.novascomp.files.model.User;

public class FileRequirement extends Requirement {

    private final Optional<String> originalFilename;
    private final Optional<Server> server;
    private final Optional<Folder> folder;
    private final Optional<User> user;

    public FileRequirement(Optional<String> originalFileName, Optional<Server> server, Optional<Folder> folder, Optional<User> user) {
        this.originalFilename = originalFileName;
        this.server = server;
        this.folder = folder;
        this.user = user;
    }

    public Optional<String> getOriginalFilename() {
        return originalFilename;
    }

    public Optional<Server> getServer() {
        return server;
    }

    public Optional<Folder> getFolder() {
        return folder;
    }

    public Optional<User> getUser() {
        return user;
    }

    @Override
    public boolean isValid() {
        return server.isPresent() && folder.isPresent() && user.isPresent() && originalFilename.isPresent();
    }

}
