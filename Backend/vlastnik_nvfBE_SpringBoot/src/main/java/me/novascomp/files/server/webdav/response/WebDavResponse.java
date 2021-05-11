package me.novascomp.files.server.webdav.response;

import java.util.Optional;
import java.util.logging.Logger;
import me.novascomp.files.services.requirement.Requirement;
import org.springframework.http.HttpStatus;

public abstract class WebDavResponse<Model, T extends Requirement> {

    protected final Optional<Model> model;
    protected final T requirement;
    protected final HttpStatus httpStatus;

    private static final Logger LOG = Logger.getLogger(WebDavResponse.class.getName());

    public WebDavResponse(Optional<Model> model, T requirement, HttpStatus httpStatus) {
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
        return "WebDavResponse{" + "model=" + model + ", requirement=" + requirement + ", httpStatus=" + httpStatus + '}';
    }

}
