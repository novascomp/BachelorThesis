package me.novascomp.microservice.nvf;

import java.util.Optional;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.services.requirement.FileViaMicroserviceRequirement;
import me.novascomp.microservice.communication.MicroserviceResponse;
import org.springframework.http.HttpStatus;

public class FileViaMicroserviceResponse extends MicroserviceResponse<FileNvf, FileViaMicroserviceRequirement> {

    public FileViaMicroserviceResponse(Optional<FileNvf> model, FileViaMicroserviceRequirement requirement, HttpStatus httpStatus) {
        super(model, requirement, httpStatus);
    }

}
