package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Client;
import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SaleCommande;
import com.stock.stock_management.dto.SaleCommandeDto;

@Mapper(config = BaseMapperConfig.class)
public interface SaleCommandeMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "client", ignore = true),
        @Mapping(target = "warehouse", ignore = true)
    })
    SaleCommande toEntity(SaleCommandeDto dto);

    @Mappings({
        @Mapping(source = "client.id", target = "clientId"),
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    SaleCommandeDto toDto(SaleCommande entity);

    List<SaleCommandeDto> toDtoList(List<SaleCommande> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SaleCommandeDto dto, @MappingTarget SaleCommande entity);
}
