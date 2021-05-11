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

public interface CategoryRepository extends PagingAndSortingRepository<Category, String>, CrudRepository<Category, String>, GeneralRepository<Category, String> {

  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<Category> findById(String id);

    public Optional<Category> findByText(String text);

    public boolean existsByText(String text);

    public Page<Category> findByCreatorKey(String creatorKey, Pageable pageable);

    public Category findByCreatorKeyAndText(String creatorKey, String text);

    public List<Category> findByCreatorKey(String creatorKey);

    public boolean existsByCreatorKey(String creatorKey);

    public Page<Category> findDistinctByMessageListIn(List<Message> messageList, Pageable pageable);
}
