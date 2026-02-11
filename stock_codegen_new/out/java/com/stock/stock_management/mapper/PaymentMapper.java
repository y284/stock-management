package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.SalesOrder;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Payment;
import com.stock.stock_management.dto.PaymentDto;

@Mapper(config = BaseMapperConfig.class)
public interface PaymentMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "salesOrder", ignore = true)
    })
    Payment toEntity(PaymentDto dto);

    @Mappings({
        @Mapping(source = "salesOrder.id", target = "salesOrderId")
    })
    PaymentDto toDto(Payment entity);

    List<PaymentDto> toDtoList(List<Payment> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PaymentDto dto, @MappingTarget Payment entity);
}
