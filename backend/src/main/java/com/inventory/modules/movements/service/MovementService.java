package com.inventory.modules.movements.service;

import com.inventory.common.response.PagedResponse;
import com.inventory.modules.movements.dto.MovementRequestDto;
import com.inventory.modules.movements.dto.MovementResponseDto;
import com.inventory.modules.movements.entity.MovementType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovementService {
    MovementResponseDto createMovement(MovementType type, MovementRequestDto request, String username);
    MovementResponseDto getMovementById(Long id);
    List<MovementResponseDto> getAllMovementsList();
    PagedResponse<MovementResponseDto> getAllMovements(MovementType type, String search, Pageable pageable);
}
