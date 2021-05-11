package me.novascomp.messages.repository;

import me.novascomp.messages.model.Re;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ReRepository extends PagingAndSortingRepository<Re, String>, CrudRepository<Re, String>, GeneralRepository<Re, String> {

    public Page<Re> findByCreatorKey(String creatorKey, Pageable pageable);

    public boolean existsByCreatorKey(String creatorKey);
}
