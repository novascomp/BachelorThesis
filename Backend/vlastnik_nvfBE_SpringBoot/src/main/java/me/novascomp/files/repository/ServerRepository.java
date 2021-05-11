package me.novascomp.files.repository;

import java.util.Optional;
import me.novascomp.files.model.Server;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends PagingAndSortingRepository<Server, String>, CrudRepository<Server, String>, GeneralRepository<Server, String> {

    public Optional<Server> findByBaseUrl(String baseUrl);

    public boolean existsByBaseUrl(String baseUrl);
}
