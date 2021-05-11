package me.novascomp.messages.service;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import me.novascomp.messages.model.General;
import me.novascomp.messages.model.User;
import me.novascomp.messages.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends GeneralService<User, UserRepository> {

    public UserService() {
    }

    public void addUser(User user) {
        String id = UUID.randomUUID().toString();
        user.setUserId(id);
        General general = nvfUtils.getGeneral(id);
        user.setGeneral(general);
        repository.save(user);
    }

    public Optional<ResponseEntity> verifyUserRequest(User user) {
        if (repository.findByUid(user.getUid()).isPresent()) {
            return Optional.ofNullable(new ResponseEntity<>("user UID", HttpStatus.CONFLICT));
        } else {
            return Optional.ofNullable(null);
        }
    }

    public String registerUserToApp(Jwt principal) {
        String uid = getUserUidByPrincipal(principal);
        User user = new User();

        if (!repository.existsByUid(uid)) {
            user.setUid(uid);
            addUser(user);
            LOG.log(Level.INFO, "New user added with uid: {0}", uid);
        }

        user = repository.findByUid(uid).get();
        return user.getUserId();
    }

    public String getUserUidByPrincipal(Jwt principal) {

        String uid = principal.getClaimAsString("uid");
        String cid = principal.getClaimAsString("cid");
        String userUid;

        if (uid == null) {
            userUid = cid;
        } else {
            userUid = uid;
        }

        return userUid;
    }

    public Optional<User> findByUid(String uid) {
        return repository.findByUid(uid);
    }

    public boolean existsByUid(String uid) {
        return repository.existsByUid(uid);
    }
}
