package com.stock.stock_management.mapper;


import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.StockLevel;
import com.stock.stock_management.dto.StockLevelDto;

@Mapper(config = BaseMapperConfig.class)
public interface StockLevelMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(source = "id.productId", target = "id.productId"),
        @Mapping(source = "id.warehouseId", target = "id.warehouseId"),
        @Mapping(target = "product", ignore = true),
        @Mapping(target = "warehouse", ignore = true)
    })
    StockLevel toEntity(StockLevelDto dto);

    @Mappings({
        @Mapping(source = "id.productId", target = "id.productId"),
        @Mapping(source = "id.warehouseId", target = "id.warehouseId")
    })
    StockLevelDto toDto(StockLevel entity);

    List<StockLevelDto> toDtoList(List<StockLevel> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(StockLevelDto dto, @MappingTarget StockLevel entity);
}
