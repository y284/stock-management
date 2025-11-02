package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Product;
import com.stock.stock_management.entity.PurchaseOrder;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.PurchaseOrderLine;
import com.stock.stock_management.dto.PurchaseOrderLineDto;

@Mapper(config = BaseMapperConfig.class)
public interface PurchaseOrderLineMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "purchaseOrder", ignore = true),
        @Mapping(target = "product", ignore = true)
    })
    PurchaseOrderLine toEntity(PurchaseOrderLineDto dto);

    @Mappings({
        @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId"),
        @Mapping(source = "product.id", target = "productId")
    })
    PurchaseOrderLineDto toDto(PurchaseOrderLine entity);

    List<PurchaseOrderLineDto> toDtoList(List<PurchaseOrderLine> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PurchaseOrderLineDto dto, @MappingTarget PurchaseOrderLine entity);
}
