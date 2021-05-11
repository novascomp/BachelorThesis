package me.novascomp.flat.repository;

import java.util.List;
import me.novascomp.utils.repository.GeneralRepository;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrganizationRepository extends PagingAndSortingRepository<Organization, String>, CrudRepository<Organization, String>, GeneralRepository<Organization, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Override
    public Optional<Organization> findById(String id);

    public Optional<Organization> findByIco(String ico);

    public boolean existsByIco(String ico);

    public Page<Organization> findDistinctByFlatListIn(List<Flat> flatList, Pageable pageable);

    public List<Organization> findDistinctByFlatListIn(List<Flat> flatList);
}
