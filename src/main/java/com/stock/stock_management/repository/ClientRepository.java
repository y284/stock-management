package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Client;

public interface ClientRepository extends BaseRepository<Client, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    java.util.Optional<Client> findByEmailIgnoreCase(String email);
    boolean existsByIban(String iban);
    boolean existsByIbanAndIdNot(String iban, Long id);
    java.util.Optional<Client> findByIbanIgnoreCase(String iban);
    long countByWarehouseId(Long warehouseId);

}
