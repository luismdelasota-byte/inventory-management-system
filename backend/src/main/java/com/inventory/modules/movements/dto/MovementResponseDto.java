package com.inventory.modules.movements.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementResponseDto {
    private String id;
    private String code;
    private String type;
    private String date;
    private String userName;
    private String reason;
    private List<MovementLineDto> lines;
}
