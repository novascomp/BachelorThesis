package me.novascomp.messages.config.security;

import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.messages.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserService userService;

    private static final Logger LOG = Logger.getLogger(AuthenticationSuccessListener.class.getName());

    @Autowired
    public AuthenticationSuccessListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Jwt principal = ((Jwt) event.getAuthentication().getPrincipal());
        String id = userService.registerUserToApp(principal);
        //LOG.log(Level.INFO, "User logged in with ID: {0} and UID: {1}", new Object[]{id, userService.getUserUidByPrincipal(principal)});
    }

}
