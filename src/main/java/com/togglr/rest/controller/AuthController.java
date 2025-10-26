package com.togglr.rest.controller;

import com.togglr.rest.dto.ClientLoginRequest;
import com.togglr.rest.dto.ClientLoginResponse;
import com.togglr.rest.dto.LoginRequest;
import com.togglr.rest.dto.LoginResponse;
import com.togglr.security.entity.ApiClient;
import com.togglr.security.repository.UserRepository;
import com.togglr.security.service.ApiClientService;
import com.togglr.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User and API client authentication")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApiClientService apiClientService;

    @PostMapping("/login")
    @Operation(
        summary = "User login", 
        description = "Authenticate user with username and password to obtain JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<LoginResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login credentials",
            content = @Content(examples = @ExampleObject(
                value = "{\"username\": \"admin\", \"password\": \"password\"}"
            ))
        )
        @Valid @RequestBody LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> user.getEnabled() && passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtService.generateToken(user.getUsername(), user.getRoles());
                    LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                            user.getId().toString(),
                            user.getUsername(),
                            user.getName(),
                            user.getEmail(),
                            user.getRoles()
                    );
                    return ResponseEntity.ok(new LoginResponse(token, userInfo));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/client")
    @Operation(
        summary = "API client login", 
        description = "Authenticate API client with client_id and client_secret to obtain JWT token for programmatic access"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Client authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid client credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<ClientLoginResponse> clientLogin(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "API client credentials",
            content = @Content(examples = @ExampleObject(
                value = "{\"client_id\": \"your-client-id\", \"client_secret\": \"your-client-secret\"}"
            ))
        )
        @Valid @RequestBody ClientLoginRequest request) {
        ApiClient client = apiClientService.authenticateClient(request.getClient_id(), request.getClient_secret());

        if (client != null) {
            String token = jwtService.generateClientToken(client.getClientId(), client.getScopes());
            ClientLoginResponse.ClientInfo clientInfo = new ClientLoginResponse.ClientInfo(
                    client.getId().toString(),
                    client.getName(),
                    client.getClientId(),
                    client.getScopes()
            );
            return ResponseEntity.ok(new ClientLoginResponse(token, clientInfo));
        }

        return ResponseEntity.status(401).build();
    }
}