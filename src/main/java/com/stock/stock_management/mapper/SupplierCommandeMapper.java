package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Supplier;
import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SupplierCommande;
import com.stock.stock_management.dto.SupplierCommandeDto;

@Mapper(config = BaseMapperConfig.class)
public interface SupplierCommandeMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "warehouse", ignore = true)
    })
    SupplierCommande toEntity(SupplierCommandeDto dto);

    @Mappings({
        @Mapping(source = "supplier.id", target = "supplierId"),
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    SupplierCommandeDto toDto(SupplierCommande entity);

    List<SupplierCommandeDto> toDtoList(List<SupplierCommande> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SupplierCommandeDto dto, @MappingTarget SupplierCommande entity);
}
