package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Entreprise;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Warehouse;
import com.stock.stock_management.dto.WarehouseDto;

@Mapper(config = BaseMapperConfig.class)
public interface WarehouseMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "entreprise", ignore = true)
    })
    Warehouse toEntity(WarehouseDto dto);

    @Mappings({
        @Mapping(source = "entreprise.id", target = "entrepriseId")
    })
    WarehouseDto toDto(Warehouse entity);

    List<WarehouseDto> toDtoList(List<Warehouse> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(WarehouseDto dto, @MappingTarget Warehouse entity);
}
