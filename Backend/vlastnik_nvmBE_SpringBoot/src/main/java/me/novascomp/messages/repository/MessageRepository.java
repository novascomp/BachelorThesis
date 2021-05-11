package me.novascomp.messages.repository;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import me.novascomp.messages.model.Category;
import me.novascomp.messages.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MessageRepository extends PagingAndSortingRepository<Message, String>, CrudRepository<Message, String>, GeneralRepository<Message, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<Message> findById(String id);

    public Page<Message> findByCreatorKey(String creatorKey, Pageable pageable);

    public boolean existsByCreatorKey(String creatorKey);

    //   @Lock(LockModeType.PESSIMISTIC_WRITE)
    public List<Message> findByCategoryListIn(List<Category> categoryList, Pageable pageable);

    //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    public List<Message> findDistinctByCategoryListIn(List<Category> categoryList, Pageable pageable);
}
