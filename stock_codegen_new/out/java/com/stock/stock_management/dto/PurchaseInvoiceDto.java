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
public class PurchaseInvoiceDto extends BaseDto {

    private Long id;
    @NotNull private Long purchaseOrderId;
    private java.time.OffsetDateTime issueDate;
    private java.time.OffsetDateTime dueDate;
    @Digits(integer = 10, fraction = 2) @PositiveOrZero private java.math.BigDecimal totalAmount;
    @Digits(integer = 10, fraction = 2) @PositiveOrZero private java.math.BigDecimal paidAmount;
    @Size(max = 32) private String status;

}
