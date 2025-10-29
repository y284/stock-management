package com.stock.stock_management.mapper;


import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Entreprise;
import com.stock.stock_management.dto.EntrepriseDto;

@Mapper(config = BaseMapperConfig.class)
public interface EntrepriseMapper extends BaseAuditMapper {

    Entreprise toEntity(EntrepriseDto dto);

    EntrepriseDto toDto(Entreprise entity);

    List<EntrepriseDto> toDtoList(List<Entreprise> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(EntrepriseDto dto, @MappingTarget Entreprise entity);
}
