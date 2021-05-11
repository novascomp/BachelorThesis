package me.novascomp.messages.services.requirement;

import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public class FileViaMicroserviceRequirement extends Requirement {

    private final Optional<MultipartFile> file;

    public FileViaMicroserviceRequirement(Optional<MultipartFile> file) {
        this.file = file;
    }

    public Optional<MultipartFile> getFile() {
        return file;
    }

    @Override
    public boolean isValid() {
        return file.isPresent();
    }

}
