package com.togglr.feature.service;


import com.togglr.feature.entity.Environment;
import com.togglr.feature.repository.EnvironmentRepository;
import com.togglr.feature.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentService {
    private final EnvironmentRepository environmentRepository;
    private final FeatureRepository featureRepository;

    public List<Environment> findAll() {
        return environmentRepository.findAll();
    }

    public Environment findById(Long id) {
        return environmentRepository.findById(id)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("Environment", id));
    }

    public Environment findByName(String name) {
        return environmentRepository.findByName(name)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("Environment", name));
    }

    public Environment create(String name) {
        return environmentRepository.save(Environment.builder().name(name).build());
    }

    public Environment update(Long id, String name) {
        Environment environment = findById(id);
        environment.setName(name);

        return environmentRepository.save(environment);
    }

    public void delete(Long id) {
        Environment environment = findById(id);
        long featureCount = featureRepository.countByEnvironment(environment);

        if (featureCount > 0) {
            throw new com.togglr.rest.exception.BadRequestException("Cannot delete environment with " + featureCount + " associated features");
        }

        environmentRepository.deleteById(id);
    }

    public Environment findOrCreate(String name) {
        return environmentRepository.findByName(name)
                .orElseGet(() -> create(name));
    }

    public long countActiveFeaturesByEnvironment(Environment environment) {
        return featureRepository.countByEnvironmentAndEnabled(environment, true);
    }

    public long countInactiveFeaturesByEnvironment(Environment environment) {
        return featureRepository.countByEnvironmentAndEnabled(environment, false);
    }

    public long countTotalFeaturesByEnvironment(Environment environment) {
        return featureRepository.countByEnvironment(environment);
    }
}