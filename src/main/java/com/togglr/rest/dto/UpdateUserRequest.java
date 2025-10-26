package com.togglr.rest.dto;

public record UpdateUserRequest(
        String username,
        String password,
        String name,
        String email,
        String description,
        String roles,
        Boolean enabled
) {
}