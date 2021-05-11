package me.novascomp.files.repository;

import me.novascomp.files.model.File;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends PagingAndSortingRepository<File, String>, CrudRepository<File, String>, GeneralRepository<File, String> {
}
