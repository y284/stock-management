package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.UserDto;

public interface UserService {

    UserDto create(UserDto dto);

    UserDto update(Long id, UserDto dto);

    UserDto patch(Long id, UserDto dto);

    Optional<UserDto> findById(Long id);

    Optional<UserDto> findByUuid(UUID uuid);

    List<UserDto> findAll();

    Page<UserDto> findAll(Pageable pageable);

    List<UserDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
