package com.togglr.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeatureRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Namespace is required")
        String namespace,

        @NotBlank(message = "Environment is required")
        String environment,

        @NotNull(message = "Enabled status is required")
        Boolean enabled,

        JsonNode metadata
) {
}
