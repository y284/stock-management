package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.UsersDto;

public interface UsersService {

    UsersDto create(UsersDto dto);

    UsersDto update(Long id, UsersDto dto);

    UsersDto patch(Long id, UsersDto dto);

    Optional<UsersDto> findById(Long id);

    Optional<UsersDto> findByUuid(UUID uuid);

    List<UsersDto> findAll();

    Page<UsersDto> findAll(Pageable pageable);

    List<UsersDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
