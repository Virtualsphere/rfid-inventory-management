package com.virtualsphere.rfidbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Powers Swagger UI at /swagger-ui.html (raw spec at /v3/api-docs).
 * Adds a JWT "Authorize" button - paste just the token (no "Bearer " prefix,
 * springdoc adds that for you) after calling POST /api/auth/login.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "RFID Asset Tracking - Backend API",
                version = "1.0.0",
                description = "Shared REST API for the mobile handheld app and admin web panel. "
                        + "Call POST /api/auth/login first, then click Authorize below and paste the returned token.",
                contact = @Contact(name = "Virtualsphere Technologies Pvt Ltd", email = "info@virtualspheretechnologies.in")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig {

    /**
     * Applies the bearerAuth scheme to every operation by default, so the padlock
     * icon (and the token you set via Authorize) shows up on all endpoints without
     * annotating each controller method individually. /api/auth/** is still
     * callable without a token at runtime - Spring Security decides that, not this.
     */
    @Bean
    public OpenApiCustomizer globalSecurityCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation ->
                        operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))));
    }
}
