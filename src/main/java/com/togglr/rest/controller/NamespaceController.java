package com.togglr.rest.controller;

import com.togglr.feature.entity.Namespace;
import com.togglr.feature.service.NamespaceService;
import com.togglr.rest.dto.CreateNamespaceRequest;
import com.togglr.rest.dto.NamespaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/namespaces")
@Tag(name = "Namespaces", description = "Namespace management for feature organization")
@RequiredArgsConstructor
public class NamespaceController {

    private final NamespaceService namespaceService;

    @GetMapping
    @Operation(summary = "Get all namespaces", description = "Retrieve all namespaces with feature statistics")
    public List<NamespaceResponse> getAllNamespaces() {
        return namespaceService.findAll().stream()
                .map(ns -> NamespaceResponse.from(ns,
                        namespaceService.countTotalFeaturesByNamespace(ns),
                        namespaceService.countActiveFeaturesByNamespace(ns),
                        namespaceService.countInactiveFeaturesByNamespace(ns)))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get namespace by ID", description = "Retrieve specific namespace with feature statistics")
    public NamespaceResponse getNamespace(
            @Parameter(description = "Namespace ID", example = "1") @PathVariable Long id) {
        Namespace namespace = namespaceService.findById(id);
        return NamespaceResponse.from(namespace,
                namespaceService.countTotalFeaturesByNamespace(namespace),
                namespaceService.countActiveFeaturesByNamespace(namespace),
                namespaceService.countInactiveFeaturesByNamespace(namespace));
    }

    @PostMapping
    @Operation(summary = "Create new namespace", description = "Create a new namespace for feature organization")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public NamespaceResponse createNamespace(@Valid @RequestBody CreateNamespaceRequest request) {
        Namespace namespace = namespaceService.create(request.name());
        return NamespaceResponse.from(namespace,
                namespaceService.countTotalFeaturesByNamespace(namespace),
                namespaceService.countActiveFeaturesByNamespace(namespace),
                namespaceService.countInactiveFeaturesByNamespace(namespace));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete namespace", description = "Delete namespace by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNamespace(
            @Parameter(description = "Namespace ID", example = "1") @PathVariable Long id) {
        namespaceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}