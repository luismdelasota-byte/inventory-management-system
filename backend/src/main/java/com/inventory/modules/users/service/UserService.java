package com.inventory.modules.users.service;

import com.inventory.common.response.PagedResponse;
import com.inventory.modules.users.dto.CreateUserRequestDto;
import com.inventory.modules.users.dto.UpdateUserRequestDto;
import com.inventory.modules.users.dto.UserResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsersList();
    PagedResponse<UserResponseDto> getAllUsers(String search, Pageable pageable);
    UserResponseDto getUserById(Long id);
    UserResponseDto createUser(CreateUserRequestDto request);
    UserResponseDto updateUser(Long id, UpdateUserRequestDto request);
    UserResponseDto toggleUserStatus(Long id);
}
