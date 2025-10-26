package com.togglr.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.togglr.feature.entity.Feature;

import java.time.LocalDateTime;

public record FeatureResponse(
        Long id,
        String name,
        String namespace,
        String environment,
        Boolean enabled,
        JsonNode metadata,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FeatureResponse from(Feature feature) {
        return new FeatureResponse(
                feature.getId(),
                feature.getName(),
                feature.getNamespace().getName(),
                feature.getEnvironment().getName(),
                feature.getEnabled(),
                feature.getMetadata(),
                feature.getCreatedAt(),
                feature.getUpdatedAt()
        );
    }
}