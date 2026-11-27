package cl.sonidovivo.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sonidoVivoOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API REST Tienda Sonido Vivo (EP3 - DSY1104)")
                        .version("1.0.0")
                        .description("Backend Spring Boot con MySQL, JPA, Autenticación JWT y control de acceso por roles (Administrador, Vendedor, Cliente) para Tienda Sonido Vivo, Viña del Mar.")
                        .contact(new Contact()
                                .name("Grupo 02 · Sección DSY1104-006D")
                                .email("contacto@sonidovivo.cl")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido desde POST /api/auth/login")));
    }
}
