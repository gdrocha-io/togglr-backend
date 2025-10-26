package com.togglr.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record UpdateFeatureRequest(
        Boolean enabled,
        JsonNode metadata
) {
}
