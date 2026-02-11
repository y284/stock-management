package com.stock.stock_management.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.stock.stock_management.entity.BaseEntity;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    Optional<T> findByUuid(UUID uuid);
    boolean existsByUuid(UUID uuid);
    boolean existsByUuidAndIdNot(UUID uuid, ID id);

    /** Obtain a reference proxy without hitting the DB (throws on first access if missing). */
    default T getRef(ID id) {
        return getReferenceById(id);
    }
}
