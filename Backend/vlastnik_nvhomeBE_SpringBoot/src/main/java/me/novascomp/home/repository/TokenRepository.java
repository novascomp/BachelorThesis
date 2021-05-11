package me.novascomp.home.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import me.novascomp.utils.repository.GeneralRepository;
import me.novascomp.home.model.Organization;
import me.novascomp.home.model.Token;

public interface TokenRepository extends PagingAndSortingRepository<Token, String>, CrudRepository<Token, String>, GeneralRepository<Token, String> {

    public Optional<Token> findByKey(String key);

    public Page<Token> findByOrganization(Organization organization, Pageable pageable);

    public boolean existsByKey(String key);
}
