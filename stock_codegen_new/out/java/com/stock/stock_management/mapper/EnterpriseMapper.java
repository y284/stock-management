package com.stock.stock_management.mapper;


import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Enterprise;
import com.stock.stock_management.dto.EnterpriseDto;

@Mapper(config = BaseMapperConfig.class)
public interface EnterpriseMapper extends BaseAuditMapper {

    Enterprise toEntity(EnterpriseDto dto);

    EnterpriseDto toDto(Enterprise entity);

    List<EnterpriseDto> toDtoList(List<Enterprise> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(EnterpriseDto dto, @MappingTarget Enterprise entity);
}
