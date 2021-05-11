package me.novascomp.utils.standalone.service.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.home.config.BeansInit;

public class ServiceException extends RuntimeException {

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    public ServiceException(String message) {
        super(message);
        LOG.log(Level.INFO, this.toString());
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
    }
}
