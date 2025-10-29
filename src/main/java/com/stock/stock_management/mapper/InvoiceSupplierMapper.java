package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.SupplierCommande;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.InvoiceSupplier;
import com.stock.stock_management.dto.InvoiceSupplierDto;

@Mapper(config = BaseMapperConfig.class)
public interface InvoiceSupplierMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "supplierCommande", ignore = true)
    })
    InvoiceSupplier toEntity(InvoiceSupplierDto dto);

    @Mappings({
        @Mapping(source = "supplierCommande.id", target = "supplierCommandeId")
    })
    InvoiceSupplierDto toDto(InvoiceSupplier entity);

    List<InvoiceSupplierDto> toDtoList(List<InvoiceSupplier> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(InvoiceSupplierDto dto, @MappingTarget InvoiceSupplier entity);
}
