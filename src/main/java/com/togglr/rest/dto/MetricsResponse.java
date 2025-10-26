package com.togglr.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricsResponse {
    private long totalFeatures;
    private long activeFeatures;
    private long totalEnvironments;
    private long totalNamespaces;
}