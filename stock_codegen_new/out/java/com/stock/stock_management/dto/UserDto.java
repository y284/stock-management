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
public class UserDto extends BaseDto {

    private Long id;
    @NotNull @Size(max = 128) private String username;
    @NotNull @Size(max = 128) private String firstname;
    @NotNull @Size(max = 128) private String lastname;
    @Size(max = 34) private String rib;
    @NotNull @Size(max = 255) @Email private String email;
    @NotNull @Size(max = 255) private String keycloakId;
    @NotNull private Long warehouseId;
    private Boolean isActive;

}
