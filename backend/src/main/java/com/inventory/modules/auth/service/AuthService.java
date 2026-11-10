package com.inventory.modules.auth.service;

import com.inventory.modules.auth.dto.LoginRequestDto;
import com.inventory.modules.auth.dto.LoginResponseDto;
import com.inventory.modules.users.dto.UserResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto request);
    UserResponseDto getCurrentUser(String username);
}
