package me.novascomp.utils.config.security;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.home.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationListener implements ApplicationListener<AuthorizationFailureEvent> {

    private final UserService userService;
    private static final Logger LOG = Logger.getLogger(AuthorizationListener.class.getName());

    @Autowired
    public AuthorizationListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(AuthorizationFailureEvent e) {
        if (e.getAuthentication().getPrincipal() instanceof Jwt) {
            Jwt principal = ((Jwt) e.getAuthentication().getPrincipal());
            String userUid = userService.getUserUidByPrincipal(principal);
            LOG.log(Level.WARNING, "User with user uid: {0} Authorization failure.", userUid);
        } else {
            LOG.log(Level.WARNING, "Anonymous user request failure.");
        }

        Map<String, Object> data = getMapData(e);
        LOG.log(Level.WARNING, "Request Authorization failure: {0}", data.get("source"));
    }

    private Map<String, Object> getMapData(AuthorizationFailureEvent e) {
        Map<String, Object> data = new HashMap<>();
        data.put(
                "type", e.getAccessDeniedException().getClass().getName());
        data.put("message", e.getAccessDeniedException().getMessage());
        data.put("source", (e.getSource()).toString());
        return data;
    }

}
