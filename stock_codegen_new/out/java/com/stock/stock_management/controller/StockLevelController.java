package com.stock.stock_management.controller;

import com.stock.stock_management.dto.StockLevelDto;
import com.stock.stock_management.entity.StockLevelId;
import com.stock.stock_management.service.StockLevelService;
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
@RequestMapping(path = "/api/stock-level", produces = MediaType.APPLICATION_JSON_VALUE)
public class StockLevelController {

    private final StockLevelService service;

    public StockLevelController(StockLevelService service) {
        this.service = service;
    }

    // ===== Read =====
    @GetMapping
    public List<StockLevelDto> list() {
        return service.findAll();
    }

    @GetMapping("/page")
    public Page<StockLevelDto> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Sort sort) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return service.findAll(pageable);
    }

    @GetMapping("/sorted")
    public List<StockLevelDto> listSorted(Sort sort) {
        return service.findAll(sort);
    }

    // --- ID parts in path ---
    @GetMapping("/{product_id}/{warehouse_id}")
    public StockLevelDto get(@PathVariable("product_id") Long productId, @PathVariable("warehouse_id") Long warehouseId) {
        StockLevelId id = new StockLevelId(productId, warehouseId);
        return service.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("stockLevel not found with id parts"));
    }

    @GetMapping("/uuid/{uuid}")
    public StockLevelDto getByUuid(@PathVariable UUID uuid) {
        return service.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("stockLevel not found with uuid=" + uuid));
    }

    // ===== Create =====
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StockLevelDto> create(@Valid @RequestBody StockLevelDto dto) {
        StockLevelDto created = service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/uuid/{id}")
                .buildAndExpand(created.getUuid())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ===== Update (full replace) =====
    @PutMapping(path = "/{product_id}/{warehouse_id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StockLevelDto update(@PathVariable("product_id") Long productId, @PathVariable("warehouse_id") Long warehouseId, @Valid @RequestBody StockLevelDto dto) {
        StockLevelId id = new StockLevelId(productId, warehouseId);
        return service.update(id, dto);
    }

    // ===== Patch (partial) =====
    @PatchMapping(path = "/{product_id}/{warehouse_id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StockLevelDto patch(@PathVariable("product_id") Long productId, @PathVariable("warehouse_id") Long warehouseId, @RequestBody StockLevelDto dto) {
        StockLevelId id = new StockLevelId(productId, warehouseId);
        return service.patch(id, dto);
    }

    // ===== Delete =====
    @DeleteMapping("/{product_id}/{warehouse_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("product_id") Long productId, @PathVariable("warehouse_id") Long warehouseId) {
        StockLevelId id = new StockLevelId(productId, warehouseId);
        service.deleteById(id);
    }

    @DeleteMapping("/uuid/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUuid(@PathVariable UUID uuid) {
        service.deleteByUuid(uuid);
    }
}
