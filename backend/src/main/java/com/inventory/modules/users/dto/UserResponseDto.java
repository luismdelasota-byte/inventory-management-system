package com.inventory.modules.users.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String id;
    private String name;
    private String username;
    private String email;
    private String role;
    private String status;
    private String createdAt;
}
