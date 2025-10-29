package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Product;
import com.stock.stock_management.entity.SaleCommande;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SaleCommandeLine;
import com.stock.stock_management.dto.SaleCommandeLineDto;

@Mapper(config = BaseMapperConfig.class)
public interface SaleCommandeLineMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "saleCommande", ignore = true),
        @Mapping(target = "product", ignore = true)
    })
    SaleCommandeLine toEntity(SaleCommandeLineDto dto);

    @Mappings({
        @Mapping(source = "saleCommande.id", target = "saleCommandeId"),
        @Mapping(source = "product.id", target = "productId")
    })
    SaleCommandeLineDto toDto(SaleCommandeLine entity);

    List<SaleCommandeLineDto> toDtoList(List<SaleCommandeLine> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SaleCommandeLineDto dto, @MappingTarget SaleCommandeLine entity);
}
