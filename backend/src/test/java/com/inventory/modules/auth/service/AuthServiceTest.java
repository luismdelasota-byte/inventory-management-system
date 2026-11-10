package com.inventory.modules.auth.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.modules.auth.dto.LoginRequestDto;
import com.inventory.modules.auth.dto.LoginResponseDto;
import com.inventory.modules.users.entity.Role;
import com.inventory.modules.users.entity.User;
import com.inventory.modules.users.mapper.UserMapper;
import com.inventory.modules.users.repository.UserRepository;
import com.inventory.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(1L).name("ADMIN").build();
        activeUser = User.builder()
                .id(1L)
                .fullName("Alex Morgan")
                .username("alex.morgan")
                .email("admin@inventory.com")
                .password("$2a$10$encodedPassword")
                .active(true)
                .role(adminRole)
                .build();
    }

    @Test
    @DisplayName("Login with valid credentials returns token and user info")
    void login_Successful() {
        LoginRequestDto request = new LoginRequestDto("admin@inventory.com", "Admin123!");
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(userRepository.findByUsernameOrEmail("admin@inventory.com", "admin@inventory.com"))
                .thenReturn(Optional.of(activeUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt.token.here");

        LoginResponseDto.UserInfoDto userInfoDto = LoginResponseDto.UserInfoDto.builder()
                .id("1")
                .name("Alex Morgan")
                .username("alex.morgan")
                .email("admin@inventory.com")
                .role("ADMIN")
                .status("ACTIVE")
                .build();
        when(userMapper.toUserInfoDto(activeUser)).thenReturn(userInfoDto);

        LoginResponseDto response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("admin@inventory.com");
    }

    @Test
    @DisplayName("Login with non-existent user throws BadCredentialsException")
    void login_NonExistentUser_ThrowsException() {
        LoginRequestDto request = new LoginRequestDto("unknown@inventory.com", "Password123!");
        when(userRepository.findByUsernameOrEmail("unknown@inventory.com", "unknown@inventory.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Login with inactive user throws BadRequestException")
    void login_InactiveUser_ThrowsException() {
        activeUser.setActive(false);
        LoginRequestDto request = new LoginRequestDto("alex.morgan", "Admin123!");

        when(userRepository.findByUsernameOrEmail("alex.morgan", "alex.morgan"))
                .thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User account is inactive");
    }

    @Test
    @DisplayName("Login with wrong password throws BadCredentialsException")
    void login_WrongPassword_ThrowsException() {
        LoginRequestDto request = new LoginRequestDto("admin@inventory.com", "WrongPassword!");

        when(userRepository.findByUsernameOrEmail("admin@inventory.com", "admin@inventory.com"))
                .thenReturn(Optional.of(activeUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
