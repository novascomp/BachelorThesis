package me.novascomp.files.repository;

import me.novascomp.files.model.File;
import me.novascomp.files.model.FileShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileShareRepository extends PagingAndSortingRepository<FileShare, String>, CrudRepository<FileShare, String>, GeneralRepository<FileShare, String> {

    Page<FileShare> findByFileId(File file, Pageable pageable);
}
