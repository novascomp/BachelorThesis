package me.novascomp.files.server.ocs.response;

import java.util.Optional;
import me.novascomp.files.model.FileShare;
import me.novascomp.files.server.webdav.response.WebDavResponse;
import me.novascomp.files.services.requirement.FileShareRequirement;
import org.springframework.http.HttpStatus;

public class OcsFileShareResponse extends WebDavResponse<FileShare, FileShareRequirement> {

    public OcsFileShareResponse(Optional<FileShare> model, FileShareRequirement requirement, HttpStatus httpStatus) {
        super(model, requirement, httpStatus);
    }

}
