package co.com.crediya.autenticacion.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("CrediYa - Autenticación")
                .version("v1")
                .description("API para gestión de usuarios (registro, consulta, etc.)")
                .contact(new Contact().name("Equipo CrediYa").email("dev@crediya.com"))
                .license(new License().name("Apache-2.0")));
    }
}
