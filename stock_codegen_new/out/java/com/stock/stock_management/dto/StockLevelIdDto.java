package com.stock.stock_management.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevelIdDto implements Serializable {

    @NotNull private Long productId;
    @NotNull private Long warehouseId;

}
