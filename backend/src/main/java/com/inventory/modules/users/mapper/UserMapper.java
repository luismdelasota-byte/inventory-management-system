package com.inventory.modules.users.mapper;

import com.inventory.modules.auth.dto.LoginResponseDto;
import com.inventory.modules.users.dto.UserResponseDto;
import com.inventory.modules.users.entity.User;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class UserMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public UserResponseDto toResponseDto(User user) {
        if (user == null) return null;
        return UserResponseDto.builder()
                .id(String.valueOf(user.getId()))
                .name(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .status(Boolean.TRUE.equals(user.getActive()) ? "ACTIVE" : "INACTIVE")
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : null)
                .build();
    }

    public LoginResponseDto.UserInfoDto toUserInfoDto(User user) {
        if (user == null) return null;
        return LoginResponseDto.UserInfoDto.builder()
                .id(String.valueOf(user.getId()))
                .name(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .status(Boolean.TRUE.equals(user.getActive()) ? "ACTIVE" : "INACTIVE")
                .build();
    }
}
