package com.togglr.feature.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.togglr.feature.entity.Environment;
import com.togglr.feature.entity.Feature;
import com.togglr.feature.entity.Namespace;
import com.togglr.feature.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FeatureService {
    private final FeatureRepository featureRepository;
    private final NamespaceService namespaceService;
    private final EnvironmentService environmentService;

    @Cacheable(value = "features", key = "#name + '_' + #namespaceName + '_' + #environmentName")
    public Feature getFeature(String name, String namespaceName, String environmentName) {
        Namespace namespace = namespaceService.findByName(namespaceName);
        Environment environment = environmentService.findByName(environmentName);

        return featureRepository.findByNameAndNamespaceAndEnvironment(name, namespace, environment)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException(
                        String.format("Feature '%s' not found in namespace '%s' and environment '%s'", name, namespaceName, environmentName)));
    }

    @Cacheable(value = "features", key = "'enabled_' + #namespaceName + '_' + #environmentName")
    public List<Feature> getEnabledFeatures(String namespaceName, String environmentName) {
        Namespace namespace = namespaceService.findByName(namespaceName);
        Environment environment = environmentService.findByName(environmentName);

        return featureRepository.findEnabledFeatures(namespace, environment);
    }

    @Cacheable(value = "features", key = "'namespace_' + #namespaceName + '_' + #environmentName")
    public List<Feature> getFeaturesByNamespaceAndEnvironment(String namespaceName, String environmentName) {
        Namespace namespace = namespaceService.findByName(namespaceName);
        Environment environment = environmentService.findByName(environmentName);

        return featureRepository.findByNamespaceAndEnvironment(namespace, environment);
    }

    @CacheEvict(value = {"features", "metrics"}, allEntries = true)
    public Feature createFeature(String name, String namespaceName, String environmentName, Boolean enabled, JsonNode metadata) {
        Namespace namespace = namespaceService.findOrCreate(namespaceName);
        Environment environment = environmentService.findOrCreate(environmentName);

        Feature feature = Feature.builder()
                .name(name)
                .namespace(namespace)
                .environment(environment)
                .enabled(enabled)
                .metadata(metadata)
                .build();

        return featureRepository.save(feature);
    }

    @CacheEvict(value = {"features", "metrics"}, allEntries = true)
    public Feature updateFeature(Long id, Boolean enabled, JsonNode metadata) {
        Feature feature = featureRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("Feature", id));

        if (enabled != null) {
            feature.setEnabled(enabled);
        }

        if (metadata != null) {
            feature.setMetadata(metadata);
        }

        return featureRepository.save(feature);
    }

    @CacheEvict(value = {"features", "metrics"}, allEntries = true)
    public void deleteFeature(Long id) {
        featureRepository.deleteById(id);
    }

    @Cacheable(value = "features", key = "'all_features'")
    public List<Feature> getAllFeatures() {
        return featureRepository.findAllWithRelations();
    }
}
