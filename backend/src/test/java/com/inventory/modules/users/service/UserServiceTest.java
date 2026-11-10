package com.inventory.modules.users.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.modules.users.dto.CreateUserRequestDto;
import com.inventory.modules.users.dto.UserResponseDto;
import com.inventory.modules.users.entity.Role;
import com.inventory.modules.users.entity.User;
import com.inventory.modules.users.mapper.UserMapper;
import com.inventory.modules.users.repository.RoleRepository;
import com.inventory.modules.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role adminRole;
    private User user;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(1L).name("ADMIN").build();
        user = User.builder()
                .id(1L)
                .fullName("Alex Morgan")
                .username("alex.morgan")
                .email("alex@inventory.com")
                .password("encodedPassword")
                .role(adminRole)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Create user with unique username and email succeeds")
    void createUser_Success() {
        CreateUserRequestDto request = new CreateUserRequestDto("Alex Morgan", "alex.morgan", "alex@inventory.com", "Password123!", "ADMIN");

        when(userRepository.existsByUsername("alex.morgan")).thenReturn(false);
        when(userRepository.existsByEmail("alex@inventory.com")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto dto = UserResponseDto.builder().id("1").name("Alex Morgan").username("alex.morgan").role("ADMIN").status("ACTIVE").build();
        when(userMapper.toResponseDto(user)).thenReturn(dto);

        UserResponseDto result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("alex.morgan");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create user with duplicate username throws ConflictException")
    void createUser_DuplicateUsername_ThrowsConflict() {
        CreateUserRequestDto request = new CreateUserRequestDto("Alex Morgan", "alex.morgan", "alex@inventory.com", "Password123!", "ADMIN");
        when(userRepository.existsByUsername("alex.morgan")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Create user with duplicate email throws ConflictException")
    void createUser_DuplicateEmail_ThrowsConflict() {
        CreateUserRequestDto request = new CreateUserRequestDto("Alex Morgan", "alex.morgan", "alex@inventory.com", "Password123!", "ADMIN");
        when(userRepository.existsByUsername("alex.morgan")).thenReturn(false);
        when(userRepository.existsByEmail("alex@inventory.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Cannot deactivate last active administrator")
    void toggleUserStatus_LastActiveAdmin_ThrowsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countActiveAdmins()).thenReturn(1L);

        assertThatThrownBy(() -> userService.toggleUserStatus(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot deactivate the last active administrator");
    }
}
