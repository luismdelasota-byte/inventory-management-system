package com.inventory.modules.users.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.users.dto.CreateUserRequestDto;
import com.inventory.modules.users.dto.UpdateUserRequestDto;
import com.inventory.modules.users.dto.UserResponseDto;
import com.inventory.modules.users.entity.Role;
import com.inventory.modules.users.entity.User;
import com.inventory.modules.users.mapper.UserMapper;
import com.inventory.modules.users.repository.RoleRepository;
import com.inventory.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsersList() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponseDto> getAllUsers(String search, Pageable pageable) {
        Page<User> usersPage = userRepository.findAllWithSearch(search, pageable);
        List<UserResponseDto> dtos = usersPage.getContent().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());

        return PagedResponse.<UserResponseDto>builder()
                .success(true)
                .message("Users retrieved successfully")
                .data(dtos)
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BadRequestException("Role not found: " + request.getRole()));

        User user = User.builder()
                .fullName(request.getName().trim())
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equalsIgnoreCase(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new BadRequestException("Role not found: " + request.getRole()));

        // Check if trying to change the role of the last active admin
        if ("ADMIN".equals(user.getRole().getName()) && !"ADMIN".equals(role.getName()) && Boolean.TRUE.equals(user.getActive())) {
            if (userRepository.countActiveAdmins() <= 1) {
                throw new BadRequestException("Cannot demote the last active administrator.");
            }
        }

        user.setFullName(request.getName().trim());
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(role);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public UserResponseDto toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // If currently active and is admin, check if last admin
        if (Boolean.TRUE.equals(user.getActive()) && "ADMIN".equals(user.getRole().getName())) {
            if (userRepository.countActiveAdmins() <= 1) {
                throw new BadRequestException("Cannot deactivate the last active administrator.");
            }
        }

        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        User updated = userRepository.save(user);
        return userMapper.toResponseDto(updated);
    }
}
