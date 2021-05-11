package me.novascomp.home.repository;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import me.novascomp.utils.repository.GeneralRepository;
import me.novascomp.home.model.Organization;
import me.novascomp.home.model.Token;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface OrganizationRepository extends PagingAndSortingRepository<Organization, String>, CrudRepository<Organization, String>, GeneralRepository<Organization, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<Organization> findById(String id);

    public Optional<Organization> findByIco(String ico);

    public Page<Organization> findDistinctByTokenListIn(List<Token> tokenList, Pageable pageable);

    public boolean existsByIco(String ico);
}
