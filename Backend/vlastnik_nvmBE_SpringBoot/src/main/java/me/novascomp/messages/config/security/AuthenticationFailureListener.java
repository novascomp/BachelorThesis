package me.novascomp.messages.config.security;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private static final Logger LOG = Logger.getLogger(AuthenticationFailureListener.class.getName());

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        LOG.log(Level.WARNING, "Authentication failure: {0}", e.getSource().toString());
    }

}
