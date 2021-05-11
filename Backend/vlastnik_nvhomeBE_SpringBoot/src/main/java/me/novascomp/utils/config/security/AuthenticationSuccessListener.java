package me.novascomp.utils.config.security;

import java.util.logging.Logger;
import me.novascomp.home.service.UserService;
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
        userService.registerUserToApp(principal);
    }
}
