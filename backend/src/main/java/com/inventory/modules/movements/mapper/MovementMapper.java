package com.inventory.modules.movements.mapper;

import com.inventory.modules.movements.dto.MovementLineDto;
import com.inventory.modules.movements.dto.MovementResponseDto;
import com.inventory.modules.movements.entity.StockMovement;
import com.inventory.modules.movements.entity.StockMovementItem;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class MovementMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public MovementResponseDto toResponseDto(StockMovement movement) {
        if (movement == null) return null;
        return MovementResponseDto.builder()
                .id(String.valueOf(movement.getId()))
                .code(movement.getCode())
                .type(movement.getMovementType() != null ? movement.getMovementType().name() : null)
                .date(movement.getCreatedAt() != null ? movement.getCreatedAt().format(ISO_FORMATTER) : null)
                .userName(movement.getUser() != null ? movement.getUser().getFullName() : null)
                .reason(movement.getReason())
                .lines(movement.getItems() != null ? movement.getItems().stream()
                        .map(this::toLineDto)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }

    public MovementLineDto toLineDto(StockMovementItem item) {
        if (item == null) return null;
        return MovementLineDto.builder()
                .productId(item.getProduct() != null ? String.valueOf(item.getProduct().getId()) : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .quantity(item.getQuantity())
                .stockBefore(item.getPreviousStock())
                .stockAfter(item.getNewStock())
                .build();
    }
}
