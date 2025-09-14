package co.com.crediya.autenticacion.api.dto;

import co.com.crediya.autenticacion.model.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para datos básicos de usuario para consultas inter-servicio.
 */
@Schema(description = "Datos básicos de usuario para consultas inter-servicio")
public record UserDataDTO(
        @Schema(description = "Correo electrónico del usuario", example = "usuario@email.com")
        String email,

        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        String nombre,

        @Schema(description = "Salario base del usuario", example = "3000000")
        Long salarioBase,

        @Schema(description = "Rol del Usuario", example = "SOLICITANTE")
        Long rol
) {
    public static UserDataDTO from(Usuario usuario) {
        return new UserDataDTO(
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getSalario_base(),
                usuario.getRolId()
        );
    }
}
