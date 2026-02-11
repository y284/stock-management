package com.stock.stock_management.mapper;

import com.stock.stock_management.entity.Category;

import org.mapstruct.*;
import java.util.*;
import com.stock.stock_management.entity.Product;
import com.stock.stock_management.dto.ProductDto;

@Mapper(config = BaseMapperConfig.class)
public interface ProductMapper extends BaseAuditMapper {

    @Mappings({
        @Mapping(target = "category", ignore = true)
    })
    Product toEntity(ProductDto dto);

    @Mappings({
        @Mapping(source = "category.id", target = "categoryId")
    })
    ProductDto toDto(Product entity);

    List<ProductDto> toDtoList(List<Product> entities);

    // PATCH: update only non-null properties from DTO -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductDto dto, @MappingTarget Product entity);
}
