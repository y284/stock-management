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
    @NotNull @Size(max = 64) private String username;
    @Size(max = 64) private String firstname;
    @Size(max = 64) private String lastname;
    @Size(max = 128) @Email private String email;
    private Long warehouseId;

}
