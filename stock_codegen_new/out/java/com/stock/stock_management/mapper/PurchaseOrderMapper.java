package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Supplier;
import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.PurchaseOrder;
import com.stock.stock_management.dto.PurchaseOrderDto;

@Mapper(config = BaseMapperConfig.class)
public interface PurchaseOrderMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "supplier", ignore = true),
        @Mapping(target = "warehouse", ignore = true)
    })
    PurchaseOrder toEntity(PurchaseOrderDto dto);

    @Mappings({
        @Mapping(source = "supplier.id", target = "supplierId"),
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    PurchaseOrderDto toDto(PurchaseOrder entity);

    List<PurchaseOrderDto> toDtoList(List<PurchaseOrder> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PurchaseOrderDto dto, @MappingTarget PurchaseOrder entity);
}
