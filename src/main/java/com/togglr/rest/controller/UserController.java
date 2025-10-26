package com.togglr.rest.controller;

import com.togglr.rest.dto.ChangePasswordRequest;
import com.togglr.rest.dto.CreateUserRequest;
import com.togglr.rest.dto.UpdateUserRequest;
import com.togglr.rest.dto.UserResponse;
import com.togglr.security.entity.User;
import com.togglr.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management operations")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (excluding root user)")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.findAll().stream()
                .filter(user -> !"root".equals(user.getUsername()))
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve specific user by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        return UserResponse.from(userService.findById(id));
    }

    @PostMapping
    @Operation(
        summary = "Create new user", 
        description = "Create a new user with specified roles. Available roles: USER (read-only), MANAGER (create/edit features), ADMIN (full access)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or username already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User creation data",
            content = @Content(examples = @ExampleObject(
                value = "{\"username\": \"john.doe\", \"password\": \"securePassword123\", \"name\": \"John Doe\", \"email\": \"john@company.com\", \"description\": \"Developer\", \"roles\": \"USER,MANAGER\"}"
            ))
        )
        @Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user: {}", request.username());
        User user = userService.create(request.username(), request.password(), request.name(), request.email(), request.description(), request.roles());
        log.info("User created with ID: {}", user.getId());
        return UserResponse.from(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update existing user information")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id, 
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user ID: {} with username: {}", id, request.username());
        User user = userService.update(id, request.username(), request.password(), request.name(), request.email(), request.description(), request.roles(), request.enabled());
        return UserResponse.from(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Deleting user ID: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-password")
    @Operation(
        summary = "Change password", 
        description = "Change current authenticated user's password. Requires current password for security validation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Current password is incorrect"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Void> changePassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Password change data",
            content = @Content(examples = @ExampleObject(
                value = "{\"currentPassword\": \"oldPassword123\", \"newPassword\": \"newSecurePassword456\"}"
            ))
        )
        @Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        String username = authentication.getName();
        log.info("Changing password for user: {}", username);
        userService.changePassword(username, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}