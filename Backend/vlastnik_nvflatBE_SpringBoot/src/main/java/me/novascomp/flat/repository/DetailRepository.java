package me.novascomp.flat.repository;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import me.novascomp.utils.repository.GeneralRepository;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.MessageRecordNvm;
import me.novascomp.flat.model.Resident;

public interface DetailRepository extends PagingAndSortingRepository<Detail, String>, CrudRepository<Detail, String>, GeneralRepository<Detail, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<Detail> findById(String id);

    public Optional<Detail> findByFlat(Flat flat);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Page<Detail> findByFlat(Flat flat, Pageable pageable);

    public List<Detail> findDistinctByMessageRecordNvmListIn(List<MessageRecordNvm> messageRecordNvmList);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Page<Detail> findByMessageRecordNvmListIn(List<MessageRecordNvm> messageRecordNvmList, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Page<Detail> findByResidentListIn(List<Resident> residentList, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public List<Detail> findByResidentListIn(List<Resident> residentList);
}
