package com.togglr.security.service;

import com.togglr.security.entity.ApiClient;
import com.togglr.security.repository.ApiClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiClientService {
    private final ApiClientRepository apiClientRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiClient authenticateClient(String clientId, String clientSecret) {
        return apiClientRepository.findByClientId(clientId)
                .filter(client -> client.getEnabled() &&
                        passwordEncoder.matches(clientSecret, client.getClientSecret()))
                .orElse(null);
    }

    public ApiClient createClient(String name, String clientId, String clientSecret, String scopes) {
        if (apiClientRepository.findByClientId(clientId).isPresent()) {
            throw new RuntimeException("Client ID '" + clientId + "' already exists");
        }

        ApiClient client = ApiClient.builder()
                .name(name)
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .scopes(scopes)
                .build();

        return apiClientRepository.save(client);
    }

    public List<ApiClient> findAll() {
        return apiClientRepository.findAll();
    }

    public void delete(UUID id) {
        apiClientRepository.deleteById(id);
    }
}