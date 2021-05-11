package me.novascomp.files.repository;

import java.util.List;
import java.util.Optional;
import me.novascomp.files.model.Folder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends PagingAndSortingRepository<Folder, String>, CrudRepository<Folder, String>, GeneralRepository<Folder, String> {

    public List<Optional<Folder>> findByName(String name);

    public boolean existsByName(String name);
}
