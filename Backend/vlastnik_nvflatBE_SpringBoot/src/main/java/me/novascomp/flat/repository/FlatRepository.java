package me.novascomp.flat.repository;

import java.util.List;
import java.util.Optional;
import me.novascomp.utils.repository.GeneralRepository;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.Organization;
import me.novascomp.flat.model.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlatRepository extends PagingAndSortingRepository<Flat, String>, CrudRepository<Flat, String>, GeneralRepository<Flat, String> {

    public Optional<Flat> findById(String id);

    public boolean existsByIdentifier(String identifier);

    public Page<Flat> findByOrganizationAndIdentifierNot(Organization organization, String identifier, Pageable pageable);

    public Optional<Flat> findByIdentifierAndOrganization(String identifier, Organization organization);

    public Page<Flat> findByTokenListIn(List<Token> tokenList, Pageable pageable);

    public List<Flat> findByTokenListIn(List<Token> tokenList);
}
