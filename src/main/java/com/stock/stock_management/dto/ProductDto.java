package com.stock.stock_management.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class ProductDto extends BaseDto {

    private Long id;
    @NotNull @Size(max = 64) private String sku;
    @NotNull @Size(max = 255) private String name;
    @Digits(integer = 10, fraction = 2) @PositiveOrZero private java.math.BigDecimal price;
    private Long categoryId;
    private Long tva;

}
