package co.com.crediya.autenticacion.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO para ingresar al sistema")
public class LoginDTO {
    String correoElectronico;
    String contrasena;
}
