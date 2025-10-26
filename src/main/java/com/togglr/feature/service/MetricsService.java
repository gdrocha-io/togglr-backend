package com.togglr.feature.service;

import com.togglr.feature.repository.EnvironmentRepository;
import com.togglr.feature.repository.FeatureRepository;
import com.togglr.feature.repository.NamespaceRepository;
import com.togglr.rest.dto.MetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final FeatureRepository featureRepository;
    private final EnvironmentRepository environmentRepository;
    private final NamespaceRepository namespaceRepository;

    @Cacheable(value = "metrics", key = "'dashboard'")
    public MetricsResponse getDashboardMetrics() {
        return new MetricsResponse(
                featureRepository.count(),
                featureRepository.countActiveFeatures(),
                environmentRepository.count(),
                namespaceRepository.count()
        );
    }
}