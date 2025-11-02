package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Users;
import com.stock.stock_management.dto.UsersDto;

@Mapper(config = BaseMapperConfig.class)
public interface UsersMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "warehouse", ignore = true)
    })
    Users toEntity(UsersDto dto);

    @Mappings({
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    UsersDto toDto(Users entity);

    List<UsersDto> toDtoList(List<Users> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UsersDto dto, @MappingTarget Users entity);
}
