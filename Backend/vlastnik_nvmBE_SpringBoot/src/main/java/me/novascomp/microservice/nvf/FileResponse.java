package me.novascomp.microservice.nvf;

import java.util.Optional;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.services.requirement.FileDirectRequirement;
import me.novascomp.microservice.communication.MicroserviceResponse;
import org.springframework.http.HttpStatus;

public class FileResponse extends MicroserviceResponse<FileNvf, FileDirectRequirement> {

    public FileResponse(Optional<FileNvf> model, FileDirectRequirement requirement, HttpStatus httpStatus) {
        super(model, requirement, httpStatus);
    }

}
