package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Warehouse;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Client;
import com.stock.stock_management.dto.ClientDto;

@Mapper(config = BaseMapperConfig.class)
public interface ClientMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "warehouse", ignore = true)
    })
    Client toEntity(ClientDto dto);

    @Mappings({
        @Mapping(source = "warehouse.id", target = "warehouseId")
    })
    ClientDto toDto(Client entity);

    List<ClientDto> toDtoList(List<Client> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ClientDto dto, @MappingTarget Client entity);
}
