package me.novascomp.files.server.webdav.response;

import java.util.Optional;
import me.novascomp.files.model.File;
import me.novascomp.files.services.requirement.FileRequirement;
import org.springframework.http.HttpStatus;

public class WebDavFileResponse extends WebDavResponse<File, FileRequirement> {

    public WebDavFileResponse(Optional<File> model, FileRequirement requirement, HttpStatus httpStatus) {
        super(model, requirement, httpStatus);
    }

}
