package com.inventory.modules.movements.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovementRequestDto {

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<MovementItemRequestDto> items;
}
