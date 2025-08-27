package co.com.crediya.autenticacion.api.dto;

import co.com.crediya.autenticacion.model.rol.Rol;
import lombok.Data;

@Data
public class CreateUserDTO {

    private String nombre;

    private String apellido;

    private String email;

    private String contrasena;

    private String documento_identidad;

    private String telefono;

    private Long rolId;

    private int salario_base;
}
