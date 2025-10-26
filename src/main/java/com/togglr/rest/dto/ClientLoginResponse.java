package com.togglr.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientLoginResponse {
    private String token;
    private ClientInfo client;

    @Data
    @AllArgsConstructor
    public static class ClientInfo {
        private String id;
        private String name;
        private String clientId;
        private String scopes;
    }
}