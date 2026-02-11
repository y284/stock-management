package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Product;
import com.stock.stock_management.entity.SalesOrder;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SalesOrderLine;
import com.stock.stock_management.dto.SalesOrderLineDto;

@Mapper(config = BaseMapperConfig.class)
public interface SalesOrderLineMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "salesOrder", ignore = true),
        @Mapping(target = "product", ignore = true)
    })
    SalesOrderLine toEntity(SalesOrderLineDto dto);

    @Mappings({
        @Mapping(source = "salesOrder.id", target = "salesOrderId"),
        @Mapping(source = "product.id", target = "productId")
    })
    SalesOrderLineDto toDto(SalesOrderLine entity);

    List<SalesOrderLineDto> toDtoList(List<SalesOrderLine> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SalesOrderLineDto dto, @MappingTarget SalesOrderLine entity);
}
