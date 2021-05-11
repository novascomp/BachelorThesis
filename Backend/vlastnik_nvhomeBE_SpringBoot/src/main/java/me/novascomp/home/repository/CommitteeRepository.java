package me.novascomp.home.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import me.novascomp.home.model.Committee;
import me.novascomp.utils.repository.GeneralRepository;

public interface CommitteeRepository extends PagingAndSortingRepository<Committee, String>, CrudRepository<Committee, String>, GeneralRepository<Committee, String> {

    public Optional<Committee> findByEmail(String email);

    public Optional<Committee> findByPhone(String phone);

    public boolean existsByEmail(String email);

    public boolean existsByPhone(String phone);
}
