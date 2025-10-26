package com.togglr.rest.controller;

import com.togglr.feature.entity.Feature;
import com.togglr.feature.service.FeatureService;
import com.togglr.rest.dto.CreateFeatureRequest;
import com.togglr.rest.dto.FeatureResponse;
import com.togglr.rest.dto.UpdateFeatureRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@Tag(name = "Features", description = "Feature Toggle Management API")
@RequiredArgsConstructor
@Slf4j
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping
    @Operation(summary = "Get all features", description = "Retrieve all feature toggles across all namespaces and environments")
    public ResponseEntity<List<FeatureResponse>> getAllFeatures() {
        List<FeatureResponse> features = featureService.getAllFeatures().stream()
                .map(FeatureResponse::from)
                .toList();
        return ResponseEntity.ok(features);
    }

    @GetMapping("/enabled")
    @Operation(summary = "Get enabled features", description = "Retrieve only enabled features for specific namespace and environment")
    public ResponseEntity<List<FeatureResponse>> getEnabledFeatures(
            @Parameter(description = "Namespace name", example = "ecommerce") @RequestParam String namespace,
            @Parameter(description = "Environment name", example = "dev") @RequestParam String environment) {
        log.info("Getting enabled features for namespace: {} and environment: {}", namespace, environment);
        List<FeatureResponse> features = featureService.getEnabledFeatures(namespace, environment).stream()
                .map(FeatureResponse::from)
                .toList();
        log.info("Found {} enabled features", features.size());
        return ResponseEntity.ok(features);
    }

    @GetMapping("/feature")
    @Operation(
        summary = "Get specific feature", 
        description = "Retrieve a specific feature by name, namespace, and environment. This endpoint is cached for optimal performance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature found"),
        @ApiResponse(responseCode = "404", description = "Feature not found")
    })
    public ResponseEntity<FeatureResponse> getFeature(
            @Parameter(description = "Feature name", example = "new-checkout") @RequestParam String name,
            @Parameter(description = "Namespace name", example = "ecommerce") @RequestParam String namespace,
            @Parameter(description = "Environment name", example = "dev") @RequestParam String environment) {
        Feature feature = featureService.getFeature(name, namespace, environment);
        return ResponseEntity.ok(FeatureResponse.from(feature));
    }

    @PostMapping
    @Operation(
        summary = "Create new feature", 
        description = "Create a new feature toggle with flexible JSON metadata. Namespaces and environments are created automatically if they don't exist."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Feature created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or feature already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureResponse> createFeature(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Feature creation data",
            content = @Content(examples = {
                @ExampleObject(
                    name = "Simple Feature",
                    value = "{\"name\": \"new-checkout\", \"namespace\": \"ecommerce\", \"environment\": \"dev\", \"enabled\": true, \"metadata\": {}}"
                ),
                @ExampleObject(
                    name = "Feature with Metadata",
                    value = "{\"name\": \"ab-test\", \"namespace\": \"marketing\", \"environment\": \"prod\", \"enabled\": false, \"metadata\": {\"percentage\": 50, \"variants\": [\"A\", \"B\"]}}"
                )
            })
        )
        @Valid @RequestBody CreateFeatureRequest request) {
        log.info("Creating feature: {} in namespace: {} and environment: {}",
                request.name(), request.namespace(), request.environment());
        Feature feature = featureService.createFeature(
                request.name(),
                request.namespace(),
                request.environment(),
                request.enabled(),
                request.metadata()
        );
        log.info("Feature created with ID: {}, enabled: {}", feature.getId(), feature.getEnabled());
        return ResponseEntity.status(HttpStatus.CREATED).body(FeatureResponse.from(feature));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update feature", description = "Update feature enabled status and metadata")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureResponse> updateFeature(
            @Parameter(description = "Feature ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateFeatureRequest request) {
        Feature feature = featureService.updateFeature(id, request.enabled(), request.metadata());
        return ResponseEntity.ok(FeatureResponse.from(feature));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feature", description = "Delete a feature toggle by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFeature(
            @Parameter(description = "Feature ID", example = "1") @PathVariable Long id) {
        featureService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }
}
