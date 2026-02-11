package com.stock.stock_management.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.UUID;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
public class BaseDto {
    private UUID uuid;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;
}
