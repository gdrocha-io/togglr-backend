package com.togglr.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateApiClientRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    private String scopes;
}