package me.novascomp.files.repository;

import java.util.Optional;
import me.novascomp.files.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String>, CrudRepository<User, String>, GeneralRepository<User, String> {

    public Optional<User> findByUid(String uid);

    public boolean existsByUid(String uid);
}
