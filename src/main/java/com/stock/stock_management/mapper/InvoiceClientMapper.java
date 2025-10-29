package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.SaleCommande;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.InvoiceClient;
import com.stock.stock_management.dto.InvoiceClientDto;

@Mapper(config = BaseMapperConfig.class)
public interface InvoiceClientMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "saleCommande", ignore = true)
    })
    InvoiceClient toEntity(InvoiceClientDto dto);

    @Mappings({
        @Mapping(source = "saleCommande.id", target = "saleCommandeId")
    })
    InvoiceClientDto toDto(InvoiceClient entity);

    List<InvoiceClientDto> toDtoList(List<InvoiceClient> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(InvoiceClientDto dto, @MappingTarget InvoiceClient entity);
}
