package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.PurchaseOrder;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.PurchaseInvoice;
import com.stock.stock_management.dto.PurchaseInvoiceDto;

@Mapper(config = BaseMapperConfig.class)
public interface PurchaseInvoiceMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "purchaseOrder", ignore = true)
    })
    PurchaseInvoice toEntity(PurchaseInvoiceDto dto);

    @Mappings({
        @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId")
    })
    PurchaseInvoiceDto toDto(PurchaseInvoice entity);

    List<PurchaseInvoiceDto> toDtoList(List<PurchaseInvoice> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PurchaseInvoiceDto dto, @MappingTarget PurchaseInvoice entity);
}
