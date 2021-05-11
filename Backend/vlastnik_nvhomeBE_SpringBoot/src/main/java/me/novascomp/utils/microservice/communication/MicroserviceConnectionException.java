package me.novascomp.utils.microservice.communication;

import me.novascomp.utils.standalone.service.exceptions.ServiceException;

public class MicroserviceConnectionException extends ServiceException {

    public MicroserviceConnectionException(String message) {
        super(message);
    }

}
