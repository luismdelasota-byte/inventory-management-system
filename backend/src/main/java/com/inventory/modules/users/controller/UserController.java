package com.inventory.modules.users.controller;

import com.inventory.common.response.ApiResponse;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.users.dto.CreateUserRequestDto;
import com.inventory.modules.users.dto.UpdateUserRequestDto;
import com.inventory.modules.users.dto.UserResponseDto;
import com.inventory.modules.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user management (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all users or get paginated users (ADMIN)")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (page != null) {
            PagedResponse<UserResponseDto> paged = userService.getAllUsers(search, PageRequest.of(page, size, Sort.by("id").descending()));
            return ResponseEntity.ok(paged);
        }

        List<UserResponseDto> list = userService.getAllUsersList();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (ADMIN)")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PostMapping
    @Operation(summary = "Create a new user (ADMIN)")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user (ADMIN)")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto request) {
        UserResponseDto updated = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle user active status (ADMIN)")
    public ResponseEntity<ApiResponse<UserResponseDto>> toggleStatus(@PathVariable Long id) {
        UserResponseDto updated = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updated));
    }
}
