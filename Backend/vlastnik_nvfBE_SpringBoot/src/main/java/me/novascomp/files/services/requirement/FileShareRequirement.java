package me.novascomp.files.services.requirement;

import java.util.Optional;
import me.novascomp.files.model.File;

public class FileShareRequirement extends Requirement {

    private final Optional<File> file;

    public FileShareRequirement(Optional<File> file) {
        this.file = file;
    }

    public Optional<File> getFile() {
        return file;
    }

    @Override
    public boolean isValid() {
        return file.isPresent();
    }

}
