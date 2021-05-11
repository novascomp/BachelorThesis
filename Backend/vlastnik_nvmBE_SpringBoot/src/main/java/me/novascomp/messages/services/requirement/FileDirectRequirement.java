package me.novascomp.messages.services.requirement;

import java.util.Optional;
import me.novascomp.messages.model.FileNvf;

public class FileDirectRequirement extends Requirement {

    private final Optional<FileNvf> file;
    private final Optional<String> fileInNvfId;

    public FileDirectRequirement(Optional<FileNvf> file, Optional<String> fileInNvfId) {
        this.file = file;
        this.fileInNvfId = fileInNvfId;
    }

    public Optional<FileNvf> getFile() {
        return file;
    }

    public Optional<String> getFileInNvfId() {
        return fileInNvfId;
    }

    @Override
    public boolean isValid() {
        boolean fileInNvfOk = false;
        if (fileInNvfId.isPresent()) {
            fileInNvfOk = (fileInNvfId.get().length() < 50);
        }
        return file.isEmpty() && fileInNvfOk;
    }

    @Override
    public String toString() {
        return "FileDirectRequirement{" + "file=" + file + ", fileInNvfId=" + fileInNvfId + '}';
    }

}
