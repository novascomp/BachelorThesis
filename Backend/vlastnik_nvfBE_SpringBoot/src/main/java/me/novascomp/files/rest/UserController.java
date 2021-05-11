package me.novascomp.files.rest;

import com.sun.istack.NotNull;
import java.util.Collections;
import java.util.Map;
import me.novascomp.files.model.User;
import me.novascomp.files.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController extends GeneralController<User, UserService> {

    public UserController() {
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt principal) {
        return Collections.singletonMap("user_name", principal.getClaimAsString("preferred_username"));
    }

    @GetMapping("/key")
    public Map<String, Object> getUserKey(@AuthenticationPrincipal Jwt principal) {
        return Collections.singletonMap("user_key", principal.getClaimAsString("user_key"));
    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postUser(@RequestBody @NotNull User user) {
        if (service.verifyUserRequest(user).isPresent()) {
            return service.verifyUserRequest(user).get();
        }
        service.addUser(user);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", user.getUserId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

}
