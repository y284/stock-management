package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Users;

public interface UsersRepository extends BaseRepository<Users, Long> {

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    java.util.Optional<Users> findByUsernameIgnoreCase(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    java.util.Optional<Users> findByEmailIgnoreCase(String email);
    boolean existsByKeycloakId(String keycloakId);
    boolean existsByKeycloakIdAndIdNot(String keycloakId, Long id);
    java.util.Optional<Users> findByKeycloakIdIgnoreCase(String keycloakId);
    long countByWarehouseId(Long warehouseId);

}
