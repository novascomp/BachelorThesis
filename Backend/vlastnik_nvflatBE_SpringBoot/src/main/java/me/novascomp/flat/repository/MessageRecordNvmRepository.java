package me.novascomp.flat.repository;

import me.novascomp.utils.repository.GeneralRepository;
import java.util.Optional;
import me.novascomp.flat.model.MessageRecordNvm;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MessageRecordNvmRepository extends PagingAndSortingRepository<MessageRecordNvm, String>, CrudRepository<MessageRecordNvm, String>, GeneralRepository<MessageRecordNvm, String> {

    public Optional<MessageRecordNvm> findById(String id);

    public Optional<MessageRecordNvm> findByIdInNvm(String idInNvm);

    public boolean existsByIdInNvm(String idInNvm);
}
