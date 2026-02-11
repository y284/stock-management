package com.stock.stock_management.mapper;

import org.mapstruct.*;
import com.stock.stock_management.entity.BaseEntity;
import com.stock.stock_management.dto.BaseDto;

@Mapper(config = BaseMapperConfig.class)
public interface BaseAuditMapper {

    BaseDto toDto(BaseEntity entity);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
        @Mapping(target = "uuid", source = "uuid"),
        @Mapping(target = "createdAt", source = "createdAt"),
        @Mapping(target = "updatedAt", source = "updatedAt"),
        @Mapping(target = "version", source = "version")
    })
    void copyAuditToDto(BaseEntity entity, @MappingTarget BaseDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
        @Mapping(target = "uuid", source = "uuid"),
        @Mapping(target = "createdAt", source = "createdAt"),
        @Mapping(target = "updatedAt", source = "updatedAt"),
        @Mapping(target = "version", source = "version")
    })
    void updateAuditFromDto(BaseDto dto, @MappingTarget BaseEntity entity);
}
