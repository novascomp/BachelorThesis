package me.novascomp.files.services;

import java.util.Optional;
import java.util.UUID;
import me.novascomp.files.model.General;
import me.novascomp.files.model.User;
import me.novascomp.files.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public Optional<User> findByUid(String uid) {
        return repository.findByUid(uid);
    }

    public boolean existsByUid(String uid) {
        return repository.existsByUid(uid);
    }
}
