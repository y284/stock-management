package com.stock.stock_management.repository;

import com.stock.stock_management.entity.Entreprise;

public interface EntrepriseRepository extends BaseRepository<Entreprise, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    java.util.Optional<Entreprise> findByNameIgnoreCase(String name);

}
