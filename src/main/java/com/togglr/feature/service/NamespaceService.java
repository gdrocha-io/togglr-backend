package com.togglr.feature.service;


import com.togglr.feature.entity.Namespace;
import com.togglr.feature.repository.FeatureRepository;
import com.togglr.feature.repository.NamespaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NamespaceService {
    private final NamespaceRepository namespaceRepository;
    private final FeatureRepository featureRepository;

    public List<Namespace> findAll() {
        return namespaceRepository.findAll();
    }

    public Namespace findById(Long id) {
        return namespaceRepository.findById(id)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("Namespace", id));
    }

    public Namespace findByName(String name) {
        return namespaceRepository.findByName(name)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("Namespace", name));
    }

    public Namespace create(String name) {
        return namespaceRepository.save(Namespace.builder().name(name).build());
    }

    public Namespace update(Long id, String name) {
        Namespace namespace = findById(id);
        namespace.setName(name);
        return namespaceRepository.save(namespace);
    }

    public void delete(Long id) {
        Namespace namespace = findById(id);
        long featureCount = featureRepository.countByNamespace(namespace);
        if (featureCount > 0) {
            throw new com.togglr.rest.exception.BadRequestException("Cannot delete namespace with " + featureCount + " associated features");
        }
        namespaceRepository.deleteById(id);
    }

    public Namespace findOrCreate(String name) {
        return namespaceRepository.findByName(name)
                .orElseGet(() -> create(name));
    }

    public long countTotalFeaturesByNamespace(Namespace namespace) {
        return featureRepository.countByNamespace(namespace);
    }

    public long countActiveFeaturesByNamespace(Namespace namespace) {
        return featureRepository.countByNamespaceAndEnabled(namespace, true);
    }

    public long countInactiveFeaturesByNamespace(Namespace namespace) {
        return featureRepository.countByNamespaceAndEnabled(namespace, false);
    }
}