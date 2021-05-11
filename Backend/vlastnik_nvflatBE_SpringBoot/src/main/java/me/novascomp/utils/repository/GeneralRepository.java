package me.novascomp.utils.repository;

import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralRepository<Model, Key> {

    //   public Page<Model> findAll(Pageable pageable);
}
