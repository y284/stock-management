package com.stock.stock_management.repository;

import com.stock.stock_management.entity.User;

public interface UserRepository extends BaseRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    java.util.Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByRib(String rib);
    boolean existsByRibAndIdNot(String rib, Long id);
    java.util.Optional<User> findByRibIgnoreCase(String rib);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    java.util.Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByKeycloakId(String keycloakId);
    boolean existsByKeycloakIdAndIdNot(String keycloakId, Long id);
    java.util.Optional<User> findByKeycloakIdIgnoreCase(String keycloakId);
    long countByWarehouseId(Long warehouseId);

}
