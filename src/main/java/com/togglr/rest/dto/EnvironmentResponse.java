package com.togglr.rest.dto;

import com.togglr.feature.entity.Environment;

import java.time.LocalDateTime;

public record EnvironmentResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        long totalFeatures,
        long activeFeatures,
        long inactiveFeatures
) {
    public static EnvironmentResponse from(Environment environment, long totalFeatures, long activeFeatures, long inactiveFeatures) {
        return new EnvironmentResponse(
                environment.getId(),
                environment.getName(),
                environment.getCreatedAt(),
                totalFeatures,
                activeFeatures,
                inactiveFeatures
        );
    }
}