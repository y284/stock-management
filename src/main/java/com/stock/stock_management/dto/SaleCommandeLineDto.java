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
public class SaleCommandeLineDto extends BaseDto {

    private Long id;
    @NotNull private Long saleCommandeId;
    @NotNull private Long productId;
    @NotNull private Long quantity;
    @NotNull @Digits(integer = 10, fraction = 2) @PositiveOrZero private java.math.BigDecimal price;
    @NotNull @Digits(integer = 0, fraction = 2) private java.math.BigDecimal discount;

}
