package me.novascomp.home.repository;

import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import me.novascomp.utils.repository.GeneralRepository;
import me.novascomp.home.model.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String>, CrudRepository<User, String>, GeneralRepository<User, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<User> findById(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<User> findByUid(String uid);

    public boolean existsByUid(String uid);
}
