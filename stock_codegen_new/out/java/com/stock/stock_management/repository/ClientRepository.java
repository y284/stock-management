package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Client;

public interface ClientRepository extends BaseRepository<Client, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    java.util.Optional<Client> findByEmailIgnoreCase(String email);
    boolean existsByRib(String rib);
    boolean existsByRibAndIdNot(String rib, Long id);
    java.util.Optional<Client> findByRibIgnoreCase(String rib);
    long countByWarehouseId(Long warehouseId);

}
