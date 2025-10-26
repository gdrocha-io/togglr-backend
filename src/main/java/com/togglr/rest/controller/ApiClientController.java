package com.togglr.rest.controller;

import com.togglr.rest.dto.ApiClientResponse;
import com.togglr.rest.dto.CreateApiClientRequest;
import com.togglr.security.entity.ApiClient;
import com.togglr.security.service.ApiClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "API Clients", description = "API client management for application authentication")
public class ApiClientController {
    private final ApiClientService apiClientService;

    @PostMapping
    @Operation(
        summary = "Create new API client", 
        description = "Create a new API client with client_id and client_secret for programmatic access. Scopes control access permissions (READ, WRITE)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API client created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or client_id already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    public ResponseEntity<ApiClientResponse> createClient(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "API client creation data",
            content = @Content(examples = @ExampleObject(
                value = "{\"name\": \"Mobile App\", \"clientId\": \"mobile-app-client\", \"clientSecret\": \"super-secret-key\", \"scopes\": \"READ,WRITE\"}"
            ))
        )
        @Valid @RequestBody CreateApiClientRequest request) {
        ApiClient client = apiClientService.createClient(
                request.getName(),
                request.getClientId(),
                request.getClientSecret(),
                request.getScopes()
        );
        return ResponseEntity.ok(ApiClientResponse.from(client));
    }

    @GetMapping
    @Operation(summary = "Get all API clients", description = "Retrieve all API clients")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    public List<ApiClientResponse> getAllClients() {
        return apiClientService.findAll().stream()
                .map(ApiClientResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete API client", description = "Delete API client by ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "API Client ID", example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable UUID id) {
        apiClientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}