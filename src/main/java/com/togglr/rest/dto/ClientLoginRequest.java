package com.togglr.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientLoginRequest {
    @NotBlank
    private String client_id;

    @NotBlank
    private String client_secret;
}