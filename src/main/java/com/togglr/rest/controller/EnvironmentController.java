package com.togglr.rest.controller;

import com.togglr.feature.entity.Environment;
import com.togglr.feature.service.EnvironmentService;
import com.togglr.rest.dto.CreateEnvironmentRequest;
import com.togglr.rest.dto.EnvironmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/environments")
@Tag(name = "Environments", description = "Environment management for deployment contexts")
@RequiredArgsConstructor
@Slf4j
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @GetMapping
    @Operation(summary = "Get all environments", description = "Retrieve all environments with feature statistics")
    public List<EnvironmentResponse> getAllEnvironments() {
        return environmentService.findAll().stream()
                .map(env -> EnvironmentResponse.from(env,
                        environmentService.countTotalFeaturesByEnvironment(env),
                        environmentService.countActiveFeaturesByEnvironment(env),
                        environmentService.countInactiveFeaturesByEnvironment(env)))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get environment by ID", description = "Retrieve specific environment with feature statistics")
    public EnvironmentResponse getEnvironment(
            @Parameter(description = "Environment ID", example = "1") @PathVariable Long id) {
        Environment environment = environmentService.findById(id);
        return EnvironmentResponse.from(environment,
                environmentService.countTotalFeaturesByEnvironment(environment),
                environmentService.countActiveFeaturesByEnvironment(environment),
                environmentService.countInactiveFeaturesByEnvironment(environment));
    }

    @PostMapping
    @Operation(summary = "Create new environment", description = "Create a new deployment environment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EnvironmentResponse createEnvironment(@Valid @RequestBody CreateEnvironmentRequest request) {
        log.info("Creating environment: {}", request.name());
        Environment environment = environmentService.create(request.name());
        log.info("Environment created with ID: {}", environment.getId());
        return EnvironmentResponse.from(environment,
                environmentService.countTotalFeaturesByEnvironment(environment),
                environmentService.countActiveFeaturesByEnvironment(environment),
                environmentService.countInactiveFeaturesByEnvironment(environment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete environment", description = "Delete environment by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnvironment(
            @Parameter(description = "Environment ID", example = "1") @PathVariable Long id) {
        log.info("Deleting environment ID: {}", id);
        environmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}