package com.togglr.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Togglr API")
                        .version("1.0.0")
                        .description("""
                                # Feature Toggle Management System
                                
                                Complete feature toggle management system with:
                                - **Namespaces**: Organize features by domain/application
                                - **Environments**: Separate control for dev, staging, production
                                - **JSON Metadata**: Flexible configuration support
                                - **Redis/Caffeine Cache**: Optimized performance
                                - **Audit Trail**: Complete change tracking
                                
                                ## Authentication
                                Use `/api/auth/login` endpoint to obtain a JWT token.
                                """)
                        .contact(new Contact()
                                .name("API Team")
                                .email("api@company.com")
                                .url("https://github.com/company/togglr-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.company.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using Bearer scheme. Example: 'Bearer {token}'")));
    }
}
