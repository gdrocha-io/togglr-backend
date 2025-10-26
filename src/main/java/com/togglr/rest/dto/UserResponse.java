package com.togglr.rest.dto;

import com.togglr.security.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        String description,
        String roles,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getDescription(),
                user.getRoles(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}