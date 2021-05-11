package me.novascomp.home.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import me.novascomp.home.model.Committee;
import me.novascomp.home.model.Member;
import me.novascomp.utils.repository.GeneralRepository;

public interface MemberRepository extends PagingAndSortingRepository<Member, String>, CrudRepository<Member, String>, GeneralRepository<Member, String> {

    public Optional<Member> findByEmail(String email);

    public boolean existsByEmail(String email);

    public Page<Member> findDistinctByCommitteeListIn(List<Committee> committeeList, Pageable pageable);
}
