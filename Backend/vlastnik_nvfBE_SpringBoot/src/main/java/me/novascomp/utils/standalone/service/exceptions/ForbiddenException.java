package me.novascomp.utils.standalone.service.exceptions;

public class ForbiddenException extends SecurityException {

    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }

}
