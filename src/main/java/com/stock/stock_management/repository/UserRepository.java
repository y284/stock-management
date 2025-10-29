package com.stock.stock_management.repository;

import com.stock.stock_management.entity.User;

public interface UserRepository extends BaseRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    java.util.Optional<User> findByUsernameIgnoreCase(String username);
    long countByWarehouseId(Long warehouseId);

}
