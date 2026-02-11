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
public class PurchaseOrderLineDto extends BaseDto {

    private Long id;
    @NotNull private Long purchaseOrderId;
    @NotNull private Long productId;
    @NotNull @Digits(integer = 11, fraction = 3) @PositiveOrZero private java.math.BigDecimal quantity;
    @NotNull @Digits(integer = 10, fraction = 2) @PositiveOrZero private java.math.BigDecimal unitPrice;
    @NotNull @Digits(integer = 1, fraction = 4) private java.math.BigDecimal discount;

}
