package me.novascomp.flat.repository;

import me.novascomp.utils.repository.GeneralRepository;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import me.novascomp.flat.model.User;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

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
