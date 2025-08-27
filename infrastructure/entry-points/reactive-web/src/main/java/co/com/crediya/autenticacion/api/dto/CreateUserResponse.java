package co.com.crediya.autenticacion.api.dto;

import co.com.crediya.autenticacion.model.usuario.Usuario;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateUserResponse {
    String nombre;
    String apellido;
    String email;
    String telefono;
    Long rolId;
    int salario_base;

    public static CreateUserResponse from(Usuario u) {
        System.out.println("Mapeando respuesta"+ u);
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
