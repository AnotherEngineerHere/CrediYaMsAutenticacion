package co.com.crediya.autenticacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO para datos básicos de usuario para consultas inter-servicio.
 */
@Builder
@Schema(description = "Datos básicos de usuario para consultas inter-servicio")
public record UserDataDTO(
        @Schema(description = "Correo electrónico del usuario", example = "usuario@email.com")
        String email,

        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        String nombre,

        @Schema(description = "Salario base del usuario", example = "3000000")
        Long salarioBase
) {}