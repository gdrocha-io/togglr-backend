package com.togglr.rest.controller;

import com.togglr.feature.service.MetricsService;
import com.togglr.rest.dto.MetricsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
@Tag(name = "Metrics", description = "Dashboard metrics and statistics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;
    private final ServletContext servletContext;

    @GetMapping("/dashboard")
    @Operation(
        summary = "Get dashboard metrics", 
        description = "Retrieve comprehensive metrics including total features, namespaces, environments, and feature statistics by status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions")
    })
    public MetricsResponse getDashboardMetrics() {
        return metricsService.getDashboardMetrics();
    }
}