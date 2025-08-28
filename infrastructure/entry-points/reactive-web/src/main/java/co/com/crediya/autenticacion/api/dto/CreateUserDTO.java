package co.com.crediya.autenticacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "DTO para crear un nuevo usuario")
public class CreateUserDTO {

    @Schema(description = "Nombre del usuario", example = "Juan", required = true, minLength = 2, maxLength = 50)
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Pérez", required = true, minLength = 2, maxLength = 50)
    private String apellido;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@email.com", required = true, format = "email")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "password123", required = true, minLength = 8)
    private String contrasena;

    @Schema(description = "Número de documento de identidad", example = "12345678")
    private String documento_identidad;

    @Schema(description = "Número de teléfono", example = "3001234567", minLength = 10, maxLength = 20)
    private String telefono;

    @Schema(description = "ID del rol asignado al usuario", example = "1", required = true)
    private Long rolId;

    @Schema(description = "Salario base del usuario", example = "1500000", minimum = "0", maximum = "15000000", required = true)
    private Long salario_base;

    @Schema(description = "Fecha de nacimiento del usuario", example = "1995-08-15", required = true, format = "date")
    private LocalDate fecha_nacimiento;

    @Schema(description = "Dirección de residencia del usuario", example = "Calle 123 #45-67, Bogotá", required = true)
    private String direccion;
}
