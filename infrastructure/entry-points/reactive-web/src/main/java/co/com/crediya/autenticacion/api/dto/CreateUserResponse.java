package co.com.crediya.autenticacion.api.dto;

import co.com.crediya.autenticacion.model.usuario.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Respuesta de creación de usuario")
public class CreateUserResponse {
    
    @Schema(description = "Nombre del usuario", example = "Juan")
    String nombre;
    
    @Schema(description = "Apellido del usuario", example = "Pérez")
    String apellido;
    
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@email.com")
    String email;
    
    @Schema(description = "Número de teléfono", example = "3001234567")
    String telefono;
    
    @Schema(description = "ID del rol asignado al usuario", example = "1")
    Long rolId;
    
    @Schema(description = "Salario base del usuario", example = "1500000")
    int salario_base;

    public static CreateUserResponse from(Usuario u) {

        return CreateUserResponse.builder()
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .rolId(u.getRolId() != null ? u.getRolId() : null)
                .salario_base(u.getSalario_base())
                .build();
    }
}
