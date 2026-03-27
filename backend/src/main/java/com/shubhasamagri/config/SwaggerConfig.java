package com.shubhasamagri.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 Swagger Configuration.
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 *
 * To test protected endpoints:
 *   1. Use /api/auth/login to get a JWT token
 *   2. Click "Authorize" in Swagger UI
 *   3. Enter: Bearer <your-token>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ShubhaSamagri API",
        version = "1.0.0",
        description = "REST API for ShubhaSamagri - Pooja Essentials & Ritual Kits Ecommerce Platform",
        contact = @Contact(name = "ShubhaSamagri Team", email = "support@shubhasamagri.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server"),
        @Server(url = "https://shubha-samagri-api.onrender.com", description = "Production Server")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "JWT Bearer token. Get token from POST /api/auth/login"
)
public class SwaggerConfig {
    // Configuration is done via annotations above
}
