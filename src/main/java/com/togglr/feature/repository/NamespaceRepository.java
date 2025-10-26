package com.togglr.feature.repository;

import com.togglr.feature.entity.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NamespaceRepository extends JpaRepository<Namespace, Long> {
    Optional<Namespace> findByName(String name);
}