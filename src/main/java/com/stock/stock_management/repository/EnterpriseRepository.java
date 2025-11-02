package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Enterprise;

public interface EnterpriseRepository extends BaseRepository<Enterprise, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    java.util.Optional<Enterprise> findByNameIgnoreCase(String name);

}
