package me.novascomp.home.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import me.novascomp.home.model.File;
import me.novascomp.utils.repository.GeneralRepository;

@Repository
public interface FileRepository extends PagingAndSortingRepository<File, String>, CrudRepository<File, String>, GeneralRepository<File, String> {

    public Optional<File> findByIdNvm(String idNvm);

    public boolean existsByIdNvm(String idNvm);
}
