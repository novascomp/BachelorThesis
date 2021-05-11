package me.novascomp.messages.repository;

import java.util.List;
import java.util.Optional;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.model.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FileNvfRepository extends PagingAndSortingRepository<FileNvf, String>, CrudRepository<FileNvf, String>, GeneralRepository<FileNvf, String> {

    public Optional<FileNvf> findByFileIdInNvf(String fileIdInNvf);

    public boolean existsByFileIdInNvf(String fileIdInNvf);

    public Page<FileNvf> findByCreatorKey(String creatorKey, Pageable pageable);

    public boolean existsByCreatorKey(String creatorKey);

    public Page<FileNvf> findDistinctByMessageListIn(List<Message> messageList, Pageable pageable);
}
