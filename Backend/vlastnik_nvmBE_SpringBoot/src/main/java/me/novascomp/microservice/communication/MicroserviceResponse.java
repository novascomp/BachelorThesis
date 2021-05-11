package me.novascomp.microservice.communication;

import java.util.Optional;
import java.util.logging.Logger;
import me.novascomp.messages.services.requirement.Requirement;
import org.springframework.http.HttpStatus;

public abstract class MicroserviceResponse<Model, T extends Requirement> {

    protected final Optional<Model> model;
    protected final T requirement;
    protected final HttpStatus httpStatus;

    private static final Logger LOG = Logger.getLogger(MicroserviceResponse.class.getName());

    public MicroserviceResponse(Optional<Model> model, T requirement, HttpStatus httpStatus) {
        this.model = model;
        this.requirement = requirement;
        this.httpStatus = httpStatus;
    }

    public Optional<Model> getModel() {
        return model;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public boolean isRequirementValid() {
        return requirement.isValid();
    }

    @Override
    public String toString() {
        return "MicroserviceResponse{" + "model=" + model + ", requirement=" + requirement + ", httpStatus=" + httpStatus + '}';
    }

}
