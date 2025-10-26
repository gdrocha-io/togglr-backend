package com.togglr.rest.dto;

import com.togglr.security.entity.ApiClient;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiClientResponse {
    private String id;
    private String name;
    private String clientId;
    private String scopes;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public static ApiClientResponse from(ApiClient client) {
        return new ApiClientResponse(
                client.getId().toString(),
                client.getName(),
                client.getClientId(),
                client.getScopes(),
                client.getEnabled(),
                client.getCreatedAt()
        );
    }
}