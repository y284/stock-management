package com.stock.stock_management.service;

import java.util.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.stock.stock_management.dto.PaymentDto;

public interface PaymentService {

    PaymentDto create(PaymentDto dto);

    PaymentDto update(Long id, PaymentDto dto);

    PaymentDto patch(Long id, PaymentDto dto);

    Optional<PaymentDto> findById(Long id);

    Optional<PaymentDto> findByUuid(UUID uuid);

    List<PaymentDto> findAll();

    Page<PaymentDto> findAll(Pageable pageable);

    List<PaymentDto> findAll(Sort sort);

    void deleteById(Long id);

    void deleteByUuid(UUID uuid);
}
