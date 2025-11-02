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
public class PaymentDto extends BaseDto {

    private Long id;
    @NotNull @Digits(integer = 10, fraction = 2) @PositiveOrZero private java.math.BigDecimal amount;
    @NotNull @Size(max = 32) private String paymentMethod;
    @NotNull @Size(max = 16) private String paymentType;
    private Long salesOrderId;

}
