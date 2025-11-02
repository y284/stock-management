package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.SalesOrder;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.SalesInvoice;
import com.stock.stock_management.dto.SalesInvoiceDto;

@Mapper(config = BaseMapperConfig.class)
public interface SalesInvoiceMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "salesOrder", ignore = true)
    })
    SalesInvoice toEntity(SalesInvoiceDto dto);

    @Mappings({
        @Mapping(source = "salesOrder.id", target = "salesOrderId")
    })
    SalesInvoiceDto toDto(SalesInvoice entity);

    List<SalesInvoiceDto> toDtoList(List<SalesInvoice> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SalesInvoiceDto dto, @MappingTarget SalesInvoice entity);
}
