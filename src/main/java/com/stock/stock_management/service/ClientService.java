package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.ClientDto;

public interface ClientService {

    ClientDto create(ClientDto dto);

    ClientDto update(Long id, ClientDto dto);

    ClientDto patch(Long id, ClientDto dto);

    Optional<ClientDto> findById(Long id);

    Optional<ClientDto> findByUuid(UUID uuid);

    List<ClientDto> findAll();

    Page<ClientDto> findAll(Pageable pageable);

    List<ClientDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
