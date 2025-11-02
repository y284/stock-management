package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Supplier;

public interface SupplierRepository extends BaseRepository<Supplier, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    java.util.Optional<Supplier> findByEmailIgnoreCase(String email);
    boolean existsByIban(String iban);
    boolean existsByIbanAndIdNot(String iban, Long id);
    java.util.Optional<Supplier> findByIbanIgnoreCase(String iban);
    long countByWarehouseId(Long warehouseId);

}
