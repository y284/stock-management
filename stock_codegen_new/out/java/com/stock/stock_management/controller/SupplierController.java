package com.stock.stock_management.controller;

import com.stock.stock_management.dto.SupplierDto;
import com.stock.stock_management.service.SupplierService;
import com.stock.stock_management.error.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.*;
import java.util.UUID;

@RestController
@Validated
@RequestMapping(path = "/api/supplier", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    // ===== Read =====
    @GetMapping
    public List<SupplierDto> list() {
        return service.findAll();
    }

    @GetMapping("/page")
    public Page<SupplierDto> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Sort sort) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.findAll(pageable);
    }

    @GetMapping("/sorted")
    public List<SupplierDto> listSorted(Sort sort) {
        return service.findAll(sort);
    }

    @GetMapping("/{id}")
    public SupplierDto get(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("supplier not found with id=" + id));
    }

    @GetMapping("/uuid/{uuid}")
    public SupplierDto getByUuid(@PathVariable UUID uuid) {
        return service.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("supplier not found with uuid=" + uuid));
    }

    // ===== Create =====
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SupplierDto> create(@Valid @RequestBody SupplierDto dto) {
        SupplierDto created = service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/uuid/{id}")
                .buildAndExpand(created.getUuid())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ===== Update (full replace) =====
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SupplierDto update(@PathVariable Long id, @Valid @RequestBody SupplierDto dto) {
        return service.update(id, dto);
    }

    // ===== Patch (partial) =====
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SupplierDto patch(@PathVariable Long id, @RequestBody SupplierDto dto) {
        return service.patch(id, dto);
    }

    // ===== Delete =====
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteById(id);
    }

    @DeleteMapping("/uuid/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUuid(@PathVariable UUID uuid) {
        service.deleteByUuid(uuid);
    }

    private org.springframework.data.domain.Sort parseSort(List<String> sorts) {
        if (sorts == null || sorts.isEmpty()) return org.springframework.data.domain.Sort.unsorted();
        java.util.List<org.springframework.data.domain.Sort.Order> orders = new java.util.ArrayList<>();
        for (String s : sorts) {
            if (s == null || s.isBlank()) continue;
            String[] parts = s.split(",", 2);
            String prop = parts[0].trim();
            String dir = parts.length > 1 ? parts[1].trim() : "asc";
            orders.add(new org.springframework.data.domain.Sort.Order(
                    "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC
                                                : org.springframework.data.domain.Sort.Direction.ASC,
                    prop));
        }
        return org.springframework.data.domain.Sort.by(orders);
    }
}
