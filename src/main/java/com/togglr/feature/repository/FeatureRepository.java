package com.togglr.feature.repository;

import com.togglr.feature.entity.Environment;
import com.togglr.feature.entity.Feature;
import com.togglr.feature.entity.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {
    @Query("SELECT f FROM Feature f JOIN FETCH f.namespace JOIN FETCH f.environment WHERE f.id = :id")
    Optional<Feature> findByIdWithRelations(@Param("id") Long id);

    Optional<Feature> findByNameAndNamespaceAndEnvironment(String name, Namespace namespace, Environment environment);

    List<Feature> findByNamespaceAndEnvironment(Namespace namespace, Environment environment);

    @Query("SELECT f FROM Feature f WHERE f.namespace = :namespace AND f.environment = :environment AND f.enabled = true")
    List<Feature> findEnabledFeatures(@Param("namespace") Namespace namespace, @Param("environment") Environment environment);

    @Query("SELECT f FROM Feature f JOIN FETCH f.namespace JOIN FETCH f.environment")
    List<Feature> findAllWithRelations();

    long countByEnvironment(Environment environment);

    long countByEnvironmentAndEnabled(Environment environment, boolean enabled);

    long countByNamespace(Namespace namespace);

    long countByNamespaceAndEnabled(Namespace namespace, boolean enabled);

    @Query("SELECT COUNT(f) FROM Feature f WHERE f.enabled = true")
    long countActiveFeatures();
}
