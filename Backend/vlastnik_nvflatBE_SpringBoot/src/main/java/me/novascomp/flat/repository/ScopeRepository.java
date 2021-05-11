package me.novascomp.flat.repository;

import java.util.List;
import me.novascomp.utils.repository.GeneralRepository;
import java.util.Optional;
import me.novascomp.flat.model.Scope;
import me.novascomp.flat.model.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ScopeRepository extends PagingAndSortingRepository<Scope, String>, CrudRepository<Scope, String>, GeneralRepository<Scope, String> {

    public Optional<Scope> findByScope(String scope);

    public boolean existsByScope(String scope);

    public Page<Scope> findDistinctByTokenListIn(List<Token> tokenList, Pageable pageable);
}
