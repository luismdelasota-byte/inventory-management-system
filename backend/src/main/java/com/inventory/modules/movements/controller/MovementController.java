package com.inventory.modules.movements.controller;

import com.inventory.common.response.ApiResponse;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.movements.dto.MovementRequestDto;
import com.inventory.modules.movements.dto.MovementResponseDto;
import com.inventory.modules.movements.entity.MovementType;
import com.inventory.modules.movements.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
@Tag(name = "Movements", description = "Endpoints for stock entries, exits, and movement history")
@SecurityRequirement(name = "bearerAuth")
public class MovementController {

    private final MovementService movementService;

    @PostMapping("/in")
    @Operation(summary = "Record stock entry (IN)")
    public ResponseEntity<ApiResponse<MovementResponseDto>> recordEntry(
            @Valid @RequestBody MovementRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MovementResponseDto response = movementService.createMovement(MovementType.IN, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock entry recorded successfully", response));
    }

    @PostMapping("/out")
    @Operation(summary = "Record stock exit (OUT)")
    public ResponseEntity<ApiResponse<MovementResponseDto>> recordExit(
            @Valid @RequestBody MovementRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MovementResponseDto response = movementService.createMovement(MovementType.OUT, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock exit recorded successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get movements list or paginated movement history")
    public ResponseEntity<?> getAllMovements(
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (page != null) {
            PagedResponse<MovementResponseDto> paged = movementService.getAllMovements(type, search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
            return ResponseEntity.ok(paged);
        }

        List<MovementResponseDto> list = movementService.getAllMovementsList();
        return ResponseEntity.ok(ApiResponse.success("Movements retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movement by ID with item lines")
    public ResponseEntity<ApiResponse<MovementResponseDto>> getMovementById(@PathVariable Long id) {
        MovementResponseDto movement = movementService.getMovementById(id);
        return ResponseEntity.ok(ApiResponse.success("Movement retrieved successfully", movement));
    }
}
