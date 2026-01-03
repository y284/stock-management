package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.User;
import com.stock.stock_management.dto.UserDto;

@Mapper(config = BaseMapperConfig.class)
public interface UserMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "warehouse", ignore = true)
    })
    User toEntity(UserDto dto);

    @Mappings({
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    UserDto toDto(User entity);

    List<UserDto> toDtoList(List<User> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserDto dto, @MappingTarget User entity);
}
