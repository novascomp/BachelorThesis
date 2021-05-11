package me.novascomp.flat.repository;

import java.util.List;
import me.novascomp.utils.repository.GeneralRepository;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.Token;
import me.novascomp.flat.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TokenRepository extends PagingAndSortingRepository<Token, String>, CrudRepository<Token, String>, GeneralRepository<Token, String> {

    public Optional<Token> findByKey(String key);

    public Page<Token> findByFlat(Flat flat, Pageable pageable);

    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    //@QueryHints({
    //    @QueryHint(name = "javax.persistence.lock.timeout", value = "300000000")})
    public List<Token> findByUserId(User userId);

    public boolean existsByKey(String key);
}
