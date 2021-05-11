package me.novascomp.messages.repository;

import java.util.List;
import java.util.Optional;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PriorityRepository extends PagingAndSortingRepository<Priority, String>, CrudRepository<Priority, String>, GeneralRepository<Priority, String> {

    public Optional<Priority> findByText(String text);

    public boolean existsByText(String text);

    public Page<Priority> findByCreatorKey(String creatorKey, Pageable pageable);

    public List<Priority> findByCreatorKey(String creatorKey);

    public boolean existsByCreatorKey(String creatorKey);

    public Page<Priority> findDistinctByMessageListIn(List<Message> messageList, Pageable pageable);
}
