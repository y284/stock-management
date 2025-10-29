package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Product;
import com.stock.stock_management.entity.SupplierCommande;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SupplierCommandeLine;
import com.stock.stock_management.dto.SupplierCommandeLineDto;

@Mapper(config = BaseMapperConfig.class)
public interface SupplierCommandeLineMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "supplierCommande", ignore = true),
        @Mapping(target = "product", ignore = true)
    })
    SupplierCommandeLine toEntity(SupplierCommandeLineDto dto);

    @Mappings({
        @Mapping(source = "supplierCommande.id", target = "supplierCommandeId"),
        @Mapping(source = "product.id", target = "productId")
    })
    SupplierCommandeLineDto toDto(SupplierCommandeLine entity);

    List<SupplierCommandeLineDto> toDtoList(List<SupplierCommandeLine> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SupplierCommandeLineDto dto, @MappingTarget SupplierCommandeLine entity);
}
