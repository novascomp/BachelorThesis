package me.novascomp.flat.repository;

import java.util.List;
import me.novascomp.utils.repository.GeneralRepository;
import java.util.Optional;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.Resident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResidentRepository extends PagingAndSortingRepository<Resident, String>, CrudRepository<Resident, String>, GeneralRepository<Resident, String> {

    public Optional<Resident> findById(String id);

    public Optional<Resident> findByEmail(String email);

    public boolean existsByEmail(String email);

    public Page<Resident> findDistinctByDetailListIn(List<Detail> detailList, Pageable pageable);
}
