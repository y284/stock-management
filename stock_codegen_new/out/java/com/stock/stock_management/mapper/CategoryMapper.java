package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Category;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Category;
import com.stock.stock_management.dto.CategoryDto;

@Mapper(config = BaseMapperConfig.class)
public interface CategoryMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "parent", ignore = true)
    })
    Category toEntity(CategoryDto dto);

    @Mappings({
        @Mapping(source = "parent.id", target = "parentId")
    })
    CategoryDto toDto(Category entity);

    List<CategoryDto> toDtoList(List<Category> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoryDto dto, @MappingTarget Category entity);
}
