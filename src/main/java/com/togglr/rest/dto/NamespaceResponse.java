package com.togglr.rest.dto;

import com.togglr.feature.entity.Namespace;

import java.time.LocalDateTime;

public record NamespaceResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        long totalFeatures,
        long activeFeatures,
        long inactiveFeatures
) {
    public static NamespaceResponse from(Namespace namespace, long totalFeatures, long activeFeatures, long inactiveFeatures) {
        return new NamespaceResponse(
                namespace.getId(),
                namespace.getName(),
                namespace.getCreatedAt(),
                totalFeatures,
                activeFeatures,
                inactiveFeatures
        );
    }
}