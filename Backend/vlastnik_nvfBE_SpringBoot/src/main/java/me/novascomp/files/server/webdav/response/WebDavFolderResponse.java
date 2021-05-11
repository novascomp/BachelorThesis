package me.novascomp.files.server.webdav.response;

import java.util.Optional;
import me.novascomp.files.model.Folder;
import me.novascomp.files.services.requirement.FolderRequirement;
import org.springframework.http.HttpStatus;

public class WebDavFolderResponse extends WebDavResponse<Folder, FolderRequirement> {

    public WebDavFolderResponse(Optional<Folder> model, FolderRequirement requirement, HttpStatus httpStatus) {
        super(model, requirement, httpStatus);
    }

}
