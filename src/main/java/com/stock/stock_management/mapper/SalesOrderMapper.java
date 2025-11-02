package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Client;
import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SalesOrder;
import com.stock.stock_management.dto.SalesOrderDto;

@Mapper(config = BaseMapperConfig.class)
public interface SalesOrderMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "client", ignore = true),
        @Mapping(target = "warehouse", ignore = true)
    })
    SalesOrder toEntity(SalesOrderDto dto);

    @Mappings({
        @Mapping(source = "client.id", target = "clientId"),
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    SalesOrderDto toDto(SalesOrder entity);

    List<SalesOrderDto> toDtoList(List<SalesOrder> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SalesOrderDto dto, @MappingTarget SalesOrder entity);
}
