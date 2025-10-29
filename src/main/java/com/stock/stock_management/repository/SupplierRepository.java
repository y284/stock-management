package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Supplier;

public interface SupplierRepository extends BaseRepository<Supplier, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    java.util.Optional<Supplier> findByEmailIgnoreCase(String email);
    boolean existsByRib(String rib);
    boolean existsByRibAndIdNot(String rib, Long id);
    java.util.Optional<Supplier> findByRibIgnoreCase(String rib);
    long countByWarehouseId(Long warehouseId);

}
