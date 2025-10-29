package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Supplier;
import com.stock.stock_management.dto.SupplierDto;

@Mapper(config = BaseMapperConfig.class)
public interface SupplierMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "warehouse", ignore = true)
    })
    Supplier toEntity(SupplierDto dto);

    @Mappings({
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    SupplierDto toDto(Supplier entity);

    List<SupplierDto> toDtoList(List<Supplier> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SupplierDto dto, @MappingTarget Supplier entity);
}
