package me.novascomp.files.services.requirement;

import java.util.Optional;
import me.novascomp.files.model.FileShare;

public class FileShareDownloadRequirement extends Requirement {

    private final Optional<FileShare> fileShare;

    public FileShareDownloadRequirement(Optional<FileShare> file) {
        this.fileShare = file;
    }

    public Optional<FileShare> getFileShare() {
        return fileShare;
    }

    @Override
    public boolean isValid() {
        return fileShare.isPresent();
    }

}
