package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Warehouse;

public interface WarehouseRepository extends BaseRepository<Warehouse, Long> {

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    java.util.Optional<Warehouse> findByCodeIgnoreCase(String code);
    long countByEnterpriseId(Long enterpriseId);

}
