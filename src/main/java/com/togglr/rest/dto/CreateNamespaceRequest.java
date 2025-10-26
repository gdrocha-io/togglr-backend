package com.togglr.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateNamespaceRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}