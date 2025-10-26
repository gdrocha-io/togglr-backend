package com.togglr.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNamespaceRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}