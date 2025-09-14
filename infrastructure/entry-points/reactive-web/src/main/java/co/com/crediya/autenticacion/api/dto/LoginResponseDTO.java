package co.com.crediya.autenticacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de login con tokens de autenticación")
public record LoginResponseDTO(
        @Schema(description = "Tipo de token", example = "Bearer")
        String token_type,
        @Schema(description = "Token de acceso JWT")
        String access_token,
        @Schema(description = "Token de refresco")
        String refresh_token,
        @Schema(description = "Tiempo de expiración en segundos", example = "3600")
        long expires_in,
        @Schema(description = "Correo electrónico del usuario autenticado")
        String subject,
        @Schema(description = "Rol del usuario", example = "ADMINISTRADOR")
        String role,
        @Schema(description = "Permisos del usuario separados por espacio")
        String scope
) {}
